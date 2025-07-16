package com.revotech.meetingservice.business.meeting.dto

import com.revotech.meetingservice.business.meeting.model.*
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.multipart.MultipartFile
import java.io.Serializable
import java.time.LocalDateTime

data class MeetingDTO(
    var id: String?,
    var content: String,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var startTime: LocalDateTime,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var endTime: LocalDateTime?,
    var hostId: String,
    var attendeeIds: MutableList<String>,
    var roomId: String,
    var meetingType: MeetingType = MeetingType.ONLINE,
    var onlineUrl: String?,
    var departmentIds: MutableList<String>?,
    var mediaDeviceIds: MutableList<String>?,
    var setup: String?,
    var note: String?,
    var isImportant: Boolean = false,
    var isPrivate: Boolean = false,
    var repeat: RepeatMeeting = RepeatMeeting.NO_REPEAT,
    var numberAttendees: Int = 0,
    var fileAttachments: List<MultipartFile>?,
    var reportFileAttachments: List<MultipartFile>?,
    var filesDeleted: List<String>?,
    var reportFilesDeleted: List<String>?,
    var status: StatusMeetingEnum = StatusMeetingEnum.DRAFT,
    var guest: String?,
    var confirmed: Boolean = false,
    var cronConfig: String?,
    var projectId: String?,
    var remind: Int?,
    var remindTimeType: RemindTimeType?,
    var remindType: RemindType?
) : Serializable
