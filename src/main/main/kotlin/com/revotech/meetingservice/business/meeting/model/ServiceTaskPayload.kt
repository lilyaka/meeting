package com.revotech.meetingservice.business.meeting.model

import com.revotech.dto.IServiceTaskPayload

data class ServiceTaskPayload(
    val taskType: String,
    val service: String,
    val endpoint: String,
    val cronExpression: String,
) : IServiceTaskPayload