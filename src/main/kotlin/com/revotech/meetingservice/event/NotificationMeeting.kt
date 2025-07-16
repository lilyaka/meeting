package com.revotech.meetingservice.event

import com.revotech.dto.NotificationRequest
import com.revotech.event.notification.NotificationEvent
import com.revotech.event.notification.NotificationPayload
import com.revotech.meetingservice.business.meeting.model.Meeting
import com.revotech.meetingservice.business.meeting_attendee.MeetingAttendeeService
import org.springframework.context.ApplicationEventPublisher
import java.time.format.DateTimeFormatter
import org.springframework.stereotype.Service

@Service
class NotificationMeetingService(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val meetingAttendeeService: MeetingAttendeeService
) {
    fun sendMeetingNotification(meeting: Meeting, webUtilTenant: String, webUtilUser: String) {
        val attendees = meeting.id?.let { meetingAttendeeService.getListAttendeesByMeetingId(it) }
        val userIds = (attendees?.map { it.userId } ?: emptyList())
        val formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")

        userIds.forEach { userId ->
            val notification = NotificationEvent(
                NotificationPayload(
                    webUtilTenant, webUtilUser,
                    NotificationRequest(
                        userId = userId,
                        content = "Bạn có lịch họp ${meeting.content} vào lúc ${meeting.startTime.format(formatter)}.",
                        module = "MEETING",
                        function = "MEETING/MEETING",
                        title = "Lịch họp",
                        action = ""
                    )
                )
            )
            applicationEventPublisher.publishEvent(notification)
        }
    }

    fun createNotificationRunnable(meeting: Meeting, webUtilTenant: String, webUtilUser: String): Runnable {
        return Runnable {
            sendMeetingNotification(meeting, webUtilTenant, webUtilUser)
        }
    }
}
