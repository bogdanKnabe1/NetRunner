package com.b_knabe.net_runner.fragment_test

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withHint
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
class ReplaceRequestFragmentTest {

    //alternative rule set up
    @get:Rule
    val activityTestRule = ActivityTestRule(MainActivity::class.java)


    @Before
    fun setUp() {
        //set fragment in fragment container in activity
        activityTestRule.activity.initFragment(1)
    }

    @Test
    fun shouldDisplayNoDataHintForAEmptyScreen() {
        Espresso.onView(ViewMatchers.withId(R.id.et_url))
                .check(matches(withHint(R.string.edit_url)))
    }

    @After
    fun tearDown() {
    }
}