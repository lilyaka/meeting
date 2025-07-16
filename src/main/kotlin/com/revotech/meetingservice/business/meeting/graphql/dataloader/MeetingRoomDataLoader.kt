package com.revotech.meetingservice.business.meeting.graphql.dataloader

import com.netflix.graphql.dgs.DgsDataLoader
import com.revotech.meetingservice.business.meeting_room.MeetingRoomService
import com.revotech.meetingservice.business.meeting_room.model.MeetingRoom
import com.revotech.util.WebUtil
import org.dataloader.MappedBatchLoader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@DgsDataLoader(name = "meetingRoom")
class MeetingRoomDataLoader(private val meetingRoomService: MeetingRoomService, private val webUtil: WebUtil) :
    MappedBatchLoader<MeetingKeyLoader, MeetingRoom> {
    override fun load(keys: MutableSet<MeetingKeyLoader>): CompletionStage<MutableMap<MeetingKeyLoader, MeetingRoom>> {
        return CompletableFuture.supplyAsync {
            keys.associateWith { key ->
                webUtil.changeTenant(key.tenantId) {
                    meetingRoomService.findMeetingRoomById(key.id)
                }
            }.toMutableMap()
        }
    }
}
