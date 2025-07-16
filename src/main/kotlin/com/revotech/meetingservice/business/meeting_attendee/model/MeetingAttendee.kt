package com.revotech.meetingservice.business.meeting_attendee.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.revotech.audit.ActivityInfo
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "mt_attendee")
@JsonInclude(JsonInclude.Include.NON_NULL)
class MeetingAttendee(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: String? = null,
    var meetingId: String,
    var userId: String,
    var isHost: Boolean = false,
    var seen: Boolean = false,
    var seenTime: LocalDateTime? = null,
    var participate: Boolean = true,
    var reason: String? = null,
    var isDeleted: Boolean? = false,
) : ActivityInfo()
