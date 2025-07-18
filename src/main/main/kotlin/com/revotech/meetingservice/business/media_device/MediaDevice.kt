package com.revotech.meetingservice.business.media_device

import com.revotech.audit.JpaActivityInfo
import com.revotech.meetingservice.business.file_attachment.model.FileAttachment
import com.revotech.meetingservice.business.meeting_room.MeetingRoomStatus
import jakarta.persistence.*
import kotlin.jvm.Transient

@Entity
@Table(name = "mt_media_device")
class MediaDevice(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: String?,
    var name: String?,
    var description: String?,
    var mediaOrDevice: Boolean = false,
    @Enumerated(EnumType.STRING)
    var status: MeetingRoomStatus? = MeetingRoomStatus.ACTIVE,
    var isDeleted: Boolean = false,
    var norder: Int? = 1,
    @Transient
    var images: List<FileAttachment>? = null,
) : JpaActivityInfo() {
    constructor() : this(
        null,
        null,
        null,
        false,
        MeetingRoomStatus.ACTIVE,
        false,
        0,
    )
}