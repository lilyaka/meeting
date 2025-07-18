package com.revotech.meetingservice.business.meeting_attendee

import com.revotech.meetingservice.business.meeting_attendee.model.MeetingAttendee
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MeetingAttendeeRepository : JpaRepository<MeetingAttendee, String> {


    fun findByMeetingIdAndUserIdAndIsDeleted(
        meetingId: String,
        userId: String,
        isDeleted: Boolean = false
    ): Optional<MeetingAttendee>


    fun findByMeetingIdAndIsDeleted(
        meetingId: String,
        isDeleted: Boolean = false,
        pageable: Pageable
    ): Page<MeetingAttendee>

    fun findAllByMeetingIdAndUserIdNotInAndIsDeleted(
        meetingId: String,
        userIds: MutableList<String>?,
        isDeleted: Boolean = false
    ): List<MeetingAttendee>

    fun findByMeetingIdAndIsDeletedAndIsHost(
        meetingId: String,
        isDeleted: Boolean,
        isHost: Boolean
    ): List<MeetingAttendee>

    fun findByMeetingIdAndIsDeleted(meetingId: String, isDeleted: Boolean): List<MeetingAttendee>

    fun findAllByMeetingIdAndIsDeletedFalse(meetingId: String): List<MeetingAttendee>

    fun findByUserIdAndMeetingId(userId: String, meeting: String): MeetingAttendee?
}
