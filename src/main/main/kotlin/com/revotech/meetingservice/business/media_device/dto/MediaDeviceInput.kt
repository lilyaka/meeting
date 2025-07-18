package com.revotech.meetingservice.business.media_device.dto

import com.revotech.audit.JpaActivityInfo
import com.revotech.meetingservice.business.media_device.MediaDevice
import com.revotech.meetingservice.business.meeting_room.MeetingRoomStatus
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import org.springframework.web.multipart.MultipartFile

class MediaDeviceInput(
    var id: String?,
    var name: String?,
    var description: String?,
    var mediaOrDevice: Boolean,
    @Enumerated(EnumType.STRING)
    var status: MeetingRoomStatus?,
    var norder: Int?,
    val idFilesDelete: List<String>?,
    val images: List<MultipartFile>?
) : JpaActivityInfo() {
    fun toMediaDevice(): MediaDevice {
        return MediaDevice(
            id = this.id,
            name = this.name,
            mediaOrDevice = this.mediaOrDevice,
            description = this.description,
            status = this.status,
            norder = this.norder
        )
    }
}

