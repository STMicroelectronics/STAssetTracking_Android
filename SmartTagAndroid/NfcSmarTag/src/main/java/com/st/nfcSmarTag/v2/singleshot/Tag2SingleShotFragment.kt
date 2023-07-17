package com.st.nfcSmarTag.v2.singleshot

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.st.nfcSmarTag.R
import com.st.nfcSmarTag.SmarTagViewModel
import com.st.nfcSmarTag.v1.tagSingleShot.settings.SingleShotPreferenceActivity
import com.st.nfcSmarTag.v1.tagSingleShot.settings.SingleShotSettings
import com.st.nfcSmarTag.v2.extremes.Tag2ExtremesAdapter
import com.st.smartaglibrary.util.getTypeSerializableExtra
import com.st.smartaglibrary.v2.SmarTag2Service
import com.st.smartaglibrary.v2.catalog.NFCTag2CurrentFw
import com.st.smartaglibrary.v2.model.SmarTag2Configuration

class Tag2SingleShotFragment : Fragment() {

    private val nfcServiceResponse = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            when (intent?.action) {
                SmarTag2Service.READ_TAG_SAMPLE_DATA_ACTION -> {
                    val data: SmarTag2Configuration = intent.getTypeSerializableExtra(
                        SmarTag2Service.EXTRA_TAG_SAMPLE_DATA)
                    smartTag.newTag2Configuration(data)
                    Log.d("SINGLE SHOT DATA", data.virtualSensorsMinMax.toString())
                }
                SmarTag2Service.READ_TAG_WAIT_ANSWER_ACTION ->{
                    val timeout = intent.getLongExtra(SmarTag2Service.EXTRA_WAIT_ANSWER_TIMEOUT_MS_DATA,0)
                    smartTag.startWaitingAnswer(timeout)
                }
                SmarTag2Service.SINGLE_SHOT_DATA_NOT_READY_ACTION -> {
                    smartTag.readFail()
                }
                SmarTag2Service.READ_TAG_ERROR_ACTION -> {
                    val msg = intent.getStringExtra(SmarTag2Service.EXTRA_ERROR_STR)
                    if (msg != null) {
                        nfcTagHolder.nfcTagError(msg)
                    }
                }
            }
        }
    }

    private lateinit var smartTag: Tag2SingleShotViewModel
    private lateinit var nfcTagHolder: SmarTagViewModel
    private lateinit var timeoutProgress: ProgressBar
    private lateinit var waitingTagPb: ProgressBar
    private lateinit var errorMessageView: TextView
    private lateinit var waitReadTv : TextView
    private lateinit var singleShotFrameLayout: FrameLayout

    private lateinit var tag2SingleShotInfoCV: CardView
    private lateinit var tag2SingleShotDataCV: CardView
    private lateinit var tag2ExtremesRecyclerView: RecyclerView

    private lateinit var singleShotSettings: SingleShotSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_tag2_single_shot, container, false)
        timeoutProgress = rootView.findViewById(R.id.tag2_singleShot_timeoutProgress)
        waitingTagPb = rootView.findViewById(R.id.pb_waiting_tag2)
        errorMessageView = rootView.findViewById(R.id.tag2_singleShot_readFailMessageView)
        waitReadTv = rootView.findViewById(R.id.tag2_wait_read_tv)
        singleShotFrameLayout = rootView.findViewById(R.id.tag2_singleshot_frame_layout)

        tag2SingleShotInfoCV = rootView.findViewById(R.id.tag2_single_shot_info_cv)
        tag2SingleShotDataCV = rootView.findViewById(R.id.tag2_single_shot_data_cv)
        tag2ExtremesRecyclerView = rootView.findViewById(R.id.tag2_single_shot_extremes_rv)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        singleShotSettings = SingleShotSettings(requireContext())
        nfcTagHolder = SmarTagViewModel.create(requireActivity())
        smartTag = Tag2SingleShotViewModel()
        initializeSmartTagObserver()
        initializeNfcTagObserver()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_single_shot,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.singleShot_menu_settings -> {
                showSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSettings() {
        startActivity(Intent(requireContext(), SingleShotPreferenceActivity::class.java))
    }

    private fun initializeSmartTagObserver() {
        smartTag.tag2Configuration.observe(viewLifecycleOwner, Observer {
            //updateDataSample(it)
            if(it != null){
                tag2SingleShotInfoCV.visibility = View.GONE
                tag2SingleShotDataCV.visibility = View.VISIBLE
                tag2ExtremesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                tag2ExtremesRecyclerView.adapter =
                    Tag2ExtremesAdapter(NFCTag2CurrentFw.getCurrentFw(), it.virtualSensorsMinMax, requireContext())
            }
        })
        smartTag.waitingAnswer.observe(viewLifecycleOwner, Observer {
            showWaitingView(it)
        })
        smartTag.singleShotReadFail.observe(viewLifecycleOwner, Observer {
            updateErrorMessageView(it)
        })
    }

    private fun updateErrorMessageView(readFail: Boolean?){
        errorMessageView.visibility = if(readFail == true) View.VISIBLE else View.GONE
    }

    private fun showWaitingView(timeout: Long?) {
        if(timeout==null)
            return
        waitingTagPb.visibility = View.GONE
        timeoutProgress.visibility = View.VISIBLE
        waitReadTv.text = "Reading data..."
        val animateProgress = ObjectAnimator.ofInt(timeoutProgress,"progress",0,100)
        animateProgress.duration=timeout
        //showWaitingView()
        animateProgress.start()
    }

    private fun initializeNfcTagObserver() {
        nfcTagHolder.nfcTag.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                SmarTag2Service.startSingleShotRead(requireContext(), it,
                    singleShotSettings.readingTimeOutSec)
            }else{
                //updateDataSample(null)
            }
        })
    }


    private fun Float?.valueOrNan() : Float = this ?: Float.NaN

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(nfcServiceResponse, SmarTag2Service.getReadSingleShotFilter())
    }


    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(nfcServiceResponse)
    }

}