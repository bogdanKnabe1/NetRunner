package com.b_knabe.net_runner.fragment_test

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
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
class ReplaceToolFragmentTest {

    //alternative rule set up
    @get:Rule
    val activityTestRule = ActivityTestRule(MainActivity::class.java)


    @Before
    fun setUp() {
        //set fragment in fragment container in activity
        activityTestRule.activity.initFragment(2)
    }

    @Test
    fun shouldDisplayNoDataHintForAEmptyScreen() {
        Espresso.onView(ViewMatchers.withId(R.id.vpn))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(ViewMatchers.withId(R.id.vpn))
                .perform(ViewActions.click())
    }

    @After
    fun tearDown() {
    }
}