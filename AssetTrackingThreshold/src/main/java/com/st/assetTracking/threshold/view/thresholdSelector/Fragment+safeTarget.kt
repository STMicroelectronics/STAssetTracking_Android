package com.st.assetTracking.threshold.view.thresholdSelector

import android.app.Activity
import androidx.fragment.app.Fragment

/*
 is not possible to set a target fragment with different fragment manager.
 and this is needed if you are instantiating a fragment from another fragment.
 in that case we can use the parent fragment as the target fragment.
 and set the target passing null as target fragment to avoid exception.
 this method will return the parent fragment if the target fragment is null
 */
internal val Fragment.safeTargetFragment: Fragment?
    get() = targetFragment ?: parentFragment

internal fun Fragment.cancelRequest() {
    safeTargetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, null)
}