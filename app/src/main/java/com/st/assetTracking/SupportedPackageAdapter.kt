package com.st.assetTracking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

internal class SupportedPackageAdapter(private val packages:Array<SupportedPackage>,
                                       private val listener:PackageListener?) :
        RecyclerView.Adapter<SupportedPackageAdapter.SupportedPackageViewHolder>(){

    internal interface PackageListener{
        fun onPackageSelected(item:SupportedPackage)
        fun onMoreInfoSelected(item:SupportedPackage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupportedPackageViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.item_support_package,
                parent,false)
        return SupportedPackageViewHolder(rootView)
    }

    override fun getItemCount(): Int  = packages.size

    override fun onBindViewHolder(holder: SupportedPackageViewHolder, position: Int) {
        val supportedPackage = packages[position]
        holder.details.setText(supportedPackage.description)
        holder.icon.setImageResource(supportedPackage.image)
        holder.name.setText(supportedPackage.name)
        holder.itemView.setOnClickListener {
            listener?.onPackageSelected(supportedPackage)
        }
    }

    inner class SupportedPackageViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val icon: ImageView = view.findViewById(R.id.package_image)
        val name: TextView = view.findViewById(R.id.package_name)
        val details: TextView = view.findViewById(R.id.package_description)

        init {
            view.findViewById<View>(R.id.package_info).setOnClickListener {
                val currentItem = packages[adapterPosition]
                listener?.onMoreInfoSelected(currentItem)
            }

            view.setOnClickListener {
                val currentItem = packages[adapterPosition]
                listener?.onPackageSelected(currentItem)
            }

        }

    }
}