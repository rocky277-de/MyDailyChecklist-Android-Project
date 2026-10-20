package com.example.mydailychecklist

import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.graphics.Color

class ChecklistWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = TaskFactory(applicationContext)
}

private class TaskFactory(private val context: android.content.Context) : RemoteViewsService.RemoteViewsFactory {
    private var tasks: List<String> = emptyList()
    private var done: Set<String> = emptySet()

    override fun onCreate() = load()
    override fun onDataSetChanged() = load()
    override fun onDestroy() {}
    override fun getCount() = tasks.size
    override fun getViewAt(position: Int): RemoteViews? {
        if (position !in tasks.indices) return null
        val task = tasks[position]
        val checked = task in done
        val row = RemoteViews(context.packageName, R.layout.widget_task_row)
        val label = (if (checked) "☑  " else "☐  ") + task
        if (checked) {
            val styled = SpannableString(label)
            styled.setSpan(StrikethroughSpan(), 3, label.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            row.setTextViewText(R.id.widget_task_check, styled)
            row.setTextColor(R.id.widget_task_check, Color.GRAY)
        } else {
            row.setTextViewText(R.id.widget_task_check, label)
            row.setTextColor(R.id.widget_task_check, Color.DKGRAY)
        }
        val fill = Intent().putExtra(ChecklistWidgetProvider.EXTRA_TASK, task)
        row.setOnClickFillInIntent(R.id.widget_task_check, fill)
        return row
    }
    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount() = 1
    override fun getItemId(position: Int) = tasks.getOrNull(position)?.hashCode()?.toLong() ?: position.toLong()
    override fun hasStableIds() = true

    private fun load() {
        val prefs = context.getSharedPreferences("checklist", android.content.Context.MODE_PRIVATE)
        tasks = (prefs.getStringSet("tasks", setOf("Study for 30 minutes", "Drink water", "Revise yesterday's lesson")) ?: emptySet()).toList().sorted()
        done = prefs.getStringSet("widget_done", emptySet()) ?: emptySet()
    }
}