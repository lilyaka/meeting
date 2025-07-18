package com.revotech.meetingservice.business.meeting_attendee.graphql.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.revotech.graphql.type.CustomPageable
import com.revotech.meetingservice.business.meeting_attendee.MeetingAttendeeService
import com.revotech.meetingservice.business.meeting_attendee.model.MeetingAttendee
import graphql.relay.Connection

@DgsComponent
class MeetingAttendeeDataFetcher(
    private val meetingAttendeeService: MeetingAttendeeService,
) {
    @DgsQuery
    fun getAttendeeByMeetingId(id: String, pageable: CustomPageable): Connection<MeetingAttendee> =
        meetingAttendeeService.getAttendeesByMeetingId(id, pageable)
}
