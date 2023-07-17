package com.st.assetTracking.management.deviceList

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.st.assetTracking.R
import com.st.assetTracking.dashboard.persistance.DeviceListRepository
import com.st.assetTracking.dashboard.util.LoadingView
import com.st.assetTracking.management.AssetTrackingNavigationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


internal class DeviceListFragment(private val deviceListRepository: DeviceListRepository) : Fragment(), DeviceRecyclerAdapter.DeleteListener {

    private lateinit var rView: View
    private val mNavigationViewModel by activityViewModels<AssetTrackingNavigationViewModel>()

    private val mDeviceListViewModel by viewModels<DeviceListViewModel> {
        DeviceListViewModel.Factory(deviceListRepository)
    }

    private val deviceListRepo = deviceListRepository
    private lateinit var mSwipeLayout : SwipeRefreshLayout

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: DeviceRecyclerAdapter
    private lateinit var mLoadingView: LoadingView
    private lateinit var mEmptyView: TextView

    private lateinit var mRefreshButton: ImageButton
    private lateinit var mPbRefresh: ProgressBar

    private val strOffline: String = "You are offline. Please check your connectivity"
    private var networkConnection: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_device_list, container, false)

        mLoadingView = rootView.findViewById(R.id.deviceList_loadingView)
        mLoadingView.loadingText = getString(R.string.deviceList_loadingText)
        mRecyclerView = rootView.findViewById(R.id.deviceList_recyclerView)
        //mRecyclerView.addItemDecoration(DividerItemDecoration(this.requireContext(), DividerItemDecoration.VERTICAL))

        mEmptyView = rootView.findViewById(R.id.deviceList_emptyText)

        mRefreshButton = rootView.findViewById(R.id.iv_refresh)
        mPbRefresh = rootView.findViewById(R.id.pb_refresh)

        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.let {
            try{
                it.registerDefaultNetworkCallback(
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        //take action when network connection is gained
                        networkConnection = true
                    }

                    override fun onLost(network: Network) {
                        //take action when network connection is lost
                        networkConnection = false
                    }
                })
            }catch (e: NoSuchMethodError){ networkConnection = true }
        }

        //manageFabAddButtonVisibility()

        rView = rootView
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mRefreshButton. setOnClickListener{
            Log.i("REFRESH TAG", "onRefresh called from SwipeRefreshLayout")
            CoroutineScope(Dispatchers.Main).launch {
                updateApplicationAndRemoteDB()
            }
        }
        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        mSwipeLayout = view.findViewById(R.id.swipe_refresh)

        mSwipeLayout.setOnRefreshListener {
            Log.i("REFRESH TAG", "onRefresh called from SwipeRefreshLayout")
            CoroutineScope(Dispatchers.Main).launch {
                updateApplicationAndRemoteDB()
            }
        }
        //set refreshing color
        mSwipeLayout.setProgressBackgroundColorSchemeColor(resources.getColor(com.st.BlueSTSDK.gui.R.color.swipeColor_background))
        mSwipeLayout.setColorSchemeResources(R.color.swipeColor_1, R.color.swipeColor_2,
                R.color.swipeColor_3, R.color.swipeColor_4)

        mSwipeLayout.setSize(SwipeRefreshLayout.DEFAULT)
    }

    /**
     * When scroll down fab disappeard, when scroll is finished, fab appear
    private fun manageFabAddButtonVisibility(){
        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.addNewDevice)

        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && fab.isShown) fab.hide()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) fab.show()
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }*/

    private fun updateApplicationAndRemoteDB() {
        CoroutineScope(Dispatchers.Main).launch {
            mDeviceListViewModel.reloadDB(deviceListRepository)

            mDeviceListViewModel.deviceListStatus.observe(viewLifecycleOwner, Observer { status ->
               manageDeviceListStatus(status)
            })
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView().rootView,
                message,
                Snackbar.LENGTH_SHORT)
                .show()
    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.Main).launch {
            updateApplicationAndRemoteDB()
        }
    }

    private fun manageDeviceListStatus(status: DeviceListRepository.DeviceListLoading) {
        when (status) {
            is DeviceListRepository.DeviceListLoading.Loading -> {
                mLoadingView.visibility = View.VISIBLE
                mPbRefresh.visibility = View.VISIBLE
                mRefreshButton.visibility = View.GONE
            }
            is DeviceListRepository.DeviceListLoading.UnknownError -> {
                mLoadingView.visibility = View.GONE
                mSwipeLayout.isRefreshing = false
                mPbRefresh.visibility = View.GONE
                mRefreshButton.visibility = View.VISIBLE
                mRecyclerView.visibility = View.GONE
                mEmptyView.setText(R.string.deviceList_errorRetrievingDeviceList)
                mEmptyView.visibility = View.VISIBLE
            }
            is DeviceListRepository.DeviceListLoading.IOError -> {
                mLoadingView.visibility = View.GONE
                mSwipeLayout.isRefreshing = false
                mPbRefresh.visibility = View.GONE
                mRefreshButton.visibility = View.VISIBLE
                Toast.makeText(context, "A problem was occurred. Please, wait few seconds and refresh device list.", Toast.LENGTH_SHORT).show()

            }
            is DeviceListRepository.DeviceListLoading.Load -> {
                mLoadingView.visibility = View.GONE
                mSwipeLayout.isRefreshing = false
                mPbRefresh.visibility = View.GONE
                mRefreshButton.visibility = View.VISIBLE
                if (status.devices.isEmpty()) {
                    mEmptyView.visibility = View.VISIBLE
                    mRecyclerView.visibility = View.GONE
                } else {
                    mEmptyView.visibility = View.GONE
                    mRecyclerView.visibility = View.VISIBLE

                    mAdapter = DeviceRecyclerAdapter(this, mNavigationViewModel, this, mRecyclerView, status.devices) { device ->
                        if (networkConnection) {
                            //mNavigationViewModel.showDeviceDetail(requireContext(), device.id)

                        } else {
                            showSnackbar(strOffline)
                        }
                    }
                    mRecyclerView.adapter = mAdapter
                }
            }
        }
    }

    override fun onItemToDeleteSelected(item: String) {
        CoroutineScope(Dispatchers.Main).launch {
            if(networkConnection) {
                if (mDeviceListViewModel.deleteItem(deviceListRepo, item)) {
                    Toast.makeText(requireContext(), "$item deleted.", Toast.LENGTH_SHORT).show()
                    CoroutineScope(Dispatchers.Main).launch {
                        updateApplicationAndRemoteDB()
                    }
                } else {
                    Toast.makeText(requireContext(), "A problem was occourred.", Toast.LENGTH_LONG).show()
                }
            } else {
                showSnackbar("Offline. Check your Internet connection.")
            }
        }
    }

}
