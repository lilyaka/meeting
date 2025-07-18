package com.revotech.meetingservice.business.meeting

import com.revotech.meetingservice.business.meeting.dto.ExportMeetingRequestDTO
import com.revotech.meetingservice.business.meeting.dto.MeetingDTO
import com.revotech.meetingservice.business.meeting.model.Meeting
import com.revotech.meetingservice.business.meeting.model.MeetingStatusPayload
import com.revotech.meetingservice.event.NotificationMeetingService
import com.revotech.util.WebUtil
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

const val MEETING_ENDPOINT = "/meeting"

@RestController
@RequestMapping(MEETING_ENDPOINT)
class MeetingController(
    private val meetingService: MeetingService,
    private val scheduledMeetingTask: ScheduledMeetingTask,
    private val notificationMeetingService: NotificationMeetingService,
    private val webUtil: WebUtil
) {
    @GetMapping("/{id}")
    fun getMeeting(@PathVariable id: String): Meeting {
        return meetingService.getMeetingById(id)
    }

    @GetMapping
    fun getAllMeeting(): List<Meeting> {
        return meetingService.getAllMeeting()
    }

    @PutMapping("/{id}/status")
    fun changeStatus(@PathVariable id: String, @RequestBody payload: MeetingStatusPayload): Meeting {
        return meetingService.updateStatusMeeting(id, payload.status)
    }

    @PostMapping
    fun createMeeting(@ModelAttribute meetingDTO: MeetingDTO): Meeting {
        return meetingService.createMeeting(meetingDTO)
    }

    @PutMapping("/{meetingId}")
    fun updateMeeting(@PathVariable meetingId: String, @ModelAttribute meetingDTO: MeetingDTO): Meeting {
        return meetingService.updateMeeting(meetingId, meetingDTO)
    }

    @PostMapping("/schedule")
    fun schedule(): String {
        scheduledMeetingTask.createMeeting()
        return "OK"
    }

    // ✅ THÊM ENDPOINT MISSING
    @PostMapping("/reminder/{id}")
    fun sendMeetingReminder(@PathVariable id: String): String {
        val meeting = meetingService.getMeetingById(id)
        notificationMeetingService.sendMeetingNotification(
            meeting,
            webUtil.getTenantId(),
            webUtil.getUserId()
        )
        return "OK"
    }

    @PostMapping("/export-excel")
    fun exportExcel(@RequestBody request: ExportMeetingRequestDTO): ResponseEntity<Any> {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=meetingScheduled.xlsx")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(meetingService.exportMeeting(request))
    }

    @GetMapping("/summary-month/{year}")
    fun summaryHostMonth(@PathVariable year: Int): ResponseEntity<Any> {
        return ResponseEntity.ok(meetingService.summaryHostMonth(year))
    }

    @GetMapping("/summary-week/{year}")
    fun summaryHostWeek(@PathVariable year: Int): ResponseEntity<Any> {
        return ResponseEntity.ok(meetingService.summaryHostWeek(year))
    }
}