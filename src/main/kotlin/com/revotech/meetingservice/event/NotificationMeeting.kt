package com.revotech.meetingservice.event

import com.revotech.dto.NotificationRequest
import com.revotech.event.notification.NotificationEvent
import com.revotech.event.notification.NotificationPayload
import com.revotech.meetingservice.business.meeting.model.Meeting
import com.revotech.meetingservice.business.meeting_attendee.MeetingAttendeeService
import com.revotech.meetingservice.business.meeting_room.MeetingRoomService
import org.springframework.context.ApplicationEventPublisher
import java.time.format.DateTimeFormatter
import org.springframework.stereotype.Service

@Service
class NotificationMeetingService(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val meetingAttendeeService: MeetingAttendeeService,
    private val meetingRoomService: MeetingRoomService
) {
    fun sendMeetingNotification(meeting: Meeting, webUtilTenant: String, webUtilUser: String) {
        val attendees = meeting.id?.let { meetingAttendeeService.getListAttendeesByMeetingId(it) }
        val userIds = (attendees?.map { it.userId } ?: emptyList())
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        // Lấy thông tin phòng họp
        val roomName = meeting.roomId?.let {
            try {
                meetingRoomService.findMeetingRoomById(it).name ?: "Phòng họp không xác định"
            } catch (e: Exception) {
                "Phòng họp không xác định"
            }
        } ?: "Phòng họp không xác định"

        userIds.forEach { userId ->
            val title = "Bạn có lịch họp lúc ${meeting.startTime.format(timeFormatter)}"
            val content = buildString {
                append(meeting.content)
                append("\n")
                append(roomName)
            }

            val notification = NotificationEvent(
                NotificationPayload(
                    webUtilTenant, webUtilUser,
                    NotificationRequest(
                        userId = userId,
                        content = content,
                        module = "MEETING",
                        function = "MEETING/REMINDER",
                        title = title,
                        action = "MEETING_REMINDER"
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