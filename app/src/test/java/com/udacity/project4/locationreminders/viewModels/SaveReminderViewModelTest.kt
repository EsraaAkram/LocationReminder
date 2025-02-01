package com.udacity.project4.locationreminders.viewModels

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.models.ReminderDataItem
import com.udacity.project4.viewModels.SaveReminderViewModel
import com.udacity.project4.R

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.M])
class SaveReminderViewModelTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    val coroutineRule = MainCoroutineRule()
    private lateinit var saveReminderVModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setUp() {
        //BEFORE TEST SET UP MY FAKE DATA:
        stopKoin()
        fakeDataSource = FakeDataSource()
        saveReminderVModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),
            fakeDataSource)

    }


    @Test
    fun test_saveReminder_loading_should_be_true() = coroutineRule.runBlockingTest {
        coroutineRule.pauseDispatcher()
        val reminderDataItem = ReminderDataItem(
            "Test1",
            "desvc1",
            "lo",
            3.0,
            0.2
        )
        saveReminderVModel.saveReminder(reminderDataItem)
        MatcherAssert.assertThat(saveReminderVModel.showLoading.getOrAwaitValue(), `is`(true))
        coroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(saveReminderVModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun save_remindeer_should_be_false() = coroutineRule.runBlockingTest {

        val reminderDataItem = ReminderDataItem(

            null,
            null,
            "lo",
            3.0,
            0.2
        )
        val validEnteredData = saveReminderVModel.validateEnteredData(reminderDataItem)
        MatcherAssert.assertThat(validEnteredData, `is`(false))

        //TEST SNACK BAR:
        MatcherAssert.assertThat(saveReminderVModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))




    }
}