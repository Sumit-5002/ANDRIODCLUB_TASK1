package com.task_one.notifyme

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.task_one.notifyme.data.NotifyMeDatabase
import com.task_one.notifyme.data.Task
import com.task_one.notifyme.data.TaskRepository
import com.task_one.notifyme.databinding.ActivityMainBinding
import com.task_one.notifyme.notifications.AlarmScheduler
import com.task_one.notifyme.ui.TasksAdapter
import com.task_one.notifyme.ui.TasksViewModel
import com.task_one.notifyme.ui.AddEditTaskDialog
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import android.provider.Settings
import android.app.AlarmManager
import android.content.Intent
import android.os.PowerManager
import android.net.Uri

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TasksAdapter
    private lateinit var scheduler: AlarmScheduler

    private val viewModel: TasksViewModel by viewModels {
        val dao = NotifyMeDatabase.get(this).taskDao()
        val repo = TaskRepository(dao)
        TasksViewModel.Factory(repo)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.topAppBar))
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        scheduler = AlarmScheduler(this)
        setupList()
        setupFab()
        observeTasks()
        requestNotificationPermissionIfNeeded()
        requestExactAlarmPermissionIfNeeded()
        requestIgnoreBatteryOptimizationsIfNeeded()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean -> }

    private fun requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun requestExactAlarmPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            val canExact = alarmManager.canScheduleExactAlarms()
            if (!canExact) {
                try {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                } catch (_: Exception) {}
            }
        }
    }

    private fun requestIgnoreBatteryOptimizationsIfNeeded() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val pkg = packageName
        if (!pm.isIgnoringBatteryOptimizations(pkg)) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$pkg")
                }
                startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    private fun setupList() {
        adapter = TasksAdapter(onClick = { task -> onTaskClicked(task) }, onDelete = { task -> confirmDelete("Delete this reminder?") { onDeleteTask(task) } })
        binding.recyclerTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerTasks.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            AddEditTaskDialog(onSubmit = { task -> // Explicitly name onSubmit
                viewModel.upsertAndReturnId(task) { id ->
                    val saved = task.copy(id = id)
                    scheduler.schedule(saved)
                    Snackbar.make(binding.root, "Reminder saved", Snackbar.LENGTH_SHORT).show()
                }
            }).show(supportFragmentManager, "add")
        }
    }

    private fun observeTasks() {
        viewModel.tasksLiveData.observe(this) { list ->
            adapter.submitList(list)
            binding.emptyView.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun onTaskClicked(task: Task) {
        AddEditTaskDialog(onSubmit = { updated ->
            viewModel.upsertAndReturnId(updated) { id ->
                val saved = updated.copy(id = id)
                scheduler.cancel(saved)
                scheduler.schedule(saved)
            }
        }, existing = task).show(supportFragmentManager, "edit")
    }

    private fun onDeleteTask(task: Task) {
        viewModel.delete(task)
        scheduler.cancel(task)
        Snackbar.make(binding.root, "Reminder deleted", Snackbar.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_all -> {
                confirmDelete("Clear all reminders?") {
                    adapter.currentList.forEach { scheduler.cancel(it) }
                    viewModel.clearAll()
                    Snackbar.make(binding.root, "All reminders cleared", Snackbar.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_delete_selected -> {
                val toDelete = adapter.selectedIds
                if (toDelete.isEmpty()) return true
                confirmDelete("Delete selected reminders?") {
                    val items = adapter.currentList.filter { toDelete.contains(it.id) }
                    items.forEach { scheduler.cancel(it); viewModel.delete(it) }
                    adapter.selectedIds.clear()
                    Snackbar.make(binding.root, "Selected reminders deleted", Snackbar.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmDelete(message: String, onYes: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("Delete") { _, _ -> onYes() }
            .setNegativeButton("Cancel", null)
            .show()
    }
}