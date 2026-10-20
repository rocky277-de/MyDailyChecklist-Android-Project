package com.example.mydailychecklist

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class ChecklistWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateOne(context, manager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE) {
            val task = intent.getStringExtra(EXTRA_TASK) ?: return
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val done = prefs.getStringSet(KEY_DONE, emptySet())?.toMutableSet() ?: mutableSetOf()
            if (!done.add(task)) done.remove(task)
            prefs.edit().putStringSet(KEY_DONE, done).apply()
            updateAll(context)
        }
    }

    companion object {
        private const val PREFS = "checklist"
        private const val KEY_TASKS = "tasks"
        private const val KEY_DONE = "widget_done"
        private const val ACTION_TOGGLE = "com.example.mydailychecklist.TOGGLE_TASK"
        private const val EXTRA_TASK = "task_text"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, ChecklistWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { updateOne(context, manager, it) }
        }

        private fun updateOne(context: Context, manager: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_checklist)
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val tasks = (prefs.getStringSet(KEY_TASKS, setOf("Study for 30 minutes", "Drink water", "Revise yesterday's lesson"))
                ?: emptySet()).toList().sorted().take(5)
            val done = prefs.getStringSet(KEY_DONE, emptySet()) ?: emptySet()
            views.removeAllViews(R.id.widget_task_list)

            tasks.forEachIndexed { index, task ->
                val row = RemoteViews(context.packageName, R.layout.widget_task_row)
                row.setTextViewText(R.id.widget_task_check, task)
                row.setBoolean(R.id.widget_task_check, "setChecked", task in done)
                val toggle = Intent(context, ChecklistWidgetProvider::class.java).apply {
                    action = ACTION_TOGGLE
                    putExtra(EXTRA_TASK, task)
                    data = android.net.Uri.parse("checklist://task/$id/$index")
                }
                val pending = PendingIntent.getBroadcast(
                    context, id * 100 + index, toggle,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                row.setOnClickPendingIntent(R.id.widget_task_check, pending)
                views.addView(R.id.widget_task_list, row)
            }

            val openApp = Intent(context, MainActivity::class.java)
            val openPending = PendingIntent.getActivity(
                context, id, openApp, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, openPending)
            manager.updateAppWidget(id, views)
        }
    }
}