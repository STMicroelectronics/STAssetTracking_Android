package com.st.assetTracking.management.deviceList

import android.app.AlertDialog
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.st.assetTracking.R
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.management.AssetTrackingNavigationViewModel
import com.st.assetTracking.management.deviceInfo.DeviceInfo
import com.st.ui.databinding.ItemNodeListAtrBinding
import com.st.ui.databinding.NodeListItemBinding

internal class DeviceRecyclerAdapter(private val f: Fragment, private val mNavigationViewModel: AssetTrackingNavigationViewModel, private val deleteListener: DeleteListener, private val mRecyclerView: RecyclerView, private val devices: List<Device>, private val listener: (Device) -> Unit)
    : RecyclerView.Adapter<DeviceRecyclerAdapter.DeviceHolder>(), View.OnLongClickListener {

    override fun getItemCount() = devices.size

    override fun onBindViewHolder(holder: DeviceHolder, position: Int) = holder.bind(f, mNavigationViewModel, devices, devices[position], deleteListener, listener)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        val binding = NodeListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val binding2 = ItemNodeListAtrBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        binding.placeholderForCustomView.addView(binding2.root.rootView)

        return DeviceHolder(binding, binding2)
    }

    internal interface DeleteListener{
        fun onItemToDeleteSelected(item: String)
    }

    class DeviceHolder(private val binding: NodeListItemBinding, private val binding2: ItemNodeListAtrBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(f: Fragment, mNavigationViewModel: AssetTrackingNavigationViewModel, devices: List<Device>, device: Device, deleteListener: DeleteListener, listener: (Device) -> Unit) =
            with(binding) {

                /* OnLongClickListener -> Delete Device */
                binding.nodeCardView.setOnLongClickListener {
                    val builder = AlertDialog.Builder(itemView.context)
                    builder.setTitle("Remove Item")
                    builder.setMessage("Are you sure you want to delete the device?")

                    builder.setPositiveButton("Yes") { _, _ ->
                        deleteListener.onItemToDeleteSelected(binding.nodeId.text.toString())
                    }
                    builder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    val dialog: AlertDialog = builder.create()
                    dialog.show()

                    true
                }

                /* OnClickListener -> Open Device Info */
                binding.nodeCardView.setOnClickListener {
                    val intent = DeviceInfo.startWithDevice(context = f.requireContext(), device, mNavigationViewModel)
                    f.requireContext().startActivity(intent)
                }

                binding.nodeId.text = device.id

                binding.nodeName.text = device.name
                binding.nodeName.setTypeface(null, Typeface.BOLD)

                /* Set Board Image */
                setBoardImage(f, this, device.type)

                device.type.imageRes?.let { image ->
                    ivConnectivity.setImageResource(image)
                }

                binding2.tvLastActivity.text = device.lastActivity.toString()
                setCircleColor(f, device.lastActivity.toString(), )
            }

        private fun setCircleColor(f: Fragment, deviceLastActivity: String){
            when {
                deviceLastActivity.contains("just now") -> {
                    binding2.ivLastActivity.setColorFilter(ContextCompat.getColor(f.requireContext(),
                            R.color.dotGreen), android.graphics.PorterDuff.Mode.MULTIPLY)
                }
                deviceLastActivity.contains("minute") -> {
                    binding2.ivLastActivity.setColorFilter(ContextCompat.getColor(f.requireContext(),
                            R.color.dotYellow), android.graphics.PorterDuff.Mode.MULTIPLY)
                }
                deviceLastActivity.contains("hour") -> {
                    binding2.ivLastActivity.setColorFilter(ContextCompat.getColor(f.requireContext(),
                            R.color.dotYellow), android.graphics.PorterDuff.Mode.MULTIPLY)
                }
                deviceLastActivity.contains("day") -> {
                    binding2.ivLastActivity.setColorFilter(ContextCompat.getColor(f.requireContext(),
                            R.color.dotAmber), android.graphics.PorterDuff.Mode.MULTIPLY)
                }
                deviceLastActivity.contains("month") -> {
                    binding2.ivLastActivity.setColorFilter(ContextCompat.getColor(f.requireContext(),
                            R.color.dotOrange), android.graphics.PorterDuff.Mode.MULTIPLY)
                }
                deviceLastActivity.contains("year") -> {
                    binding2.ivLastActivity.setColorFilter(ContextCompat.getColor(f.requireContext(),
                            R.color.dotRed), android.graphics.PorterDuff.Mode.MULTIPLY)
                }
                else -> {
                    binding2.ivLastActivity.setColorFilter(ContextCompat.getColor(f.requireContext(),
                            R.color.dotRed), android.graphics.PorterDuff.Mode.MULTIPLY)
                }
            }
        }

        private fun setBoardImage(f: Fragment, deviceListItemViewBinding: NodeListItemBinding, type: Device.Type) {
            when(type){
                Device.Type.ASTRA -> {
                    setBoardImageResized(f, deviceListItemViewBinding, R.drawable.real_board_astra)
                }
                Device.Type.NFCTAG2 -> {
                    setBoardImageResized(f, deviceListItemViewBinding, R.drawable.board_smartag2)
                }
                Device.Type.NFCTAG1 -> {
                    setBoardImageResized(f, deviceListItemViewBinding, R.drawable.board_smartag1)
                }
                Device.Type.SENSORTILEBOX -> {
                    setBoardImageResized(f, deviceListItemViewBinding, R.drawable.real_board_sensortilebox)
                }
                Device.Type.SENSORTILEBOXPRO -> {
                    setBoardImageResized(f, deviceListItemViewBinding, R.drawable.real_board_sensortilebox_pro)
                }
                else -> {}
            }
        }

        private fun setBoardImageResized(f: Fragment, deviceListItemViewBinding: NodeListItemBinding, image: Int) {
            Glide
                .with(f.requireContext())
                .load(image)
                .fitCenter()
                .into(deviceListItemViewBinding.nodeBoardIcon)
        }
    }

    override fun onLongClick(v: View?): Boolean {
        TODO("Not yet implemented")
    }

}

private val Device.Type.imageRes: Int?
    get() = when (this) {
        Device.Type.ASTRA -> R.drawable.ic_polaris_star
        Device.Type.NFCTAG2 -> R.drawable.connectivity_nfc
        Device.Type.NFCTAG1 -> R.drawable.connectivity_nfc
        Device.Type.SENSORTILEBOX -> R.drawable.connectivity_ble
        Device.Type.SENSORTILEBOXPRO -> R.drawable.ic_polaris_star
        Device.Type.LORA_TTN -> R.drawable.connectivity_lora
        Device.Type.UNKNOWN -> null
    }