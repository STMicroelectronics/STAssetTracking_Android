package com.st.assetTracking.threshold.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import java.util.*
import com.st.assetTracking.threshold.R

/**
 * Custom view to display the min an max keys for a sensor
 * it has an image called [dataImage] that can be set using the extremeDataImg xml attribute
 * a data unit called [dataUnit] that can be set using the extremeDataUnit xml attribute
 * the format used to display the data can be set using the attribute extremeDataFormat
 * if a sensor doesn't have a min or max key them can be hide with the attribute: extremeHideMinValues or
 * extremeHideMaxValues
 */
class ExtremeDataView : FrameLayout {

    private var mDataFormat: String = "%f"

    private var mDataUnit: String? = ""
    var dataUnit: String?
        get() = mDataUnit
        set(value) {
            mDataUnit = value
            val maxUnit = findViewById<TextView>(R.id.extremeData_maxUnit)
            maxUnit.text = value
            val minUnit = findViewById<TextView>(R.id.extremeData_minUnit)
            minUnit.text = value
        }


    private var mImage: Drawable? = null
    var dataImage: Drawable?
        get() = mImage
        set(value) {
            mImage = value
            val img = findViewById<ImageView>(R.id.singleShot_data_image)
            img.setImageDrawable(mImage)
        }


    var maxValue: CharSequence?
        get() = mMaxValue.text
        set(value) {
            mMaxValue.text = value
        }

    var maxDateValue: CharSequence?
        get() = mMaxDateValue.text
        set(value) {
            mMaxDateValue.text = value
        }

    var minDateValue: CharSequence?
        get() = mMinDateValue.text
        set(value) {
            mMinDateValue.text = value
        }

    var minValue: CharSequence?
        get() = mMinValue.text
        set(value) {
            mMinValue.text = value
        }

    private val mMaxValue by lazy { findViewById<TextView>(R.id.extremeData_maxValue) }
    private val mMaxDateValue by lazy { findViewById<TextView>(R.id.extremeData_maxDateValue) }

    private val mMinValue by lazy { findViewById<TextView>(R.id.extremeData_minValue) }
    private val mMinDateValue by lazy { findViewById<TextView>(R.id.extremeData_minDateValue) }

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs, defStyle)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        inflate(context, R.layout.extremes_view, this)

        val a = context.obtainStyledAttributes(
                attrs, R.styleable.ExtremeDataView, defStyle, 0)

        dataUnit = a.getString(
                R.styleable.ExtremeDataView_extremeDataUnit)

        mDataFormat = a.getString(
                R.styleable.ExtremeDataView_extremeDataFormat) ?: "%f"

        if (a.hasValue(R.styleable.ExtremeDataView_extremeDataImg)) {
            val img = findViewById<ImageView>(R.id.singleShot_data_image)
            img.setImageResource(a.getResourceId(R.styleable.ExtremeDataView_extremeDataImg, 0))
        }

        val hideMin = a.getBoolean(R.styleable.ExtremeDataView_extremeHideMinValues, false)
        hideMinValues(hideMin)
        val hideMax = a.getBoolean(R.styleable.ExtremeDataView_extremeHideMaxValues, false)
        hideMaxValues(hideMax)
        a.recycle()



    }

    private fun hideMinValues(hide: Boolean) {
        val visibility = if (hide) View.INVISIBLE else View.VISIBLE

        mMinValue.visibility = visibility
        mMinDateValue.visibility = visibility
        findViewById<View>(R.id.extremeData_minUnit).visibility = visibility
        findViewById<View>(R.id.extremeData_minValueLabel).visibility = visibility

    }

    private fun hideMaxValues(hide: Boolean) {
        val visibility = if (hide) View.INVISIBLE else View.VISIBLE

        mMaxValue.visibility = visibility
        mMaxDateValue.visibility = visibility
        findViewById<View>(R.id.extremeData_maxUnit).visibility = visibility
        findViewById<View>(R.id.extremeData_maxValueLabel).visibility = visibility
    }

    fun setMax(value: Float, date: String) {
        maxValue = String.format(Locale.getDefault(),mDataFormat,value)
        maxDateValue = date
    }

    fun setMin(value: Float, date: String) {
        minValue=String.format(Locale.getDefault(),mDataFormat,value)
        minDateValue=date
    }

}