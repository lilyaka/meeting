package com.revotech.meetingservice.business.meeting.dto

import com.revotech.meetingservice.business.meeting.model.ExportType
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

class ExportMeetingRequestDTO(
    val type: ExportType,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val startDate: LocalDate,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val endDate: LocalDate,
    val isConfig: Boolean
)