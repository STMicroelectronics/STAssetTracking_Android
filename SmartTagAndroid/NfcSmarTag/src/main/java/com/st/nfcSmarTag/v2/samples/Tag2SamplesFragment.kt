package com.st.nfcSmarTag.v2.samples

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.dashboard.uploadData.uploader.UploaderGenericDataActivity
import com.st.assetTracking.data.*
import com.st.nfcSmarTag.v2.NfcTag2ViewModel
import com.st.smartaglibrary.stringId
import com.st.smartaglibrary.util.getTypeSerializableExtra
import com.st.smartaglibrary.v2.SmarTag2Service
import com.st.smartaglibrary.v2.SmarTag2Service.Companion.EXTRA_ERROR_STR
import com.st.smartaglibrary.v2.SmarTag2Service.Companion.EXTRA_TAG_NUMBER_SAMPLE
import com.st.smartaglibrary.v2.SmarTag2Service.Companion.EXTRA_TAG_SAMPLE_DATA
import com.st.smartaglibrary.v2.SmarTag2Service.Companion.getReadDataSampleFilter
import com.st.login.Configuration
import com.st.login.LoginManager
import com.st.login.loginprovider.LoginProviderFactory
import com.st.nfcSmarTag.R

class Tag2SamplesFragment(private val deviceID: String?) : Fragment() {

    private val nfcServiceResponse = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            when (intent?.action) {
                SmarTag2Service.READ_TAG_NUMBER_SAMPLE_DATA_ACTION -> {
                    val numberSample = intent.getIntExtra(EXTRA_TAG_NUMBER_SAMPLE, 0)
                    tag2SamplesViewModel.setNumberSample(numberSample)
                }
                SmarTag2Service.READ_TAG_SAMPLE_DATA_ACTION -> {
                    val data = intent.getTypeSerializableExtra<GenericSample>(EXTRA_TAG_SAMPLE_DATA)
                    tag2SamplesViewModel.appendSample(data)
                }
                SmarTag2Service.READ_TAG_ERROR_ACTION -> {
                    val msg = intent.getStringExtra(EXTRA_ERROR_STR)
                    readProgress.visibility=View.GONE // Hide the progress after an error
                    nfcTag2ViewModel.nfcTagError(msg!!)
                }
            }
        }
    }

    private lateinit var nfcTag2ViewModel: NfcTag2ViewModel
    private lateinit var tag2SamplesViewModel: Tag2SamplesViewModel

    private lateinit var rootView: View
    private lateinit var readProgress: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_tag2_samples, container, false)
        rootView.findViewById<ViewPager>(R.id.tag2_samples_viewPager).adapter =
            TabViewAdapter(childFragmentManager)
        tag2SamplesViewModel = Tag2SamplesViewModel.create(requireActivity())
        nfcTag2ViewModel = NfcTag2ViewModel.create(requireActivity())
        initializeNfcTagObserver()

        readProgress = rootView.findViewById(R.id.tag2_samples_readProgress)

        activity?.title = "Samples"

        return rootView
    }

    private fun initializeNfcTagObserver() {

        nfcTag2ViewModel.nfcTag.observe(viewLifecycleOwner, Observer {
            if (it != null) SmarTag2Service.startReadingTag2DataSample(requireContext(), it)
        })
        tag2SamplesViewModel.numberSample.observe(viewLifecycleOwner, Observer {
            it?.let { nSample ->
                Log.d("progress", "Total: $nSample")
                readProgress.max = nSample
                readProgress.visibility = View.VISIBLE
            }
        })
        tag2SamplesViewModel.lastSensorSample.observe(viewLifecycleOwner,getProgressUpdate())

        tag2SamplesViewModel.isUpdating.observe(viewLifecycleOwner, Observer {
            it?.let { isReading ->
                if(!isReading){
                    val id: String? = deviceID ?: nfcTag2ViewModel.nfcTag.value?.stringId
                    //val samples = smartTag.allSampleList.value
                    val samplesAWS = tag2SamplesViewModel.allSampleList.value
                    println(samplesAWS)

                    if(id !=null && samplesAWS!=null){
                        if(samplesAWS.size != 0) {
                            askToSyncData(id, samplesAWS)
                        }
                    }
                }
            }
        })
    }

    private fun <T> getProgressUpdate():Observer<T>{
        return Observer {
            val nReadSample = tag2SamplesViewModel.allSampleList.value?.size ?: 0
            val nTotalSample = tag2SamplesViewModel.numberSample.value ?: 0
            Log.d("progress","$nReadSample / $nTotalSample")
            if(nReadSample>=nTotalSample){
                readProgress.visibility = View.GONE
            }else{
                readProgress.progress = nReadSample
            }
        }
    }


    private fun askToSyncData(deviceId: String, data: List<GenericSample>) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Sync cloud Data")
            .setMessage("Do you want to upload the data on cloud?")
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                syncData(deviceId, data)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun syncData(deviceId: String, data: List<GenericSample>) {
        val loginManager = LoginManager(
            requireActivity().activityResultRegistry,
            requireActivity() as AppCompatActivity,
            requireContext(),
            LoginProviderFactory.LoginProviderType.COGNITO,
            Configuration.getInstance(requireContext(), com.st.login.R.raw.auth_config_cognito)
        )

        loginManager.getAtrAuthData { authData ->
            if (authData != null) {
                val dshSamples = genericSampleToDSHSample(data)
                UploaderGenericDataActivity.startActivityToUploadGenericData(authData, requireContext(), deviceId, dshSamples, Device.Type.NFCTAG2.toString(), "nfc")
            }
        }
    }

    fun genericSampleToDSHSample(data: List<GenericSample>): List<GenericDSHSample> {
        val samples = data.getSensorDataSample()
        val dshSamples: ArrayList<GenericDSHSample> = ArrayList()
        samples.forEach { sample ->
            val strV = String.format("%.2f", sample.value)
            val stringValue = strV.replace(",", ".")
            val v = stringValue.toDouble()
            dshSamples.add(GenericDSHDataSample(sample.id, sample.type, sample.date, v))
        }
        return dshSamples
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).apply {
            registerReceiver(nfcServiceResponse, getReadDataSampleFilter())
        }

    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).apply {
            unregisterReceiver(nfcServiceResponse)
        }
    }

    private class TabViewAdapter(fm: FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        companion object {
            val FRAGMENT_VIEW = arrayOf(
                Tag2SamplesPlotFragment::class.java,
                Tag2SamplesListedFragment::class.java
            )
        }

        override fun getCount(): Int {
            return FRAGMENT_VIEW.size
        }

        override fun getItem(position: Int): Fragment {
            return FRAGMENT_VIEW[position].newInstance()
        }

        override fun getPageTitle(position: Int): CharSequence {
            return if (position==0)
                "Data Plots"
            else
                "Data Listed"
        }
    }
}