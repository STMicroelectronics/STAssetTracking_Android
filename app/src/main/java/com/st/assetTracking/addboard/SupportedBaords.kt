package com.st.assetTracking.addboard

import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class SupportedBaords(
        @StringRes val name:Int,
        @StringRes val specific:Int,
        @StringRes val description:Int,
        @DrawableRes val image:Int,
        val onSelect:(Context)->Unit,
        val moreInfo: Uri? = null
)