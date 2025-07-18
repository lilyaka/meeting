package com.revotech.meetingservice.business.meeting.exception

import com.revotech.exception.AppException
import com.revotech.exception.NotFoundException
import com.revotech.exception.ValidateException


open class MeetingException(code: String, message: String) : AppException(code, message)

class MeetingNotFoundException(code: String, message: String) : MeetingException(code, message),
    NotFoundException

class MeetingConflictException(code: String, message: String) : MeetingException(code, message),
    ValidateException

class MeetingParamInvalidException(code: String, message: String) : MeetingException(code, message),
    ValidateException
