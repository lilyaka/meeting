package com.revotech.meetingservice.business.meeting_attendee

import com.revotech.graphql.GraphqlUtil
import com.revotech.graphql.type.CustomPageable
import com.revotech.meetingservice.business.meeting.MeetingRepository
import com.revotech.meetingservice.business.meeting.dto.MeetingDTO
import com.revotech.meetingservice.business.meeting.exception.MeetingException
import com.revotech.meetingservice.business.meeting.model.StatusMeetingEnum
import com.revotech.meetingservice.business.meeting_attendee.exception.MeetingAttendeeException
import com.revotech.meetingservice.business.meeting_attendee.model.MeetingAttendee
import com.revotech.util.WebUtil
import graphql.relay.Connection
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@Transactional
class MeetingAttendeeService(
    private val meetingAttendeeRepository: MeetingAttendeeRepository,
    private val meetingRepository: MeetingRepository,
    private val webUtil: WebUtil,
) {
    fun createAttendee(meetingDTO: MeetingDTO, meetingId: String): List<MeetingAttendee> {
        return meetingDTO.attendeeIds.map {
            MeetingAttendee(userId = it, isHost = it == meetingDTO.hostId, meetingId = meetingId).run {
                meetingAttendeeRepository.save(this)
            }
        }
    }

    fun updateAttendee(meetingDTO: MeetingDTO, meetingId: String): List<MeetingAttendee> {
        meetingAttendeeRepository.findAllByMeetingIdAndUserIdNotInAndIsDeleted(meetingId, meetingDTO.attendeeIds)
            .forEach {
                it.isDeleted = true
                meetingAttendeeRepository.save(it)
            }
        return meetingDTO.attendeeIds.map {
            meetingAttendeeRepository.findByMeetingIdAndUserIdAndIsDeleted(meetingId, it).run {
                if (this.isPresent) {
                    this.get()
                } else {
                    meetingAttendeeRepository.save(
                        MeetingAttendee(
                            userId = it,
                            isHost = it == meetingDTO.hostId,
                            meetingId = meetingId
                        )
                    )
                }
            }
        }
    }

    fun seen(meetingId: String, userId: String) {
        meetingAttendeeRepository.findByMeetingIdAndUserIdAndIsDeleted(meetingId, userId).ifPresent {
            it.seen = true
            it.seenTime = it.seenTime ?: LocalDateTime.now()
            meetingAttendeeRepository.save(it)
        }
    }

    fun getAttendeesByMeetingId(meetingId: String, pageable: CustomPageable): Connection<MeetingAttendee> {
        return meetingAttendeeRepository.findByMeetingIdAndIsDeleted(
            meetingId,
            pageable = GraphqlUtil.toPageable(pageable)
        ).let {
            GraphqlUtil.createConnection(it)
        }
    }

    fun getListAttendeesByMeetingId(meetingId: String): List<MeetingAttendee> {
        return meetingAttendeeRepository.findAllByMeetingIdAndIsDeletedFalse(meetingId)
    }

    fun getAttendeesByMeetingIdAndIsHost(meetingId: String, isHost: Boolean): List<MeetingAttendee> {
        return meetingAttendeeRepository.findByMeetingIdAndIsDeletedAndIsHost(meetingId, false, isHost)
    }

    fun updateAttendee(id: String, participate: Boolean, reason: String): String {
        val attendee = meetingAttendeeRepository.findById(id).orElseThrow {
            MeetingAttendeeException("attendeeNotFound", "Attendee not found.")
        }
        attendee.participate = participate
        attendee.reason = reason

        if (attendee.isHost == true && participate == false) {
            val meeting = meetingRepository.findById(attendee.meetingId)
                .orElseThrow { MeetingException("MeetingNotFound", "Meeting not found") }
            meeting.status = StatusMeetingEnum.CANCEL
            meetingRepository.save(meeting)
        }

        meetingAttendeeRepository.save(attendee)
        return "ok"
    }

    fun updateAttendeeByUserId(meetingId: String, participate: Boolean, reason: String): Boolean {
        val attendee = meetingAttendeeRepository.findByUserIdAndMeetingId(webUtil.getUserId(), meetingId)
            ?: throw MeetingAttendeeException("attendeeNotFound", "Attendee not found.")

        attendee.apply {
            this.participate = participate
            this.reason = reason
        }

        if (attendee.isHost == true && !participate) {
            val meeting = meetingRepository.findById(meetingId)
                .orElseThrow { MeetingException("MeetingNotFound", "Meeting not found") }

            meeting.status = StatusMeetingEnum.CANCEL
            meetingRepository.save(meeting)
        }

        meetingAttendeeRepository.save(attendee)
        return true
    }

}
