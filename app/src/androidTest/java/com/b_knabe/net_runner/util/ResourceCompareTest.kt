package com.b_knabe.net_runner.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.b_knabe.net_runner.R
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before

import org.junit.Test

class ResourceCompareTest {
   private lateinit var resourceCompare: ResourceCompare

   @Before
   fun setup() {
      resourceCompare = ResourceCompare()
   }

   @After
   fun tearDown() {
      //close resource
   }

   @Test
   fun stringResourceSameAsGivenString_returnsTrue(){
      val context = ApplicationProvider.getApplicationContext<Context>()
      val result = resourceCompare.isEqual(context, R.string.app_name, "NetRunner")
      assertThat(result).isTrue()
   }

   @Test
   fun stringResourceSameAsGivenString_returnsFalse(){
      val context = ApplicationProvider.getApplicationContext<Context>()
      val result = resourceCompare.isEqual(context, R.string.app_name, "Hello")
      assertThat(result).isFalse()
   }
}