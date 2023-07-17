package com.st.assetTracking.util

import androidx.annotation.StringRes
import androidx.test.espresso.ViewAction
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.st.assetTracking.SupportedPackageAdapter

internal fun onRecycleItemWithText(@StringRes str:Int, viewAction: ViewAction):ViewAction{
    return RecyclerViewActions.actionOnItem<SupportedPackageAdapter.SupportedPackageViewHolder>(
            ViewMatchers.hasDescendant(
                    ViewMatchers.withText(str)
            ),
            viewAction)
}