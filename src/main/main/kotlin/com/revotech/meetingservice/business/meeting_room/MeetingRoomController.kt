package com.revotech.meetingservice.business.meeting_room

import com.revotech.meetingservice.business.meeting_room.graphql.type.MeetingRoomInput
import com.revotech.meetingservice.business.meeting_room.model.MeetingRoom
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/meeting-room")
class MeetingRoomController(
    private val meetingRoomService: MeetingRoomService
) {
    @PostMapping
    fun createMeetingRoom(
        @ModelAttribute meetingRoomInput: MeetingRoomInput
    ): MeetingRoom {
        return meetingRoomService.createMeetingRoom(meetingRoomInput)
    }

    @PutMapping
    fun updateMeetingRoom(
        @ModelAttribute meetingRoomInput: MeetingRoomInput
    ): MeetingRoom {
        return meetingRoomService.createMeetingRoom(meetingRoomInput)
    }
}
