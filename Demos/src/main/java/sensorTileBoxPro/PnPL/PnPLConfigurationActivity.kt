package sensorTileBoxPro.PnPL

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.ActivityWithNode
import com.st.demos.R
import kotlinx.coroutines.*
import kotlin.coroutines.resume

class PnPLConfigurationActivity : ActivityWithNode() {

    companion object {

        fun startWithNode(context: Context, node: Node): Intent {
            return getStartIntent(
                context,
                PnPLConfigurationActivity::class.java,
                node,
                true
            )
        }
        val  REGISTER_DIALOG_TAG = PnPLConfigurationActivity::class.java.name+".REGISTER_DIALOG_TAG"
    }

    private val PICKFILE_REQUEST_CODE = 7777
    private lateinit var viewModel: PnPLConfigViewModel

    private val adapterPnPLComponents = PnPLComponentAdapter(
        object : PnPLComponentAdapter.ComponentInteractionCallback {
            override fun onComponentCollapsed(selected: PnPLComponentViewData) {
                viewModel.collapseComponent(selected)
            }

            override fun onComponentExpanded(selected: PnPLComponentViewData) {
                viewModel.expandComponent(selected)
            }
        },
        contChangedListener = { component, content, value ->
            Log.i("PnPLConfFragment", "component: $component, content: $content,value: $value")
            viewModel.sendPnPLSetProperty(component.comp_name, content.cont_name, value)
        },
        subContChangedListener = { component, content, subContent, value ->
            Log.i("PnPLConfFragment", "component: $component, content: $content,value: $value")
            viewModel.sendPnPLSetProperty(
                component.comp_name,
                content.cont_name,
                subContent.cont_name,
                value
            )
        },
        commandSentListener = { component, content, command_list ->
            Log.i(
                "PnPLConfFragment",
                "component: $component, content: $content,value: $command_list"
            )
            val fieldMap = emptyMap<String, Any>().toMutableMap()
            if (content.sub_cont_list != null) {
                var validCommand=true
                for (c in content.sub_cont_list!!) {
                    if(c.cont_name!=null) {
                        if (c.cont_enum_pos != null) {
                            fieldMap[c.cont_name] = c.cont_enum_pos!!
                        } else {
                            if (c.cont_info != null) {
                                fieldMap[c.cont_name] = c.cont_info!!
                            } else {
                                validCommand = false
                                    Toast.makeText(
                                        applicationContext,
                                        "Missing value",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            }
                        }
                    } else {
                        validCommand=false
                        Toast.makeText(applicationContext, "Missing value", Toast.LENGTH_SHORT).show()
                    }
                }
                if(validCommand) {
                    if (content.request_name != null) {
                        viewModel.sendPnPLCommandCmd(
                            component.comp_name,
                            content.cont_name,
                            content.request_name!!,
                            fieldMap
                        )
                    } else {
                        viewModel.sendPnPLCommandCmd(
                            component.comp_name,
                            content.cont_name,
                            fieldMap
                        )
                    }
                }
            } else {
                viewModel.sendPnPLCommandCmd(component.comp_name, content.cont_name)
            }
        },
        loadfileListener = { component, content ->
            Log.i("PnPLConfFragment", "component: $component, content: $content")
            viewModel.setComponentToLoadFile(
                component.comp_name,
                content.cont_name,
                content.request_name!!
            )
            requestOpenFile()
        })

    private lateinit var recyclerViewPnPLComponents: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val rootView = inflater.inflate(R.layout.fragment_generic_pnpl, container, false)
        setContentView(R.layout.activity_generic_pnpl)

        title = "PnPL"
        //Get the ViewModel
        viewModel = ViewModelProvider(this@PnPLConfigurationActivity)[PnPLConfigViewModel::class.java]
        viewModel.context = applicationContext

        node?.let { n -> enableNeededNotification(n) }

        CoroutineScope(Dispatchers.Main).launch {
            node?.waitStatus(Node.State.Connected)
            if(node?.isConnected == true) {
                node?.let { n -> enableNeededNotification(n) }
            }
        }


        //Set the Recycler View
        recyclerViewPnPLComponents = findViewById(R.id.PnPLComponentsRecycler)
        recyclerViewPnPLComponents.adapter = adapterPnPLComponents

    }

    override fun onResume() {
        super.onResume()

        viewModel.compList.observe(this, Observer {
            adapterPnPLComponents.updatePnPLCompList(it)
        })

        if(node?.dtdlModel!=null) {
            viewModel.parseDeviceModel(node!!.dtdlModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disableNeedNotification(node!!)
    }

    private fun requestOpenFile() {
        val chooserFile = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/*", "text/*"))
            type = "*/*"
        }
        val chooserTitle = "Load a configuration File (UCF)"
        startActivityForResult(
            Intent.createChooser(chooserFile, chooserTitle),
            PICKFILE_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PICKFILE_REQUEST_CODE -> {
                var type = ""
                val fileUri = data?.data?.also { uri ->
                    type = applicationContext?.contentResolver?.getType(uri).toString()
                }
                if (resultCode == Activity.RESULT_OK) {
                    if (type != "application/octet-stream") { //NOTE filter other known MIME types (it is not exhaustive)
                        displayErrorMessage("Invalid File")
                    } else {
                        viewModel.sendFileToSelectedComponent(
                            fileUri,
                            applicationContext.contentResolver
                        )
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun displayErrorMessage(error: String) {
        Toast.makeText(applicationContext, error, Toast.LENGTH_LONG).show()
    }

    private fun enableNeededNotification(node: Node) {
        viewModel.enableNotificationFromNode(node)
        viewModel.sendPnPLGetDeviceStatus()
    }

    private fun disableNeedNotification(node: Node) {
        viewModel.disableNotificationFromNode(node)
    }
}

private class WaitStateListener(private val finalState: Node.State,
                                private val continuation: CancellableContinuation<Unit>
) :
    Node.NodeStateListener {
    override fun onStateChange(node: Node, newState: Node.State, prevState: Node.State) {
        if (newState == finalState) {
            node.removeNodeStateListener(this)
            continuation.resume(Unit)
        }
    }

}


internal suspend fun Node.waitStatus(status: Node.State) {
    if (state == status)
        return
    //else wait
    suspendCancellableCoroutine<Unit> { cancellableContinuation ->
        val nodeListener = WaitStateListener(status, cancellableContinuation)
        addNodeStateListener(nodeListener)
        cancellableContinuation.invokeOnCancellation {
            removeNodeStateListener(nodeListener)
        }
    }
}