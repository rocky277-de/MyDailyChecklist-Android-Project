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

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, ChecklistWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { updateOne(context, manager, it) }
        }

        private fun updateOne(context: Context, manager: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, com.example.mydailychecklist.R.layout.widget_checklist)
            val prefs = context.getSharedPreferences("checklist", Context.MODE_PRIVATE)
            val tasks = prefs.getStringSet("tasks", setOf("Study for 30 minutes", "Drink water", "Revise yesterday's lesson"))
                ?.take(4)?.joinToString("\n") { "□  $it" } ?: "No tasks yet"
            views.setTextViewText(com.example.mydailychecklist.R.id.widget_tasks, tasks)
            val intent = Intent(context, MainActivity::class.java)
            val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(com.example.mydailychecklist.R.id.widget_root, pending)
            manager.updateAppWidget(id, views)
        }
    }
}