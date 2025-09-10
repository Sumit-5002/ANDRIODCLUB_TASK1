package com.task_one.notifyme.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.task_one.notifyme.R
import com.task_one.notifyme.data.Task

class TasksAdapter(
    private val onClick: (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : ListAdapter<Task, TasksAdapter.TaskViewHolder>(Diff) {

    val selectedIds: MutableSet<Long> = linkedSetOf()

    object Diff : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val subtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        private var checkbox: CheckBox? = itemView.findViewById(R.id.cbSelect)

        fun bind(task: Task) {
            title.text = task.title
            subtitle.text = formatSubtitle(task)
            itemView.setOnClickListener { onClick(task) }
            btnDelete.setOnClickListener { onDelete(task) }
            checkbox?.setOnCheckedChangeListener(null)
            checkbox?.visibility = View.VISIBLE
            checkbox?.isChecked = selectedIds.contains(task.id)
            checkbox?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedIds.add(task.id) else selectedIds.remove(task.id)
            }
        }

        private fun formatSubtitle(task: Task): String {
            return "Due: " + android.text.format.DateFormat.format("MMM d, h:mm a", task.triggerAtEpochMillis)
        }
    }
}


