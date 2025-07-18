package com.revotech.meetingservice.business.meeting.graphql.mutation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.revotech.meetingservice.business.meeting.MeetingService
import com.revotech.meetingservice.business.meeting.model.Meeting
import com.revotech.meetingservice.business.meeting.model.StatusMeetingEnum

@DgsComponent
class MeetingMutation(
    private val meetingService: MeetingService,
) {
    @DgsMutation
    fun updateStatusMeeting(id: String, status: StatusMeetingEnum): Meeting {
        return meetingService.updateStatusMeeting(id, status)
    }

    @DgsMutation
    fun deleteMeeting(id: String): Boolean{
        meetingService.deleteMeeting(id)
        return true
    }
}