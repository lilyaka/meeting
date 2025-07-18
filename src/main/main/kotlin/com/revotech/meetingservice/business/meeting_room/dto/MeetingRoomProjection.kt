package com.revotech.meetingservice.business.meeting_room.dto

import com.revotech.meetingservice.business.meeting.dto.BaseProjection
import com.revotech.meetingservice.business.meeting_room.MeetingRoomStatus


interface MeetingRoomProjection: BaseProjection {
    fun getId(): String
    fun getName(): String
    fun getStatus(): MeetingRoomStatus
    fun getAddress(): String
    fun getIsDeleted(): Boolean
    fun getNOrder(): Int
}