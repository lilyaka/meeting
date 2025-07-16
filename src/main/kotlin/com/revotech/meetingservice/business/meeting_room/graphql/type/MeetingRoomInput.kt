package com.revotech.meetingservice.business.meeting_room.graphql.type

import com.revotech.meetingservice.business.meeting_room.MeetingRoomStatus
import com.revotech.meetingservice.business.meeting_room.model.MeetingRoom
import org.springframework.web.multipart.MultipartFile

data class MeetingRoomInput(
    var id: String?,
    var name: String?,
    var address: String?,
    var status: MeetingRoomStatus?,
    var norder: Int?,
    val idFilesDelete: List<String>?,
    val images: List<MultipartFile>?
) {
    fun toMeetingRoom(): MeetingRoom {
        return MeetingRoom(
            id = this.id,
            name = this.name,
            address = this.address,
            status = this.status,
            norder = this.norder
        )
    }
}