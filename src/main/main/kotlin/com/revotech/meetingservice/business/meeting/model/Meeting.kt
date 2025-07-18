package com.revotech.meetingservice.business.meeting.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.revotech.audit.JpaActivityInfo
import com.revotech.meetingservice.business.meeting.dto.MeetingDTO
import com.revotech.meetingservice.business.meeting_attendee.model.MeetingAttendee
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.time.LocalDateTime
import kotlin.jvm.Transient

@Entity
@Table(name = "mt_meeting")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Meeting(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: String?,
    @Column(columnDefinition = "TEXT")
    var content: String,
    var startTime: LocalDateTime = LocalDateTime.now(),
    var endTime: LocalDateTime?,
    var hostId: String,
    var roomId: String?,
    var meetingType: MeetingType = MeetingType.ONLINE,
    var onlineUrl: String?,
    @Type(JsonBinaryType::class)
    @Column(columnDefinition = "jsonb")
    var departmentIds: List<String>?,
    @Type(JsonBinaryType::class)
    @Column(columnDefinition = "jsonb")
    var mediaDeviceIds: List<String>?,
    var guest: String?,
    @Column(length = 5000)
    var setup: String?,
    @Column(length = 5000)
    var note: String?,
    var isImportant: Boolean = false,
    var isPrivate: Boolean? = false,
    @Enumerated(EnumType.STRING)
    var repeat: RepeatMeeting = RepeatMeeting.NO_REPEAT,
    var numberAttendees: Int = 0,
    @Enumerated(EnumType.STRING)
    var status: StatusMeetingEnum = StatusMeetingEnum.DRAFT,
    var remind: Int? = null,
    var remindTimeType: RemindTimeType? = RemindTimeType.MINUTE,
    var remindType: RemindType? = RemindType.NOTIFICATION,
    var isDeleted: Boolean? = false,
    var isBase: Boolean? = true,
    var cronConfig: String? = null,
    var projectId: String? = null,
    @Transient
    var attendees: List<MeetingAttendee>? = emptyList(),

    ) : JpaActivityInfo() {
    constructor(meetingDTO: MeetingDTO) : this(
        id = meetingDTO.id,
        content = meetingDTO.content,
        startTime = meetingDTO.startTime,
        endTime = meetingDTO.endTime,
        hostId = meetingDTO.hostId,
        roomId = meetingDTO.roomId,
        meetingType = meetingDTO.meetingType,
        onlineUrl = meetingDTO.onlineUrl,
        departmentIds = meetingDTO.departmentIds,
        mediaDeviceIds = meetingDTO.mediaDeviceIds,
        setup = meetingDTO.setup,
        note = meetingDTO.note,
        isImportant = meetingDTO.isImportant,
        isPrivate = meetingDTO.isPrivate,
        repeat = meetingDTO.repeat,
        numberAttendees = meetingDTO.numberAttendees,
        guest = meetingDTO.guest,
        cronConfig = meetingDTO.cronConfig,
        status = meetingDTO.status,
        projectId = meetingDTO.projectId,
        remind = meetingDTO.remind,
        remindTimeType = meetingDTO.remindTimeType,
        remindType = meetingDTO.remindType,
    )
}

enum class StatusMeetingEnum {
    DRAFT,
    PENDING,
    GIVE_BACK,
    REFUSE,
    APPROVED,
    CANCEL
}

data class MeetingStatusPayload(
    val status: StatusMeetingEnum,
)

enum class RepeatMeeting {
    NO_REPEAT,
    DAILY_REPEAT,
    WEEKLY_REPEAT,
    MONTHLY_REPEAT,
    YEARLY_REPEAT,
}

enum class MeetingType {
    ONLINE, OFFLINE, ONLINE_OFFLINE
}

enum class RemindTimeType {
    MINUTE, HOUR, DAY
}

enum class RemindType {
    NOTIFICATION, EMAIL, ALL
}
