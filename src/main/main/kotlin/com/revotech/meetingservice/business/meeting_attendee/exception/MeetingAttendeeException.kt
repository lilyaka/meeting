package com.revotech.meetingservice.business.meeting_attendee.exception


import com.revotech.exception.AppException
import com.revotech.exception.NotFoundException
import com.revotech.exception.ValidateException
import com.revotech.meetingservice.business.meeting.exception.MeetingException

open class MeetingAttendeeException(code: String, message: String) : AppException(code, message)

class MeetingAttendeeNotFoundException(code: String, message: String) : MeetingException(code, message),
    NotFoundException

class MeetingAttendeeParamInvalidException(code: String, message: String) : MeetingException(code, message),
    ValidateException
