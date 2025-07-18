package com.revotech.meetingservice.business.meeting.dto

import java.time.LocalDateTime

interface BaseProjection {
    fun getCreatedTime(): LocalDateTime?
    fun getCreatedBy(): String?
    fun getLastModifiedTime(): LocalDateTime?
    fun getLastModifiedBy(): String?
}