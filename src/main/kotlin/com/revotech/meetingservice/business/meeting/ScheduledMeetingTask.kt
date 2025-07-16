package com.revotech.meetingservice.business.meeting

import com.revotech.client.ScheduleServiceClient
import com.revotech.config.multitenant.db_config.DatabaseConfig
import com.revotech.meetingservice.business.meeting.model.Meeting
import com.revotech.meetingservice.business.meeting.model.ServiceTaskPayload
import com.revotech.util.WebUtil
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Service
class ScheduledMeetingTask(
    private val meetingRepository: MeetingRepository,
    private val scheduleServiceClient: ScheduleServiceClient,
    private val databaseConfig: DatabaseConfig,
    private val webUtil: WebUtil,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun registerScheduleServiceTask() {
        try {
            scheduleServiceClient.registerServiceTask(
                ServiceTaskPayload(
                    taskType = "MEETING_SCHEDULE",
                    service = "meeting-service",
                    endpoint = "/meeting/schedule",
                    cronExpression = "0 0 0 * * *"
                )
            )
        } catch (e: Exception) {
            log.warn("Failed to register schedule service task", e)
        }
    }

    fun createMeeting() {
        databaseConfig.list.forEach {
            it.name?.let { tenantId ->
                webUtil.changeTenant(tenantId) {
                    meetingRepository.findByIsBaseAndIsDeleted().forEach { meeting: Meeting ->
                        log.info(meeting.content)
                        log.info(meeting.cronConfig)

                        shouldCreateMeeting(meeting.cronConfig)?.let {
                            log.info("save")
                            meetingRepository.save(meeting.apply {
                                val diffTime = Duration.between(startTime, endTime)
                                val exactTime =
                                    LocalDateTime.of(it.toLocalDate(), LocalTime.of(startTime.hour, startTime.minute))
                                id = null
                                startTime = exactTime
                                endTime = exactTime.plus(diffTime)
                                isBase = false
                            })
                        }
                    }
                }
            }
        }
    }

    private fun shouldCreateMeeting(cronString: String?): LocalDateTime? {
        //TODO: get schedule date from config
        val scheduleDate = LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.DAYS)
        log.info("scheduleDate: $scheduleDate")
        return cronString?.let {
            CronExpression.parse(it)
                .next(scheduleDate.minusSeconds(1))?.run {
                    log.info(toLocalDate().toString())
                    if (toLocalDate() == scheduleDate.toLocalDate()) this
                    else null
                }
        }
    }
}
