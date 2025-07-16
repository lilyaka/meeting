package com.revotech.meetingservice.business.flow

import com.revotech.client.Endpoint
import com.revotech.client.FlowRegister
import com.revotech.client.FlowServiceClient
import com.revotech.meetingservice.business.meeting.MEETING_ENDPOINT
import com.revotech.meetingservice.business.meeting.model.Meeting
import com.revotech.meetingservice.business.meeting.model.StatusMeetingEnum
import com.revotech.util.WebUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class FlowRegister(
    private val flowServiceClient: FlowServiceClient,
    private val webUtil: WebUtil,
) {

    @Value("\${spring.application.name}")
    private var service: String = ""

    private val log = LoggerFactory.getLogger(this::class.java)

    @EventListener(classes = [ApplicationReadyEvent::class])
    fun register() {
        try {
            flowServiceClient.register(
                mapOf(webUtil.tenantHeaderKey to "master"),
                FlowRegister(
                    app = "MEETING",
                    type = Meeting::class.java.name,
                    name = "Lịch biểu",
                    statuses = StatusMeetingEnum.entries.map { it.name },
                    service = service,
                    endpoint = Endpoint(
                        getObjectUrl = "$MEETING_ENDPOINT/{id}",
                        listUrs = MEETING_ENDPOINT,
                        changeStatusUrl = "$MEETING_ENDPOINT/{id}/status",
                    )
                )
            )
        } catch (e: Exception) {
            log.warn("Failed to register to flow service")
            log.warn(e.message)
        }
    }

}