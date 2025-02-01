package com.udacity.project4.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.data.ReminderDataSource
import com.udacity.project4.data.dto.ReminderDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.udacity.project4.data.dto.Result
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersLocalRepository: RemindersLocalRepository

    private lateinit var remindersDatabase: RemindersDatabase


    @Before
    fun setupMyTest() {
        // TEST DB:
        remindersDatabase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java).allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(remindersDatabase.reminderDao(), Dispatchers.Main)
    }


    @Test
    fun test_saveReminder_get_by_id_pass() = runBlocking {
        val reminder = ReminderDTO("title",
            "desc", "lo", 0.2, 7.1)
        remindersLocalRepository.saveReminder(reminder)

        val savedReminder = remindersLocalRepository.getReminder(reminder.id) as? Result.Success

        //TEST SUCCESS
        assertThat(savedReminder is Result.Success, `is`(true))

        //TEST ELEMENTS OF SAVED REMINDER
        assertThat(savedReminder?.data?.description, `is`(reminder.description))
        assertThat(savedReminder?.data?.latitude, `is`(reminder.latitude))
        assertThat(savedReminder?.data?.longitude, `is`(reminder.longitude))
        assertThat(savedReminder?.data?.location, `is`(reminder.location))
        assertThat(savedReminder?.data?.title, `is`(reminder.title))

    }


    //TEST SAVE AND DELETE ONE:
    @Test
    fun testSave_reminder_delete_then_get_by_id_result_error_and_pass() = runBlocking {

        val reminder = ReminderDTO("title",
            "desc", "lo", 0.2, 7.1)
        remindersLocalRepository.saveReminder(reminder)

        remindersLocalRepository.deleteAllReminders()

        val repoReminder = remindersLocalRepository.getReminder(reminder.id)

        assertThat(repoReminder is Result.Error, `is`(true))
        repoReminder as Result.Error
        assertThat(repoReminder.message, `is`("Reminder not found!"))//ERROR MSG IN RemindersLocalRepository CLASS
    }



    //TEST SAVE AND DELETE ALL:
    @Test
    fun test_delete_all_reminders()= runBlocking {
        for (i in 0 until 3){

            val reminder = ReminderDTO("title$i"
                ,"desc$i",
                "loc$i",
                i+0.0,
                i+0.0)

            remindersLocalRepository.saveReminder(reminder)
        }

        remindersLocalRepository.deleteAllReminders()

        val remindersList = remindersLocalRepository.getReminders()

        assertThat(remindersList is Result.Success, `is`(true))
        remindersList as Result.Success

        assertThat(remindersList.data, `is` (listOf()))
    }

    @After
    fun cleanUp() {
        remindersDatabase.close()
    }


}