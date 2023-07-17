package com.st.assetTracking

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes


data class SupportedPackage(
        @StringRes val name:Int,
        @StringRes val description:Int,
        @DrawableRes val image:Int,
        val onSelect:(MainAssetTracking, Context)->Unit,
        val moreInfo: Uri? = null
)