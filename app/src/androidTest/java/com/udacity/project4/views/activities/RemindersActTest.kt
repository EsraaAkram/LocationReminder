package com.udacity.project4.views.activities

import android.app.Activity
import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.udacity.project4.data.ReminderDataSource
import com.udacity.project4.data.local.LocalDB
import com.udacity.project4.data.local.RemindersLocalRepository
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.viewModels.RemindersListViewModel
import com.udacity.project4.viewModels.SaveReminderViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import com.udacity.project4.R
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.project4.views.activities.reminders.RemindersAct
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActTest : AutoCloseKoinTest() {
    // Extended Koin Test - embed autoclose @after method to close Koin after every test
    private var permissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)
    private var locationPermission =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private val dataBindingIdlingResource = DataBindingIdlingResource()//TEST VIEWS


    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application


    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    private fun getAct(activityScenario: ActivityScenario<RemindersAct>): Activity {
        lateinit var activity: Activity
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }


    @Test
    fun testSaveEmptyTitleReminderToShowProgress() {

        val activityScenario = ActivityScenario.launch(RemindersAct::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())

        val barMessage = appContext.getString(R.string.err_enter_title)
        onView(withText(barMessage))
            .check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun testSaveEmptyLocationReminderToShowProgress() {

        val activityScenario = ActivityScenario.launch(RemindersAct::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("testTitle"))

        onView(withId(R.id.reminderTitle)).perform(pressImeActionButton())
        onView(withId(R.id.saveReminder)).perform(click())

        val snackBarMessage = appContext.getString(R.string.err_select_location)
        onView(withText(snackBarMessage))
            .check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun test_save_location_full_reminder_data() {

        val activityScenario = ActivityScenario.launch(RemindersAct::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("testTitle"))
        onView(withId(R.id.reminderTitle)).perform(pressImeActionButton())
        onView(withId(R.id.reminderDescription))
            .perform(typeText("testDesc"))
        onView(withId(R.id.reminderDescription)).perform(pressImeActionButton())
        //TODO:MUST CONFIRM PERMISSIONS MANUALLY AND TURN ON PHONE LOCATION BEFORE THIS BEFORE THIS:
        onView(withId(R.id.selectLocation)).perform(click())

//        onView(withContentDescription("Google Map")).perform(click())//map//BY ID BETTER
        onView(withId(R.id.map)).perform(longClick())
        onView(withId(R.id.saveBtn)).perform(click())

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withText(R.string.reminder_saved)).inRoot(
            withDecorView(not(`is`(getAct(activityScenario).window.decorView)))
        ).check(matches(isDisplayed()))

        activityScenario.close()
    }


    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


}
