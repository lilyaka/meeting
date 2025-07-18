package com.revotech.meetingservice.business.meeting.graphql.dataloader

data class MeetingKeyLoader(
    val tenantId: String,
    val id: String,
    var organizationCodes : List<String>? = listOf()
)
