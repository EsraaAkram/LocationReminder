package com.udacity.project4.locationreminders.viewModels

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.viewModels.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.M])
class RemindersListViewModelTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var coroutineRule = MainCoroutineRule()


    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setUp() {
        //STOP COIN (SCOPE):
        stopKoin()
        //INIT BEFORE TEST
        fakeDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeDataSource)

    }


    //THIS TEST WILL PASS BUT WILL CONFIRM THAT THE NULL SAFETY IS WORKING
    @Test
    fun testShouldReturn_in_loadReminders() = coroutineRule.runBlockingTest {
        fakeDataSource.setReturnError(true)

        //LOAD TEST METHOD
        remindersListViewModel.loadReminders()

        //TEST:
        MatcherAssert.assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(),
            //Matchers.`is`("Reminder not found!")
            Matchers.`is`("Test exception!")
        )
    }



    @Test
    fun test_loadReminders() = coroutineRule.runBlockingTest {
        //PAUSE
        coroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        MatcherAssert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), Matchers.`is`(true))
        coroutineRule.resumeDispatcher()

        //RESUMED
        MatcherAssert.assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), Matchers.`is`(false))
    }
}