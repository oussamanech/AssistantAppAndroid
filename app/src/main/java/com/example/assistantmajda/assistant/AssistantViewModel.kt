package com.example.assistantmajda.assistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.assistantmajda.data.Assistant
import com.example.assistantmajda.data.AssistantDao
import kotlinx.coroutines.*

class AssistantViewModel (val database: AssistantDao,application: Application): AndroidViewModel(application) {

    private var viewModelJob = Job()
    private var imageIndex: Int = 0

    override fun onCleard(){
        super.onCleared()
        viewModelJob.cancel()
    }


    private  val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var currentMassage = MutableLiveData<Assistant?>()

    val messages = database.getAllMessages()

    init{
        initalizeCurrentMessage()
    }

    private fun initalizeCurrentMessage() {
        uiScope.launch { currentMassage.value = getCurrentMessageFromDatabase() }

    }

    private suspend fun getCurrentMessageFromDatabase(): Assistant? {
        return  withContext(Dispatchers.IO){
            var messagge = database.getCurrentMessage()
            if(messagge?.assistant_message == "DEFAULT_MESSAGE" || messagge?.human_message == "DEFAULT_MESSAGE" ){
                messagge = null
            }
            messagge
        }
    }


    fun senMessageToDasaBase(assistantMesssage: String , humaanMessage: String){

        uiScope.launch {
            val newAssistant = Assistant()
            newAssistant.assistant_message = assistantMesssage
            newAssistant.human_message = humaanMessage
            insert(newAssistant)
            currentMassage.value=getCurrentMessageFromDatabase()
        }
    }

    private suspend fun insert(message: Assistant){
        withContext(Dispatchers.IO){
            database.insert(message)
        }
    }

    private suspend fun update(message: Assistant){
        withContext(Dispatchers.IO){
            database.update(message)
        }
    }

    fun onClear(){
        uiScope.launch {
            clear()
            currentMassage.value=null
        }
    }

    private suspend fun clear(){
        withContext(Dispatchers.IO){
            database.clear()
        }
    }





}