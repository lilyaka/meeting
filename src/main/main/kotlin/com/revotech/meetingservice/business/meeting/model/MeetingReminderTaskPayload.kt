package com.revotech.meetingservice.business.meeting.model

data class MeetingReminderTaskPayload(
    val taskType: String,
    val service: String,
    val endpoint: String,
    val cronExpression: String,
    val meetingId: String,
    val remindMinutes: Int
)