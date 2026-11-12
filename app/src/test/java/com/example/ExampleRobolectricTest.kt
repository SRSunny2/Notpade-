package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MyPocket Diary", appName)
  }

  @Test
  fun `test Taka formatting`() {
    val formatted = Formatters.formatTaka(1250.50)
    assertEquals("৳1,250.50", formatted)
  }

  @Test
  fun `test word count`() {
    val count = Formatters.countWords("Meeting with client in Dhaka")
    assertEquals(5, count)
  }
}
