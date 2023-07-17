package com.st.smartaglibrary.util

import android.content.Intent

@Suppress("UNCHECKED_CAST")
fun <T> Intent.getTypeSerializableExtra(name:String):T{
    return this.getSerializableExtra(name) as T
}