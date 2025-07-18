package com.revotech.meetingservice.business.meeting.graphql.dataloader

import com.netflix.graphql.dgs.DgsDataLoader
import com.revotech.meetingservice.business.meeting_attendee.MeetingAttendeeService
import com.revotech.meetingservice.business.meeting_attendee.model.MeetingAttendee
import com.revotech.util.WebUtil
import org.dataloader.MappedBatchLoader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@DgsDataLoader(name = "attendees")
class AttendeeDataLoader(private val attendeeService: MeetingAttendeeService, private val webUtil: WebUtil) :
    MappedBatchLoader<MeetingKeyLoader, List<MeetingAttendee>> {
    override fun load(keys: MutableSet<MeetingKeyLoader>): CompletionStage<MutableMap<MeetingKeyLoader, List<MeetingAttendee>>> {
        return CompletableFuture.supplyAsync {
            keys.associateWith { key ->
                webUtil.changeTenant(key.tenantId) {
                    attendeeService.getAttendeesByMeetingIdAndIsHost(key.id, false)
                }
            }.toMutableMap()
        }
    }
}
