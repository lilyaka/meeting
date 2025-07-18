package com.revotech.meetingservice.business.meeting

import com.revotech.ExcelUtils
import com.revotech.client.AdminServiceClient
import com.revotech.client.WorkServiceClient
import com.revotech.client.ScheduleServiceClient
import com.revotech.dto.Organization
import com.revotech.dto.UserMoreInfoResponse
import com.revotech.meetingservice.business.file_attachment.model.FileType
import com.revotech.meetingservice.business.file_attachment.repository.FileAttachmentRepository
import com.revotech.meetingservice.business.file_attachment.service.FileAttachmentService
import com.revotech.meetingservice.business.meeting.dto.*
import com.revotech.meetingservice.business.meeting.exception.MeetingConflictException
import com.revotech.meetingservice.business.meeting.exception.MeetingException
import com.revotech.meetingservice.business.meeting.model.*
import com.revotech.meetingservice.business.meeting_attendee.MeetingAttendeeRepository
import com.revotech.meetingservice.business.meeting_attendee.MeetingAttendeeService
import com.revotech.meetingservice.event.NotificationMeetingService
import com.revotech.util.WebUtil
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import java.time.*
import java.time.temporal.TemporalAdjusters
import java.util.*

@Service
@Transactional
class MeetingService(
    private val meetingRepository: MeetingRepository,
    private val fileAttachmentService: FileAttachmentService,
    private val fileAttachmentRepository: FileAttachmentRepository,
    private val meetingAttendeeService: MeetingAttendeeService,
    private val attendeeRepository: MeetingAttendeeRepository,
    private val adminServiceClient: AdminServiceClient,
    private val webUtil: WebUtil,
    private val taskScheduler: TaskScheduler,
    private val notificationMeetingService: NotificationMeetingService,
    private val workServiceClient: WorkServiceClient,
    private val scheduleServiceClient: ScheduleServiceClient,
) {

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun createMeeting(meetingDTO: MeetingDTO): Meeting {
        checkConfirmed(meetingDTO)

        val baseMeeting = Meeting(meetingDTO)
        val savedMeeting = meetingRepository.save(baseMeeting)

        savedMeeting.attendees = meetingAttendeeService.createAttendee(meetingDTO, savedMeeting.id!!)
        fileAttachmentService.uploadAttachment(
            meetingDTO.fileAttachments,
            savedMeeting.id!!,
            FileType.DOCUMENTS_PREPARED
        )

        savedMeeting.projectId?.let {
            workServiceClient.createMeetingEvent(webUtil.getHeaders(), savedMeeting.id!!)
        }

        handleRepeatMeetings(meetingDTO)

        return savedMeeting
    }

    private fun handleRepeatMeetings(meetingDTO: MeetingDTO) {
        val repeatType = meetingDTO.repeat
        val meetingsToCreate = mutableListOf<MeetingDTO>()

        val start = meetingDTO.startTime
        val end = meetingDTO.endTime

        val baseDuration = Duration.between(start, end)

        when (repeatType) {
            RepeatMeeting.DAILY_REPEAT -> {
                val endOfWeek = start.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
                var nextStart = start.plusDays(1)
                while (!nextStart.isAfter(endOfWeek)) {
                    meetingsToCreate.add(cloneMeetingDTO(meetingDTO, nextStart, nextStart.plus(baseDuration)))
                    nextStart = nextStart.plusDays(1)
                }
            }

            RepeatMeeting.WEEKLY_REPEAT -> {
                val endOfYear = start.withMonth(12).withDayOfMonth(31)
                var nextStart = start.plusWeeks(1)
                while (!nextStart.isAfter(endOfYear)) {
                    meetingsToCreate.add(cloneMeetingDTO(meetingDTO, nextStart, nextStart.plus(baseDuration)))
                    nextStart = nextStart.plusWeeks(1)
                }
            }

            RepeatMeeting.MONTHLY_REPEAT -> {
                val endOfYear = start.withMonth(12).withDayOfMonth(31)
                var nextStart = start.plusMonths(1)
                while (!nextStart.isAfter(endOfYear)) {
                    meetingsToCreate.add(cloneMeetingDTO(meetingDTO, nextStart, nextStart.plus(baseDuration)))
                    nextStart = nextStart.plusMonths(1)
                }
            }

            else -> return
        }

        meetingsToCreate.forEach {
            val meeting = meetingRepository.save(Meeting(it))
            meetingAttendeeService.createAttendee(it, meeting.id!!)
            fileAttachmentService.uploadAttachment(it.fileAttachments, meeting.id!!, FileType.DOCUMENTS_PREPARED)
            it.projectId?.let { projectId ->
                workServiceClient.createMeetingEvent(webUtil.getHeaders(), meeting.id!!)
            }
        }
    }

    private fun cloneMeetingDTO(original: MeetingDTO, newStart: LocalDateTime, newEnd: LocalDateTime): MeetingDTO {
        return original.copy(
            id = null,
            startTime = newStart,
            endTime = newEnd,
            fileAttachments = null,
            reportFileAttachments = null,
            filesDeleted = emptyList(),
            reportFilesDeleted = emptyList(),
            confirmed = false,
            status = StatusMeetingEnum.DRAFT
        )
    }

    fun getAllMeeting(): List<Meeting> {
        return meetingRepository.findAllByIsDeletedFalse()
    }

    fun updateMeeting(id: String, meetingDTO: MeetingDTO): Meeting {
        val meeting =
            meetingRepository.findById(id).orElseThrow { MeetingException("MeetingNotFound", "Meeting not found") }
        checkConfirmed(meetingDTO, true)
        meetingDTO.filesDeleted?.forEach {
            val file = fileAttachmentRepository.findById(it)
            if (file.isPresent) {
                file.get().isDeleted = true
                fileAttachmentRepository.save(file.get())
            }
        }
        meetingDTO.reportFilesDeleted?.forEach {
            val file = fileAttachmentRepository.findById(it)
            if (file.isPresent) {
                file.get().isDeleted = true
                fileAttachmentRepository.save(file.get())
            }
        }
        fileAttachmentService.uploadAttachment(
            meetingDTO.fileAttachments?.filter { !it.isEmpty },
            id,
            FileType.DOCUMENTS_PREPARED
        )
        fileAttachmentService.uploadAttachment(
            meetingDTO.reportFileAttachments?.filter { !it.isEmpty },
            id,
            FileType.REPORT_DOCUMENTS
        )
        return meetingRepository.save(Meeting(meetingDTO).apply { this.id = id;this.status = meeting.status }).also {
            meetingAttendeeService.updateAttendee(meetingDTO, it.id!!)
        }
    }

    fun getMeetingById(id: String): Meeting {
        return meetingRepository.findById(id).orElseThrow { MeetingException("MeetingNotFound", "Meeting not found") }
    }

    fun viewDetailMeeting(id: String): Meeting {
        val meeting = getMeetingById(id)
        meetingAttendeeService.seen(id, webUtil.getUserId())
        return meeting
    }

    fun updateStatusMeeting(id: String, status: StatusMeetingEnum): Meeting {
        return meetingRepository.findById(id).orElseThrow { MeetingException("MeetingNotFound", "Meeting not found") }
            .apply {
                this.status = status
            }.let {
                if (status == StatusMeetingEnum.APPROVED) {
                    scheduleReminderTask(it)
                }
                meetingRepository.save(it)
            }
    }

    // ✅ NEW: Schedule reminder task thông qua Schedule service
    private fun scheduleReminderTask(meeting: Meeting) {
        val remind = meeting.remind ?: return
        if (remind <= 0) return

        try {
            val reminderTime = when (meeting.remindTimeType) {
                RemindTimeType.MINUTE -> meeting.startTime.minusMinutes(remind.toLong())
                RemindTimeType.HOUR -> meeting.startTime.minusHours(remind.toLong())
                RemindTimeType.DAY -> meeting.startTime.minusDays(remind.toLong())
                else -> return
            }

            // Kiểm tra thời gian nhắc nhở không được trong quá khứ
            if (reminderTime.isBefore(LocalDateTime.now())) {
                log.warn("Reminder time is in the past for meeting ${meeting.id}")
                return
            }

            // Tạo cron expression cho thời điểm nhắc nhở
            val cronExpression = createCronExpression(reminderTime)

            val taskPayload = ServiceTaskPayload(
                taskType = "MEETING_REMINDER_${meeting.id}",
                service = "meeting-service",
                endpoint = "meeting/reminder/${meeting.id}",
                cronExpression = cronExpression
            )

            scheduleServiceClient.registerServiceTask(taskPayload)

            log.info("Scheduled reminder for meeting ${meeting.id} at $reminderTime")

        } catch (e: Exception) {
            log.warn("Failed to schedule reminder task for meeting ${meeting.id}", e)
        }
    }

    // ✅ NEW: Tạo cron expression từ LocalDateTime
    private fun createCronExpression(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()

        log.info("=== CRON CREATION DEBUG ===")
        log.info("Target time: $dateTime")
        log.info("Current time: $now")
        log.info("System timezone: ${ZoneId.systemDefault()}")

        // Validation
        if (dateTime.year != now.year) {
            log.warn("⚠️ Different year: target=${dateTime.year}, current=${now.year}")
        }

        if (dateTime.isBefore(now)) {
            log.warn("⚠️ Time in past: $dateTime")
        }

        val cron = "${dateTime.second} ${dateTime.minute} ${dateTime.hour} ${dateTime.dayOfMonth} ${dateTime.monthValue} ?"
        log.info("Generated cron: $cron")

        // ✅ TEST CRON EXPRESSION
        try {
            val cronExpr = CronExpression.parse(cron)
            val nextRun = cronExpr.next(now)
            log.info("✅ Next execution: $nextRun")

            if (nextRun == null) {
                log.error("❌ Cron will never execute!")
            }
        } catch (e: Exception) {
            log.error("❌ Invalid cron expression: $cron", e)
        }

        return cron
    }

    // ✅ NEW: Method để Schedule service gọi để gửi reminder
    fun sendMeetingReminder(meetingId: String) {
        try {
            val meeting = meetingRepository.findById(meetingId).orElse(null) ?: return

            if (meeting.status == StatusMeetingEnum.APPROVED) {
                notificationMeetingService.sendMeetingNotification(
                    meeting,
                    webUtil.getTenantId(),
                    meeting.hostId
                )
            }
        } catch (e: Exception) {
            log.error("Failed to send meeting reminder for meeting $meetingId", e)
        }
    }

    private fun sendNotiBeforeMeeting(id: String) {
        val meeting = meetingRepository.findById(id).get()

        val remind = meeting.remind ?: return
        val startTime = when (meeting.remindTimeType) {
            RemindTimeType.MINUTE -> meeting.startTime.minusMinutes(remind.toLong())
            RemindTimeType.HOUR -> meeting.startTime.minusHours(remind.toLong())
            RemindTimeType.DAY -> meeting.startTime.minusDays(remind.toLong())
            else -> return
        }

        taskScheduler.schedule(
            notificationMeetingService.createNotificationRunnable(
                meeting,
                webUtil.getTenantId(),
                webUtil.getUserId()
            ), startTime.atZone(ZoneId.systemDefault()).toInstant()
        )
    }

    fun findConflictMeeting(roomId: String): List<Meeting> {
        return meetingRepository.findConflictMeeting(roomId)
    }

    fun isConflictMeeting(roomId: String): Boolean {
        return meetingRepository.isConflictMeeting(roomId)
    }

    fun listMeetingByStartEnd(startDate: LocalDate, endDate: LocalDate, isConfig: Boolean?): List<Meeting> {
        val startDateTime = startDate.atStartOfDay()
        val endDateTime = endDate.atTime(23, 59)

        return if (isConfig == true) {
            meetingRepository.listMeetingByStartEnd(startDateTime, endDateTime)
        } else {
            val publicMeetings = meetingRepository.listMeetingByStartEndAndStatus(startDateTime, endDateTime)
            val privateMeetings = meetingRepository.listMeetingByStartEndAndStatusAndUserId(
                webUtil.getUserId(),
                startDateTime,
                endDateTime
            )
            (publicMeetings + privateMeetings).sortedBy { it.startTime }
        }
    }

    fun listMeetingByStartEndAndStatusMoreDetail(startDate: LocalDate, endDate: LocalDate): List<ExportMeetingDTO> {
        return meetingRepository.listMeetingByStartEndAndStatusMoreDetail(
            startDate.atStartOfDay(),
            endDate.atTime(23, 59)
        )
    }

    fun exportMeeting(request: ExportMeetingRequestDTO): ByteArray {
        val numDay = if (request.type == ExportType.WEEKLY) 6 else {
            request.endDate.lengthOfMonth() - 1
        }
        val data: MutableMap<String, Any?> = mutableMapOf()
        val listData: List<ExportMeetingDTO> =
            listMeetingByStartEndAndStatusMoreDetail(request.startDate, request.endDate)
        val setUserId: Set<String> = listData.mapNotNull { it.attendeeId }.toSet()
        val users: Map<String?, UserMoreInfoResponse> = getUsersCache(setUserId).associateBy { it.id }
        listData.forEach {
            it.attendeeName = it.attendeeId?.run { getUserCache(this) }?.fullName
        }
        val groupByDay: MutableMap<LocalDate, MutableList<ExportMeetingDTO>> =
            listData.groupByTo(mutableMapOf()) { it.startTime.toLocalDate() }
        (0..numDay).forEach { i ->
            groupByDay.computeIfAbsent(request.startDate.plusDays(i.toLong())) { mutableListOf(ExportMeetingDTO()) }
        }
        val sortedGroupByDay: Map<LocalDate, List<ExportMeetingFinalDTO>> =
            TreeMap(groupByDay).mapValues { entry ->
                entry.value.groupBy { it.meetingId }.values.map { meeting ->
                    ExportMeetingFinalDTO(meeting.map {
                        it.apply {
                            attendeeName = it.attendeeId?.let { id -> users[id]?.fullName }
                        }
                    })
                }.sortedWith(
                    compareBy<ExportMeetingFinalDTO> { it.startTime }
                        .thenBy { it.endTime }
                )
            }
        data["list"] = sortedGroupByDay
        data["startDate"] = request.startDate
        data["endDate"] = request.endDate

        data["tenant"] = "Tenant"
        data["address"] = "Address"
        return ExcelUtils.exportExcelFixMerge("meeting/meeting-scheduled.xlsx", data)
    }

    private fun checkConfirmed(meetingDTO: MeetingDTO, isUpdate: Boolean = false) {
        if (!meetingDTO.confirmed) {
            if (meetingDTO.endTime == null) {
                meetingRepository.checkOverlapWithoutEndTime(
                    meetingDTO.startTime,
                    meetingDTO.roomId,
                    meetingDTO.id,
                    isUpdate
                ).run {
                    if (this) {
                        throw MeetingConflictException("MeetingOverlapTime", "Meeting overlap time")
                    }
                }
            } else {
                meetingRepository.checkOverlapWithEndTime(
                    meetingDTO.startTime,
                    meetingDTO.endTime!!,
                    meetingDTO.roomId,
                    meetingDTO.id,
                    isUpdate
                ).run {
                    if (this) {
                        throw MeetingConflictException("MeetingOverlapTime", "Meeting overlap time")
                    }
                }
            }
            if (meetingDTO.meetingType === MeetingType.OFFLINE) {

                if (meetingDTO.endTime == null) {
                    meetingRepository.existsOverlapByHostWithoutEndTime(
                        meetingDTO.startTime,
                        meetingDTO.roomId,
                        meetingDTO.id,
                        isUpdate
                    ).run {
                        if (this) {
                            throw MeetingConflictException("MeetingOverlapTime", "Meeting overlap time")
                        }
                    }
                } else {
                    meetingRepository.existsOverlapByHostWithEndTime(
                        meetingDTO.startTime,
                        meetingDTO.endTime!!,
                        meetingDTO.roomId,
                        meetingDTO.id,
                        isUpdate
                    ).run {
                        if (this) {
                            throw MeetingConflictException("MeetingConflictHost", "Meeting conflict host")
                        }
                    }
                }
            }
        }
    }

    fun deleteMeeting(id: String) {
        val meeting =
            meetingRepository.findById(id).orElseThrow { MeetingException("MeetingNotFound", "Meeting not found") }
        meeting.isDeleted = true
        meetingRepository.save(meeting)

        fileAttachmentRepository.findByObjectIdAndIsDeleted(id, false).forEach {
            it.isDeleted = true
            fileAttachmentRepository.save(it)
        }

        attendeeRepository.findByMeetingIdAndIsDeleted(id, false).forEach {
            it.isDeleted = true
            attendeeRepository.save(it)
        }
    }

    fun getOrganizations(tenantId: String, organizationCodes: List<String>?): List<Organization> {
        if (organizationCodes?.size!! > 0) {
            val header = mutableMapOf(webUtil.tenantHeaderKey to tenantId)
            return adminServiceClient.getOrganizationByCodes(header, organizationCodes)
        }
        return listOf()
    }

    private fun getUserCache(userId: String): UserMoreInfoResponse? {
        try {
            val header = mutableMapOf(webUtil.tenantHeaderKey to webUtil.getTenantId())
            return adminServiceClient.getUserCache(header, userId)
        } catch (e: Exception) {
            log.error(e.message, e)
            return null
        }
    }

    private fun getUsersCache(userIds: Collection<String>): List<UserMoreInfoResponse> {
        try {
            val header = mutableMapOf(webUtil.tenantHeaderKey to webUtil.getTenantId())
            return adminServiceClient.getUsersCache(header, userIds)
        } catch (e: Exception) {
            log.error(e.message, e)
            return emptyList()
        }
    }

    fun summaryHostMonth(year: Int): List<SummaryMonthHostProjection> {
        return meetingRepository.summaryHostByMonth(year)
    }

    fun summaryHostWeek(year: Int): List<SummaryWeekHostProjection> {
        return meetingRepository.summaryHostByWeek(year)
    }

    fun findAllByProjectIdAndIsDeletedFalse(
        startDate: LocalDate,
        endDate: LocalDate,
        projectId: String
    ): List<Meeting> {
        return meetingRepository.findAllByProjectIdAndIsDeletedFalse(
            startDate.atStartOfDay(),
            endDate.atTime(23, 59),
            projectId
        )
    }
}