package com.revotech.meetingservice.business.meeting_room.graphql.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.revotech.graphql.GraphqlUtil
import com.revotech.graphql.type.CustomPageable
import com.revotech.meetingservice.business.meeting_room.MeetingRoomService
import com.revotech.meetingservice.business.meeting_room.model.MeetingRoom
import graphql.relay.Connection

@DgsComponent
class MeetingRoomDataFetcher(
    private val meetingRoomService: MeetingRoomService
) {
    @DgsQuery
    fun getMeetingRoomById(id: String): MeetingRoom {
        return meetingRoomService.getMeetingRoomById(id)
    }

    @DgsQuery
    fun getAllActiveMeetingRooms(): List<MeetingRoom> {
        return meetingRoomService.getAllActiveMeetingRooms()
    }

    @DgsQuery
    fun searchTextMeetingRoom(
        keyword: String?,
        pageable: CustomPageable?,
    ): Connection<out MeetingRoom> {
        val page = GraphqlUtil.toPageable(pageable)
        val meetingRooms = meetingRoomService.searchByKeyword(keyword, page)
        return GraphqlUtil.createConnection(meetingRooms)
    }

    @DgsQuery
    fun maxNOrderMeetingRoom(): Int {
        return meetingRoomService.maxNOrder()
    }
}