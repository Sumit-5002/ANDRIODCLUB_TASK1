package com.task_one.notifyme.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    fun observeTasks(): Flow<List<Task>> = taskDao.observeAll()

    suspend fun getTask(id: Long): Task? = taskDao.getById(id)

    suspend fun addOrUpdate(task: Task): Long = taskDao.upsert(task)

    suspend fun delete(task: Task) = taskDao.delete(task)

    suspend fun clear() = taskDao.clear()
}


