package com.revotech.meetingservice.business.meeting.graphql.dataloader

import com.netflix.graphql.dgs.DgsDataLoader
import com.revotech.meetingservice.business.meeting.MeetingService
import com.revotech.meetingservice.business.meeting.model.Meeting
import com.revotech.util.WebUtil
import org.dataloader.MappedBatchLoader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@DgsDataLoader(name = "conflictMeetings")
class ConflictMeetingDataLoader(private val meetingService: MeetingService, private val webUtil: WebUtil) :
    MappedBatchLoader<MeetingKeyLoader, List<Meeting>> {
    override fun load(keys: MutableSet<MeetingKeyLoader>): CompletionStage<MutableMap<MeetingKeyLoader, List<Meeting>>> {
        return CompletableFuture.supplyAsync {
            keys.associateWith { key ->
                webUtil.changeTenant(key.tenantId) {
                    meetingService.findConflictMeeting(key.id)
                }
            }.toMutableMap()
        }
    }
}

@DgsDataLoader(name = "isConflict")
class IsConflictMeetingDataLoader(private val meetingService: MeetingService, private val webUtil: WebUtil) :
    MappedBatchLoader<MeetingKeyLoader, Boolean> {
    override fun load(keys: MutableSet<MeetingKeyLoader>): CompletionStage<MutableMap<MeetingKeyLoader, Boolean>> {
        return CompletableFuture.supplyAsync {
            keys.associateWith { key ->
                webUtil.changeTenant(key.tenantId) {
                    meetingService.isConflictMeeting(key.id)
                }
            }.toMutableMap()
        }
    }
}
