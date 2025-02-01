package com.udacity.project4.views.frags

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.data.ReminderDataSource
import com.udacity.project4.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.udacity.project4.R
import com.udacity.project4.data.local.LocalDB
import com.udacity.project4.data.local.RemindersLocalRepository
import com.udacity.project4.viewModels.RemindersListViewModel
import com.udacity.project4.viewModels.SaveReminderViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class RemindersListFragTest : AutoCloseKoinTest(){

    private lateinit var app: Application

    private lateinit var reminderDataSource: ReminderDataSource




    @get:Rule
    val instantExecutor = InstantTaskExecutorRule()

    @Before
    fun init() {
        //STOP CURRENT KOIN
        stopKoin()
        app = ApplicationProvider.getApplicationContext()
        val currentModule = module {
            viewModel {
                RemindersListViewModel(
                    app,
                    get() as ReminderDataSource
                )
            }
            single { SaveReminderViewModel(app, get() as ReminderDataSource) }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(app) }
        }

        //START NEW KOIN
        startKoin {
            modules(listOf(currentModule))
        }

        reminderDataSource = get()

        //DELETE ALL
        runBlocking {
            reminderDataSource.deleteAllReminders()
        }
    }



    @Test
    fun test_fab_add () {

        val fragmentScenario = launchFragmentInContainer<RemindersListFrag>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(ViewActions.click())

        //TEST NAVIGATION:
        Mockito.verify(navController).navigate(
            RemindersListFragDirections.toSaveReminder()
        )
    }

    @Test
    fun test_add_many_reminders_and_navigate(): Unit = runBlocking {//: Unit =
        //MUST RETURN UNIT OTHER WITH FAIL!!

        val reminder = ReminderDTO("title",
            "desc", "lo", 0.2, 7.1)
        val reminder2 = ReminderDTO("title2",
            "desc2", "lo2", 0.2, 7.1)

        reminderDataSource.saveReminder(reminder)
        reminderDataSource.saveReminder(reminder2)

        launchFragmentInContainer<RemindersListFrag>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.reminderssRecyclerView)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText(reminder2.title))
                )
            )
    }


    @Test
    fun test_displayed_elements_on_ui(): Unit = runBlocking {
        val reminder = ReminderDTO("title",
            "desc", "lo", 0.2, 7.1)
        reminderDataSource.saveReminder(reminder)

        reminderDataSource.deleteAllReminders()

        launchFragmentInContainer<RemindersListFrag>(Bundle(), R.style.AppTheme)

        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }






}