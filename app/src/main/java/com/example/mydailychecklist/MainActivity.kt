package com.example.mydailychecklist

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    private val tasks = mutableListOf<String>()
    private val done = mutableSetOf<Int>()
    private lateinit var list: LinearLayout
    private lateinit var count: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("checklist", MODE_PRIVATE)
        tasks.addAll(prefs.getStringSet("tasks", setOf("Study for 30 minutes", "Drink water", "Revise yesterday's lesson"))!!.sorted())
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 24, 20, 16); setBackgroundColor(0xFFFFF7F2.toInt()) }
        val title = TextView(this).apply { text = "My Daily Checklist"; textSize = 26f; setTextColor(0xFF242424.toInt()); gravity = Gravity.CENTER_VERTICAL }
        count = TextView(this).apply { textSize = 14f; setPadding(0, 8, 0, 12); setTextColor(0xFF666666.toInt()) }
        val add = Button(this).apply { text = "+ Add task"; setOnClickListener { addTask() } }
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(title); root.addView(count); root.addView(add)
        root.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root); render()
    }
    private fun addTask() {
        val input = EditText(this).apply { hint = "Enter a task" }
        AlertDialog.Builder(this).setTitle("New task").setView(input).setNegativeButton("Cancel", null).setPositiveButton("Add") { _, _ ->
            val value = input.text.toString().trim()
            if (value.isNotEmpty()) { tasks.add(value); save(); render() }
        }.show()
    }
    private fun render() {
        list.removeAllViews()
        tasks.forEachIndexed { i, task ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(4, 6, 4, 6) }
            val check = CheckBox(this).apply { text = task; textSize = 16f; isChecked = i in done; setOnCheckedChangeListener { _, checked -> if (checked) done.add(i) else done.remove(i); count.text = "${done.size} of ${tasks.size} completed"; save() } }
            row.addView(check, LinearLayout.LayoutParams(0, -2, 1f))
            val delete = ImageButton(this).apply { setImageResource(android.R.drawable.ic_menu_delete); contentDescription = "Delete task"; setOnClickListener { tasks.removeAt(i); done.clear(); save(); render() } }
            row.addView(delete, ViewGroup.LayoutParams(48, 48)); list.addView(row)
        }
        count.text = "${done.size} of ${tasks.size} completed"
    }
    private fun save() { getSharedPreferences("checklist", MODE_PRIVATE).edit().putStringSet("tasks", tasks.toSet()).apply(); ChecklistWidgetProvider.updateAll(this) }
}
