package com.udacity.project4.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.data.dto.ReminderDTO

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    private lateinit var remindersDatabase: RemindersDatabase

    @get:Rule
    var executorRule = InstantTaskExecutorRule()


    @Before
    fun initDatabase() {

        //DATABASE WILL BE KILLED OR REMOVED AFTER TEST DONE
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java)
            .allowMainThreadQueries().build()
    }




    @Test
    fun save_fake_reminder_then_test_get_by_id() = runBlockingTest {

        //SAVE FAKE DATA:
        val reminder = ReminderDTO("title",
            "desc", "lo", 0.2, 7.1)


        remindersDatabase.reminderDao().saveReminder(reminder)

        val reminderById = remindersDatabase.reminderDao().getReminderById(reminder.id)

        //TEST IF THE DATA HERE IN THE CREATED OBJECTED SAME AS THE ONE I SAVED IN DB:
       assertThat(reminderById?.id, `is`(reminder.id))
       assertThat(reminderById?.title, `is`(reminder.title))
       assertThat(reminderById?.description, `is`(reminder.description))
       assertThat(reminderById?.location, `is`(reminder.location))
       assertThat(reminderById?.latitude, `is`(reminder.latitude))
       assertThat(reminderById?.longitude, `is`(reminder.longitude))


       assertThat(reminderById as ReminderDTO, CoreMatchers.notNullValue())

    }

    @Test
    fun getAllRemindersFromDb() = runBlockingTest {

        for (i in 0 until 3){

            val reminder = ReminderDTO("title$i"
                ,"desc$i",
                "loc$i",
                i+0.0,
                i+0.0)

            remindersDatabase.reminderDao().saveReminder(reminder)
        }


        val allRemindersSved = remindersDatabase.reminderDao().getReminders()

       assertThat(allRemindersSved, `is`(CoreMatchers.notNullValue()))
    }

    @Test
    fun insert_many_reminders_then_delete_test_delete_by_get_all() = runBlockingTest {
        for (i in 0 until 3){

            val reminder = ReminderDTO("title$i"
            ,"desc$i",
                "loc$i",
                i+0.0,
                i+0.0)

            remindersDatabase.reminderDao().saveReminder(reminder)
        }


        remindersDatabase.reminderDao().deleteAllReminders()

        val remindersList = remindersDatabase.reminderDao().getReminders()

       assertThat(remindersList, `is`(emptyList()))
    }



    @After
    fun closeDb() = remindersDatabase.close()
}