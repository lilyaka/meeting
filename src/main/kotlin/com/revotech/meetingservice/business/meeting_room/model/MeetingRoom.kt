package com.revotech.meetingservice.business.meeting_room.model

import com.revotech.audit.JpaActivityInfo
import com.revotech.meetingservice.business.file_attachment.model.FileAttachment
import com.revotech.meetingservice.business.meeting_room.MeetingRoomStatus
import com.revotech.meetingservice.business.meeting_room.dto.MeetingRoomProjection
import jakarta.persistence.*
import kotlin.jvm.Transient

@Entity
@Table(name = "mt_meeting_room")
class MeetingRoom(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: String?,
    var name: String?,
    var address: String?,
    @Enumerated(EnumType.STRING)
    var status: MeetingRoomStatus? = MeetingRoomStatus.ACTIVE,
    var isDeleted: Boolean = false,
    var norder: Int? = 1,
    @Transient
    var images: List<FileAttachment>? = null,
) : JpaActivityInfo() {
    constructor(meetingRoom: MeetingRoomProjection) : this(
        meetingRoom.getId(),
        meetingRoom.getName(),
        meetingRoom.getAddress(),
        meetingRoom.getStatus(),
        meetingRoom.getIsDeleted(),
        meetingRoom.getNOrder()
    ) {
        this.createdTime = meetingRoom.getCreatedTime()
        this.createdBy = meetingRoom.getCreatedBy()
        this.lastModifiedTime = meetingRoom.getLastModifiedTime()
        this.lastModifiedBy = meetingRoom.getLastModifiedBy()
    }
}