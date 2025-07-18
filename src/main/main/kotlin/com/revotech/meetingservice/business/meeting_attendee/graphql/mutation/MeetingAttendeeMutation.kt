package com.revotech.meetingservice.business.meeting_attendee.graphql.mutation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.revotech.meetingservice.business.meeting_attendee.MeetingAttendeeService

@DgsComponent
class MeetingAttendeeMutation(
    private val meetingAttendeeService: MeetingAttendeeService,
) {
    @DgsMutation
    fun updateAttendee(id: String, participate: Boolean, reason: String) =
        meetingAttendeeService.updateAttendee(id, participate, reason)

    @DgsMutation
    fun updateAttendeeByUserId(meetingId: String, participate: Boolean, reason: String): Boolean {
        return meetingAttendeeService.updateAttendeeByUserId(meetingId, participate, reason)
    }
}
