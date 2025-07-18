package com.revotech.meetingservice.business.meeting_room.graphql.mutation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.revotech.meetingservice.business.meeting_room.MeetingRoomService

@DgsComponent
class MeetingRoomMutation(
    private val meetingRoomService: MeetingRoomService
) {
    @DgsMutation
    fun deleteMeetingRoom(id: String): Boolean {
        meetingRoomService.deleteMeetingRoom(id)
        return true
    }
}
