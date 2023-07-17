package com.st.assetTracking.sigfox.addThreshold

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.material.textfield.TextInputLayout
import com.st.assetTracking.sigfox.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers

private fun onRecycleItemWithText(@StringRes str:Int, viewAction: ViewAction): ViewAction {
    return RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
            ViewMatchers.hasDescendant(ViewMatchers.withText(str)),
            viewAction)
}

internal fun showThDialogForSensor(@StringRes sensorName:Int){
    Espresso.onView(ViewMatchers.withId(R.id.settings_add_threshold)).perform(ViewActions.click())
    Espresso.onView(ViewMatchers.withId(R.id.sensorTypeSelector_list)).perform(
            onRecycleItemWithText(sensorName, ViewActions.click())
    )
}

internal fun insertThreshold(thValue: String, @IdRes onView:Int = R.id.addThreshold_threshold_value)
        = Espresso.onView(ViewMatchers.withId(onView)).perform(ViewActions.typeText(thValue))
internal fun addThreshold()
        = Espresso.onView(ViewMatchers.withId(R.id.addThreshold_addButton)).perform(ViewActions.click())

internal fun checkThreshold(@StringRes title:Int, description: String){
    Espresso.onView(ViewMatchers.withId(R.id.settings_thresholdList))
            .check(ViewAssertions.matches(
                    atPosition(0,
                            Matchers.allOf(
                                    ViewMatchers.hasDescendant(ViewMatchers.withText(title)),
                                    ViewMatchers.hasDescendant(ViewMatchers.withText(description))
                            )//allOf
                    )//atPosition
            )//matches
            )//check
}

internal fun checkSensorLessThanInsertion(@StringRes sensorName:Int, thValue:String, expectedDescription:String){
    showThDialogForSensor(sensorName)

    insertThreshold(thValue)
    addThreshold()

    checkThreshold(sensorName,expectedDescription)
}

internal fun checkSensorBiggerInsertion(@StringRes sensorName:Int, thValue:String, expectedDescription:String){
    showThDialogForSensor(sensorName)

    insertThreshold(thValue)
    Espresso.onView(ViewMatchers.withId(R.id.addThreshold_biggerThanButton)).perform(ViewActions.click())
    addThreshold()

    checkThreshold(sensorName,expectedDescription)
}



internal fun checkErrorMessageContains(str:String, @IdRes onView:  Int = R.id.addThreshold_threshold_layout){
    Espresso.onView(ViewMatchers.withId(onView))
            .check(ViewAssertions.matches(
                    showErrorMessage(
                            Matchers.containsString(str)
                    )//showErrorMessage
            ))//matches
}


internal fun checkOutOfRangeError(@StringRes sensorName:Int, thValue:String, expectedErrorRange:String){
    showThDialogForSensor(sensorName)

    insertThreshold(thValue)

    checkErrorMessageContains(expectedErrorRange)
}

private fun showErrorMessage(matcher: Matcher<String>) : Matcher<View> {
    return object  : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java){
        override fun describeTo(description: Description) {
            description.appendText("error message match:")
            matcher.describeTo(description)
        }

        override fun matchesSafely(item: TextInputLayout): Boolean {
            val errorMessage = item.error ?: return false
            return matcher.matches(errorMessage)
        }

    }
}


private fun atPosition(position: Int, itemMatcher: Matcher<View>): Matcher<View> {
    return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has item at position $position: ")
            itemMatcher.describeTo(description)
        }

        override fun matchesSafely(view: RecyclerView): Boolean {
            // has no item on such position
            val viewHolder = view.findViewHolderForAdapterPosition(position)  ?:   return false
            return itemMatcher.matches(viewHolder.itemView)
        }
    }
}