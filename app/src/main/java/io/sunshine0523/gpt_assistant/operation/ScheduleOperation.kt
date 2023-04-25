package io.sunshine0523.gpt_assistant.operation

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import android.text.TextUtils
import io.sunshine0523.gpt_assistant.R
import io.sunshine0523.gpt_assistant.utils.PermissionUtils
import java.text.SimpleDateFormat
import java.util.TimeZone


class ScheduleOperation(private val context: Context) : Operation{

    companion object {
        private const val CALENDAR_URL = "content://com.android.calendar/calendars"
        private const val CALENDAR_EVENT_URL = "content://com.android.calendar/events"
        private const val CALENDAR_REMINDER_URL = "content://com.android.calendar/reminders"
    }

    @SuppressLint("SimpleDateFormat")
    fun makeSchedule(callback: OperationCallback, date: String, task: String) {
        if (PermissionUtils.checkRunningPermission(context, arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR))) {
            try {
                val taskDate = SimpleDateFormat("yyyy-MM-dd hh:mm").parse(date)
                insertCalendarEvent(task, task, taskDate.time, taskDate.time + 1000)
                callback.onFinish(true)
            } catch (e: Exception) {
                callback.onFinish(false, LinkedHashMap<String, String>().apply { put("Error", "$e ${e.stackTrace} ${e.cause}") })
            }
        } else {
            callback.onFinish(false, LinkedHashMap<String, String>().apply { put("Error", "Do not has read or write calendar permission") })
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun listSchedule(callback: OperationCallback, startDateStr: String, endDateStr: String) {
        if (PermissionUtils.checkRunningPermission(context, arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR))) {
            try {
                val startDate = SimpleDateFormat("yyyy-MM-dd hh:mm").parse(startDateStr)
                val endDate = SimpleDateFormat("yyyy-MM-dd hh:mm").parse(endDateStr)
                callback.onFinish(true, LinkedHashMap<String, String>().apply { put("scheduleList", listCalendarEvent(startDate.time, endDate.time)) })
            } catch (e: Exception) {
                e.printStackTrace()
                callback.onFinish(false, LinkedHashMap<String, String>().apply { put("Error", "$e ${e.stackTrace} ${e.cause}") })
            }
        } else {
            callback.onFinish(false, LinkedHashMap<String, String>().apply { put("Error", "Do not has read or write calendar permission") })
        }
    }

    private fun insertCalendarEvent(
        title: String?, description: String?,
        beginTimeMillis: Long, endTimeMillis: Long
    ): Boolean {
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description)) {
            return false
        }
        val calId: Int = checkCalendarAccount(context) // 获取日历账户的id
        if (calId < 0) { // 获取账户id失败直接返回，添加日历事件失败
            return false
        }
        try {
            /** 插入日程  */
            val eventValues = ContentValues()
            eventValues.put(CalendarContract.Events.DTSTART, beginTimeMillis) // 事件起始时间
            eventValues.put(CalendarContract.Events.DTEND, endTimeMillis) // 事件结束时间
            eventValues.put(CalendarContract.Events.TITLE, title) // 事件标题
            eventValues.put(CalendarContract.Events.DESCRIPTION, description) // 事件描述
            eventValues.put(CalendarContract.Events.CALENDAR_ID, 1) // 使用默认日历目录
            eventValues.put(
                CalendarContract.Events.EVENT_TIMEZONE,
                TimeZone.getDefault().id
            ) // 获取默认时区
            val eUri = context.contentResolver.insert(Uri.parse(CALENDAR_EVENT_URL), eventValues)
            val eventId = ContentUris.parseId(eUri!!)
            if (eventId == 0L) { // 插入失败
                return false
            }
            /** 插入提醒 - 依赖插入日程成功  */
            val reminderValues = ContentValues()
            reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId)
            reminderValues.put(CalendarContract.Reminders.MINUTES, 10) // 提前10分钟提醒
            reminderValues.put(
                CalendarContract.Reminders.METHOD,
                CalendarContract.Reminders.METHOD_ALERT
            ) // 提示方式提醒
            val rUri = context.contentResolver.insert(
                Uri.parse(CALENDAR_REMINDER_URL),
                reminderValues
            )
            if (rUri == null || ContentUris.parseId(rUri) == 0L) {
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    @SuppressLint("Range", "SimpleDateFormat")
    private fun listCalendarEvent(startTimeMillis: Long, endTimeMillis: Long): String {
        val calId: Int = checkCalendarAccount(context)
        if (calId < 0) {
            return ""
        }
        val eventCursor = context.contentResolver.query(
            Uri.parse(CALENDAR_EVENT_URL), null,
            null, null, "dtstart" + " DESC"
        )
        val calendarEventList = ArrayList<CalendarEventBean>()
        while (eventCursor!!.moveToNext()) {
            calendarEventList.add(CalendarEventBean(
                eventCursor.getString(eventCursor.getColumnIndex(CalendarContract.Events.TITLE)),
                eventCursor.getString(eventCursor.getColumnIndex(CalendarContract.Events.DESCRIPTION)),
                eventCursor.getString(eventCursor.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)),
                eventCursor.getLong(eventCursor.getColumnIndex(CalendarContract.Events.DTSTART)),
                eventCursor.getLong(eventCursor.getColumnIndex(CalendarContract.Events.DTEND))
            ))
        }
        val result = StringBuilder()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
        result.append("<b>").append(context.getString(R.string.calendar_list_info)).append("</b><br>")
        calendarEventList.filter { calendarEventBean ->
            startTimeMillis <= calendarEventBean.startTime && endTimeMillis >= calendarEventBean.endTime
        }.forEach { calendarEventBean ->
            result.append("<b>Title:</b> ${calendarEventBean.eventTitle}<br>")
            result.append("<b>Description:</b> ${calendarEventBean.description}<br>")
            result.append("<b>Location:</b> ${calendarEventBean.location}<br>")
            try {
                result.append("<b>StartTime:</b> ${simpleDateFormat.format(calendarEventBean.startTime)}<br>")
                result.append("<b>EndTime:</b> ${simpleDateFormat.format(calendarEventBean.endTime)}<br>")
            } catch (_: Exception) {}
            result.append("----------<br>")
        }
        return result.toString()
    }

    @SuppressLint("Range")
    private fun checkCalendarAccount(context: Context): Int {
        val userCursor = context.contentResolver.query(
            Uri.parse(CALENDAR_URL),
            null, null, null, null
        )
        return userCursor.use { userCursor ->
            if (userCursor == null) { // 查询返回空值
                return -1
            }
            val count = userCursor.count
            if (count > 0) { // 存在现有账户，取第一个账户的id返回
                userCursor.moveToFirst()
                userCursor.getInt(userCursor.getColumnIndex(CalendarContract.Calendars._ID))
            } else {
                -1
            }
        }
    }

    data class CalendarEventBean(
        val eventTitle: String? = "",
        val description: String? = "",
        val location: String? = "",
        val startTime: Long = 0,
        val endTime: Long = 0
    )
}