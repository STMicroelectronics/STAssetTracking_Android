package com.st.assetTracking.addboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.st.assetTracking.R
import com.st.assetTracking.atrBle1.sensorTileBox.provisioning.BleDiscoveryForProvisioning
import com.st.assetTracking.atrBle1.sensorTileBoxPRO.provisioning.BleSTBoxProDiscoveryForProvisioning
import com.st.polaris.ble.provisioning.BlePolarisDiscoveryForProvisioning
import com.st.nfcSmarTag.v1.provisioning.NfcProvisioningDevice
import com.st.nfcSmarTag.v2.provisioning.Nfc2ProvisioningDevice

class AddBoardDialogFragment : AppCompatActivity(), SupportedBoardsAdapter.PackageListener {

    companion object {

        private val SUPPORTED_PACKAGES: Array<SupportedBaords> = arrayOf(
                SupportedBaords(name = R.string.board_polaris_name,
                        specific = R.string.board_polaris_specific,
                        description = R.string.board_polaris_desc,
                        image = R.drawable.real_board_astra,
                        moreInfo = Uri.parse("https://www.st.com/en/evaluation-tools/steval-astra1b.html"),
                        onSelect = Companion::startPolarisActivity),
                SupportedBaords(name = R.string.board_smarTag2_name,
                        specific = R.string.board_smarTag2_specific,
                        description = R.string.board_smarTag2_desc,
                        image = R.drawable.board_smartag2,
                        moreInfo = Uri.parse("https://www.st.com/en/evaluation-tools/steval-smartag2.html"),
                        onSelect = Companion::startSmartTag2Activity),
                SupportedBaords(name = R.string.board_smarTag_name,
                        specific = R.string.board_smarTag_specific,
                        description = R.string.board_smarTag_desc,
                        image = R.drawable.board_smartag1,
                        moreInfo = Uri.parse("https://www.st.com/en/evaluation-tools/steval-smartag1.html"),
                        onSelect = Companion::startSmartTagActivity),
                SupportedBaords(name = R.string.board_sensortileboxpro_name,
                        specific = R.string.board_sensortileboxpro_specific,
                        description = R.string.board_sensortileboxpro_desc,
                        image = R.drawable.real_board_sensortilebox_pro,
                        moreInfo = Uri.parse("https://www.st.com/en/evaluation-tools/steval-mkboxpro.html"),
                        onSelect = Companion::startSensorTileBoxPROActivity),
                SupportedBaords(name = R.string.board_atrble1_name,
                        specific = R.string.board_atrble1_specific,
                        description = R.string.board_atrble1_desc,
                        image = R.drawable.real_board_sensortilebox,
                        moreInfo = Uri.parse("https://www.st.com/en/evaluation-tools/steval-mksbox1v1.html"),
                        onSelect = Companion::startAtrBle1Activity),
                /*SupportedBaords(name = R.string.board_lora_name,
                        specific = R.string.board_lora_specific,
                        description = R.string.board_lora_desc,
                        image = R.drawable.board_loratracker,
                        moreInfo = Uri.parse("https://www.st.com/en/embedded-software/fp-atr-lora1.html"),
                        onSelect = Companion::startLoraAssetTrackingActivity)*/
        )

        private fun startAtrBle1Activity(context: Context) {
            val intent = Intent(context, BleDiscoveryForProvisioning::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        private fun startSensorTileBoxPROActivity(context: Context) {
            val intent = Intent(context, BleSTBoxProDiscoveryForProvisioning::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        private fun startSmartTagActivity(context: Context) {
            val intent = Intent(context, NfcProvisioningDevice::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        private fun startSmartTag2Activity(context: Context) {
            val intent = Intent(context, Nfc2ProvisioningDevice::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        /*private fun startLoraAssetTrackingActivity(context: Context) {
            val intent = Intent(context, AtrLoraMainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }*/

        private fun startPolarisActivity(context: Context) {
            val intent = Intent(context, BlePolarisDiscoveryForProvisioning::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_new_device)

        // Hide title bar
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {}

        val adapter = SupportedBoardsAdapter(SUPPORTED_PACKAGES, this)
        findViewById<RecyclerView>(R.id.main_package_list).adapter = adapter
        findViewById<ImageButton>(R.id.back_arrow_button).setOnClickListener{
            onBackPressed()
        }
    }

    override fun onPackageSelected(item: SupportedBaords) {
        item.onSelect(applicationContext)
    }

    override fun onMoreInfoSelected(item: SupportedBaords) {
        if (item.moreInfo == null)
            return
        val intent = Intent(Intent.ACTION_VIEW, item.moreInfo)
        startActivity(intent)
    }
}
