package com.revotech.meetingservice.business.meeting.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
interface SummaryMonthHostProjection {
    val hostId: String
    val month1: Long?
    val month2: Long?
    val month3: Long?
    val month4: Long?
    val month5: Long?
    val month6: Long?
    val month7: Long?
    val month8: Long?
    val month9: Long?
    val month10: Long?
    val month11: Long?
    val month12: Long?
}