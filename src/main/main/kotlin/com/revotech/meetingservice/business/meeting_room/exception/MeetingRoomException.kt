package com.revotech.meetingservice.business.meeting_room.exception

import com.revotech.exception.AppException


open class MeetingRoomException(code: String, message: String) : AppException(code, message)

