package com.st.assetTracking.dashboard.util

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import com.st.assetTracking.dashboard.R

/**
 * dummy view with an indeterminate progress bar and a text
 */
class LoadingView : FrameLayout {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private lateinit var mLoadingText: TextView

    /**
     * change the text displayed by the view
     */
    var loadingText: CharSequence
        get() = mLoadingText.text
        set(value) {
            mLoadingText.text = value
        }

    private fun init() {
        inflate(context, R.layout.view_loading, this)
        mLoadingText = findViewById(R.id.loadingView_progressText)

    }

}
