package com.example.assistantmajda.assistant

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.assistantmajda.data.AssistantDao
import java.lang.IllegalArgumentException

class AssistantViewModelFactory (
    private val dataSource: AssistantDao,
            private val application: Application) :ViewModelProvider.Factory
{

    @Suppress("UNCNECKED_CAST")

    override  fun <T : ViewModel?> create(modelClass: Class<T>): T{
        if(modelClass.isAssignableFrom(AssistantViewModel::class.java)){
            return  AssistantViewModel(dataSource,application) as T
        }
        throw IllegalArgumentException("UNKOWN View Model Class")
    }

}