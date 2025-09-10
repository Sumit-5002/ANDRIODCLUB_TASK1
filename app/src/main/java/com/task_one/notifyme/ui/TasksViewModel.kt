package com.task_one.notifyme.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.task_one.notifyme.data.Task
import com.task_one.notifyme.data.TaskRepository
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(private val repository: TaskRepository) : ViewModel() {
    val tasks: StateFlow<List<Task>> = repository.observeTasks()
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val tasksLiveData = tasks.asLiveData()

    fun upsert(task: Task) {
        viewModelScope.launch { repository.addOrUpdate(task) }
    }

    fun upsertAndReturnId(task: Task, onResult: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.addOrUpdate(task)
            onResult(id)
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch { repository.delete(task) }
    }

    fun clearAll() {
        viewModelScope.launch { repository.clear() }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TasksViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


