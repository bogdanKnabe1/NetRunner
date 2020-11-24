package com.b_knabe.net_runner.fragment_test

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.b_knabe.net_runner.MainActivity
import com.b_knabe.net_runner.R
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

//fragment init test
//annotation for test class
@RunWith(AndroidJUnit4::class)
class AddCaptureFragmentTest {

    //alternative rule set up
    @get:Rule
    val activityTestRule = ActivityTestRule(MainActivity::class.java)


    @Before
    fun setUp() {
        //set fragment in fragment container in activity
        activityTestRule.activity.initFragment(0)
    }

    @Test
    fun shouldDisplayNoDataHintForAEmptyScreen() {
        onView(withId(R.id.placeholder_no_data))
                .check(matches(withText(R.string.empty_tip)))
    }

    @Test
    fun shouldChangeButtonAfterClick() {
        onView(withId(R.id.start_capture))
                .check(matches(isDisplayed()))

        onView(withId(R.id.cardViewStartStop))
                .perform(click())

        onView(withId(R.id.cardViewStartStop))
                .check(matches(isDisplayed()))

        onView(withId(R.id.placeholder_no_data))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @After
    fun tearDown() {
    }

    //check color of background view
    /*
    private fun withTextColor(expectedId: Int): Matcher<View?>? {
        return object : BoundedMatcher<View?, TextView>(TextView::class.java) {
            override fun matchesSafely(textView: TextView): Boolean {
                val colorId = ContextCompat.getColor(textView.context, expectedId)
                return textView.currentTextColor == colorId
            }

            override fun describeTo(description: Description) {
                description.appendText("with text color: ")
                description.appendValue(expectedId)
            }
        }
    }
*/
}