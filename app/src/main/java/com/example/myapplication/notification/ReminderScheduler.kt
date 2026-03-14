package com.example.myapplication.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import com.example.myapplication.model.ConsultationBooking
import com.example.myapplication.model.ConsultationSlot
import com.example.myapplication.model.Event
import com.example.myapplication.model.Expert
import com.example.myapplication.model.SavedEvent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

private const val REMINDER_MINUTES_BEFORE = 15L

fun scheduleSavedEventReminders(
    context: Context,
    userId: Int,
    events: List<Event>,
    savedEvents: List<SavedEvent>
) {
    val savedEventIds = savedEvents.filter { it.userId == userId }.map { it.eventId }.toSet()
    val userEvents = events.filter { savedEventIds.contains(it.id) }
    userEvents.forEach { event ->
        val startAtMillis = startTimeMillis(event.startTime) ?: return@forEach
        val triggerAtMillis = startAtMillis - REMINDER_MINUTES_BEFORE * 60_000
        scheduleReminder(
            context = context,
            requestCode = reminderRequestCode("event", userId, event.id),
            notificationId = reminderNotificationId("event", userId, event.id),
            title = "Event reminder",
            message = "${event.title} starts in 15 minutes.",
            triggerAtMillis = triggerAtMillis,
            startAtMillis = startAtMillis
        )
    }
}

fun cancelSavedEventReminder(
    context: Context,
    userId: Int,
    eventId: Int
) {
    cancelReminder(context, reminderRequestCode("event", userId, eventId))
}

fun scheduleConsultationReminders(
    context: Context,
    userId: Int,
    experts: List<Expert>,
    slots: List<ConsultationSlot>,
    bookings: List<ConsultationBooking>
) {
    val expertById = experts.associateBy { it.id }
    val slotById = slots.associateBy { it.id }
    bookings.filter { it.userId == userId }.forEach { booking ->
        val slot = slotById[booking.slotId] ?: return@forEach
        val expertName = expertById[slot.expertId]?.name ?: "Expert"
        val startAtMillis = startTimeMillis(slot.startTime) ?: return@forEach
        val triggerAtMillis = startAtMillis - REMINDER_MINUTES_BEFORE * 60_000
        scheduleReminder(
            context = context,
            requestCode = reminderRequestCode("consult", userId, slot.id),
            notificationId = reminderNotificationId("consult", userId, slot.id),
            title = "Consultation reminder",
            message = "Consultation with $expertName starts in 15 minutes.",
            triggerAtMillis = triggerAtMillis,
            startAtMillis = startAtMillis
        )
    }
}

private fun scheduleReminder(
    context: Context,
    requestCode: Int,
    notificationId: Int,
    title: String,
    message: String,
    triggerAtMillis: Long,
    startAtMillis: Long
) {
    val now = System.currentTimeMillis()
    val sentKey = "sent_${requestCode}_$startAtMillis"

    // If app opens in the valid reminder window, show immediately once.
    if (now in triggerAtMillis until startAtMillis) {
        val prefs = context.getSharedPreferences("reminder_state", Context.MODE_PRIVATE)
        val alreadySent = prefs.getBoolean(sentKey, false)
        if (!alreadySent) {
            val immediateIntent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(ReminderReceiver.EXTRA_TITLE, title)
                putExtra(ReminderReceiver.EXTRA_MESSAGE, message)
                putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            ReminderReceiver().onReceive(context, immediateIntent)
            prefs.edit { putBoolean(sentKey, true) }
        }
        return
    }

    if (triggerAtMillis <= now) return

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra(ReminderReceiver.EXTRA_TITLE, title)
        putExtra(ReminderReceiver.EXTRA_MESSAGE, message)
        putExtra(ReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    try {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    } catch (_: SecurityException) {
        // Fallback for devices/versions where exact alarms are restricted.
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}

private fun cancelReminder(context: Context, requestCode: Int) {
    val intent = Intent(context, ReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
}

private fun startTimeMillis(startTime: String): Long? {
    val parsedDateTime = runCatching { LocalDateTime.parse(startTime) }.getOrNull()
    val startDateTime = if (parsedDateTime != null) {
        parsedDateTime
    } else {
        val parsedTime = runCatching { LocalTime.parse(startTime) }.getOrNull() ?: return null
        LocalDateTime.of(LocalDate.now(), parsedTime)
    }
    return startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun reminderRequestCode(type: String, userId: Int, entityId: Int): Int {
    return "$type-$userId-$entityId".hashCode()
}

private fun reminderNotificationId(type: String, userId: Int, entityId: Int): Int {
    return "n-$type-$userId-$entityId".hashCode()
}
