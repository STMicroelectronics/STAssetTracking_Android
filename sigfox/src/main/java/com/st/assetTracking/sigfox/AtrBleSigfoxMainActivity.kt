package com.st.assetTracking.sigfox

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.NodeListActivity
import com.st.assetTracking.sigfox.AtrBleSigfoxDetails

class AtrBleSigfoxMainActivity : NodeListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displayInfoMessage(applicationContext)
    }
    override fun onResume() {
        super.onResume()
        displayInfoMessage(applicationContext)
    }

    override fun onNodeSelected(n: Node) {
        val intent = AtrBleSigfoxDetails.startWithNode(this, n)
        startActivity(intent)
    }

    override fun onNodeAdded(mItem: Node?, mNodeAddedIcon: ImageView?) {
        TODO("Not yet implemented")
    }

    override fun displayNode(n: Node): Boolean {
        return n.type == Node.Type.NUCLEO
    }

    private fun displayInfoMessage(context: Context){
        Toast.makeText(context, "Please select your SIGFOX board.", Toast.LENGTH_SHORT).show()
    }

}