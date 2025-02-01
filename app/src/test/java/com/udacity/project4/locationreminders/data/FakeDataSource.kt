package com.udacity.project4.locationreminders.data

import com.udacity.project4.data.ReminderDataSource
import com.udacity.project4.data.dto.ReminderDTO
import com.udacity.project4.data.dto.Result


//REPO
//class FakeDataSource(private var reminderDTOMutableList: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {
class FakeDataSource : ReminderDataSource {

    private var shouldReturnError = false
    private val reminderDTOMutableList = mutableListOf<ReminderDTO>()

    override suspend fun getReminder(id: String): Result<ReminderDTO> {


        if (shouldReturnError)
//            return Result.Error("Reminder not found!")
            return Result.Error("Test exception!")

        return try{
            val found = reminderDTOMutableList.find { it.id == id }
            //return Result.Success(found)

            if (found!=null){
                Result.Success(found)
            }else{
                Result.Error("Reminder not found!")
            }

        }catch (e: Exception) {
            return Result.Error(e.localizedMessage)
        }


    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
//            return Result.Error("Reminder not found!")
            return Result.Error("Test exception!")
        }

        return try {
            reminderDTOMutableList.let {
                return Result.Success(ArrayList(it))
            }
        } catch (ex: Exception) {
            Result.Error(ex.localizedMessage)
        }

//        reminderDTOMutableList.let {
//            return Result.Success(ArrayList(it))
//        }
//        //return Result.Error("Reminder not found!")




    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderDTOMutableList.add(reminder)
    }


    override suspend fun deleteAllReminders() {
        //JUST CLEAR FAKE DATA
        reminderDTOMutableList.clear()
    }

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }


}