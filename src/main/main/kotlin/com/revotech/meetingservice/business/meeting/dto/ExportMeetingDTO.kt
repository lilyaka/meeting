package com.revotech.meetingservice.business.meeting.dto

import com.revotech.meetingservice.business.meeting.model.StatusMeetingEnum
import java.time.LocalDateTime

data class ExportMeetingDTO(
    val meetingId: String = "",
    var content: String = "",
    var startTime: LocalDateTime = LocalDateTime.now(),
    var endTime: LocalDateTime? = null,
    var roomName: String? = null,
    var attendeeId: String? = null,
    var isHost: Boolean? = false,
    var onlineUrl: String? = null,
    var guest: String? = null,
    var setup: String? = null,
    var note: String? = null,
    var status: StatusMeetingEnum = StatusMeetingEnum.DRAFT,
) {
    var attendeeName: String? = null
}