package com.st.assetTracking.addboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.st.assetTracking.R
import com.st.ui.databinding.ItemSupportedBoardsAtrDetailsBinding
import com.st.ui.databinding.ItemSupportedBoardsBinding

internal class SupportedBoardsAdapter(private val packages:Array<SupportedBaords>,
                                      private val listener:PackageListener?) :
        RecyclerView.Adapter<SupportedBoardsAdapter.SupportedPackageViewHolder>(){

    private lateinit var context: Context

    internal interface PackageListener{
        fun onPackageSelected(item:SupportedBaords)
        fun onMoreInfoSelected(item:SupportedBaords)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupportedPackageViewHolder {
        val mainBinding = ItemSupportedBoardsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val specializedBinding = ItemSupportedBoardsAtrDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        mainBinding.itemSupportedBoardsPlaceholder1.addView(specializedBinding.root.rootView)

        context = parent.context
        return SupportedPackageViewHolder(mainBinding, specializedBinding)
    }

    override fun getItemCount(): Int  = packages.size

    override fun onBindViewHolder(holder: SupportedPackageViewHolder, position: Int) {
        val supportedPackage = packages[position]
        holder.details.setText(supportedPackage.description)

        setBoardImageResized(supportedPackage, holder)

        holder.name.setText(supportedPackage.name)
        holder.specific.setText(supportedPackage.specific)
        holder.itemView.setOnClickListener {
            listener?.onPackageSelected(supportedPackage)
        }
    }

    private fun setBoardImageResized(supportedPackage: SupportedBaords, holder: SupportedPackageViewHolder) {
        Glide
            .with(context)
            .load(supportedPackage.image)
            .fitCenter()
            .into(holder.icon)
    }

    inner class SupportedPackageViewHolder(mainView: ItemSupportedBoardsBinding, specializedView: ItemSupportedBoardsAtrDetailsBinding) : RecyclerView.ViewHolder(mainView.root){
        val icon: ImageView = mainView.boardImage
        val name: TextView = specializedView.boardName
        val specific: TextView = specializedView.boardSpecific
        val details: TextView = specializedView.boardDescription

        init {
            specializedView.boardInfo.setOnClickListener {
                val currentItem = packages[adapterPosition]
                listener?.onMoreInfoSelected(currentItem)
            }

            specializedView.boardAdd.setOnClickListener {
                val currentItem = packages[adapterPosition]
                listener?.onPackageSelected(currentItem)
            }

            //view.setOnClickListener {
                //val currentItem = packages[adapterPosition]
                //listener?.onPackageSelected(currentItem)
            //}

        }

    }
}