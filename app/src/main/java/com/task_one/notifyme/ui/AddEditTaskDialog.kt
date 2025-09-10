package com.task_one.notifyme.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.task_one.notifyme.R
import com.task_one.notifyme.data.RepeatRule
import com.task_one.notifyme.data.Task
import java.util.Calendar
import android.widget.CheckBox

class AddEditTaskDialog(
    private val onSubmit: (Task) -> Unit,
    private val existing: Task? = null
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_edit_task, null)
        val titleLayout: TextInputLayout = view.findViewById(R.id.tilTitle)
        val titleEdit: EditText = view.findViewById(R.id.etTitle)
        val descEdit: EditText = view.findViewById(R.id.etDesc)
        val dateEdit: EditText = view.findViewById(R.id.etDate)
        val timeEdit: EditText = view.findViewById(R.id.etTime)
        val chipDaily: com.google.android.material.chip.Chip = view.findViewById(R.id.chipDaily)
        val chipMon: com.google.android.material.chip.Chip = view.findViewById(R.id.chipMon)
        val chipTue: com.google.android.material.chip.Chip = view.findViewById(R.id.chipTue)
        val chipWed: com.google.android.material.chip.Chip = view.findViewById(R.id.chipWed)
        val chipThu: com.google.android.material.chip.Chip = view.findViewById(R.id.chipThu)
        val chipFri: com.google.android.material.chip.Chip = view.findViewById(R.id.chipFri)
        val chipSat: com.google.android.material.chip.Chip = view.findViewById(R.id.chipSat)
        val chipSun: com.google.android.material.chip.Chip = view.findViewById(R.id.chipSun)

        val cal = Calendar.getInstance()

        existing?.let { task ->
            titleEdit.setText(task.title)
            descEdit.setText(task.description ?: "")
            cal.timeInMillis = task.triggerAtEpochMillis
            dateEdit.setText(android.text.format.DateFormat.format("MMM d, yyyy", cal))
            timeEdit.setText(android.text.format.DateFormat.format("h:mm a", cal))
        }
        dateEdit.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(Calendar.YEAR, y)
                cal.set(Calendar.MONTH, m)
                cal.set(Calendar.DAY_OF_MONTH, d)
                dateEdit.setText(android.text.format.DateFormat.format("MMM d, yyyy", cal))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        timeEdit.setOnClickListener {
            TimePickerDialog(requireContext(), { _, h, min ->
                cal.set(Calendar.HOUR_OF_DAY, h)
                cal.set(Calendar.MINUTE, min)
                cal.set(Calendar.SECOND, 0)
                timeEdit.setText(android.text.format.DateFormat.format("h:mm a", cal))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }

        if (existing != null) {
            val mask = existing.repeatDaysMask
            val chips = listOf(chipMon, chipTue, chipWed, chipThu, chipFri, chipSat, chipSun)
            chips.forEachIndexed { idx, chip -> chip.isChecked = (mask and (1 shl idx)) != 0 }
            chipDaily.isChecked = mask == 0b1111111 || existing.repeatRule == RepeatRule.DAILY
        }
        chipDaily.setOnCheckedChangeListener { _, isChecked ->
            val chips = listOf(chipMon, chipTue, chipWed, chipThu, chipFri, chipSat, chipSun)
            chips.forEach { it.isChecked = isChecked }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (existing == null) R.string.app_name else R.string.app_name)
            .setView(view)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                titleLayout.error = null
                val title = titleEdit.text?.toString()?.trim().orEmpty()
                if (title.isEmpty()) {
                    titleLayout.error = "Title is required"
                    return@setOnClickListener
                }
                val whenMillis = cal.timeInMillis
                if (whenMillis <= System.currentTimeMillis()) {
                    titleLayout.error = "Choose a future time"
                    return@setOnClickListener
                }
                val chips = listOf(chipMon, chipTue, chipWed, chipThu, chipFri, chipSat, chipSun)
                val mask = chips.foldIndexed(0) { idx, acc, c -> if (c.isChecked) acc or (1 shl idx) else acc }
                val repeat = when {
                    chipDaily.isChecked || mask == 0b1111111 -> RepeatRule.DAILY
                    mask != 0 -> RepeatRule.WEEKLY
                    else -> RepeatRule.NONE
                }
                val toSave = (existing ?: Task(
                    title = title,
                    description = null,
                    triggerAtEpochMillis = whenMillis
                )).copy(
                    title = title,
                    description = descEdit.text?.toString()?.trim().takeIf { it?.isNotEmpty() == true },
                    triggerAtEpochMillis = whenMillis,
                    repeatRule = repeat,
                    repeatDaysMask = mask
                )
                onSubmit(toSave)
                dialog.dismiss()
            }
        }
        return dialog
    }
}


