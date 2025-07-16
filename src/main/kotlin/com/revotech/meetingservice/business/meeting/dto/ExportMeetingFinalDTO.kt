package com.revotech.meetingservice.business.meeting.dto

import com.revotech.meetingservice.business.meeting.model.StatusMeetingEnum
import java.time.LocalDateTime

data class ExportMeetingFinalDTO(
    var content: String = "",
    var startTime: LocalDateTime = LocalDateTime.now(),
    var endTime: LocalDateTime? = null,
    var hostName: String? = null,
    var roomName: String? = null,
    var attendeeNames: String? = null,
    var onlineUrl: String? = null,
    var guest: String? = null,
    var setup: String? = null,
    var note: String? = null,
    var status: StatusMeetingEnum = StatusMeetingEnum.DRAFT,
) {
    constructor(dto: List<ExportMeetingDTO>) : this() {
        if (dto.isNotEmpty()) {
            val firstDto = dto[0]
            this.content = firstDto.content
            this.startTime = firstDto.startTime
            this.endTime = firstDto.endTime
            this.hostName = dto.find { it.isHost == true }?.attendeeName
            this.roomName = firstDto.roomName
            this.attendeeNames =
                dto.filter { it.attendeeName != null && it.isHost == false }
                    .joinToString(separator = ", ") { it.attendeeName.toString() }
            this.onlineUrl = firstDto.onlineUrl
            this.guest = firstDto.guest
            this.setup = firstDto.setup
            this.note = firstDto.note
            this.status = firstDto.status
        }
    }

}