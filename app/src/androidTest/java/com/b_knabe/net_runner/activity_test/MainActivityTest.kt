package com.b_knabe.net_runner.activity_test

import android.view.View
import androidx.test.rule.ActivityTestRule
import com.b_knabe.net_runner.MainActivity
import com.b_knabe.net_runner.R
import junit.framework.Assert.assertNotNull
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
    //Kotlin also allows compile properties to fields on the JVM,
    // in which case the annotations and modifiers apply to the generated field.
    // This is done using Kotlin’s @JvmField property annotation.
    //the public property fix is adding annotation @JvmField and make property is default java field
    @Rule
    @JvmField
    var myActivityTestRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)
    private var mainActivity: MainActivity? = null

    @Before
    fun setUp() {
        mainActivity = myActivityTestRule.activity
    }

    @Test
    fun testLaunchMainActivity() {
        //finding view in main activity if it finds, it means activity is started and test is passed
        val viewFragmentContainer: View? = mainActivity?.findViewById(R.id.nav_host_fragment)
        val viewBottomNavigation: View? = mainActivity?.findViewById(R.id.bottom_navigation)
        assertNotNull(viewFragmentContainer)
        assertNotNull(viewBottomNavigation)
    }

    @After
    fun tearDown() {
        mainActivity = null
    }
}