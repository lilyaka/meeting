package com.revotech.meetingservice.business.media_device

import com.revotech.meetingservice.business.media_device.dto.MediaDeviceInput
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/media-device")
class MediaDeviceController(
    private val mediaDeviceService: MediaDeviceService
) {
    @PostMapping
    fun createMediaDevice(
        @ModelAttribute mediaDeviceInput: MediaDeviceInput
    ): MediaDevice {
        return mediaDeviceService.createMediaDevice(mediaDeviceInput)
    }

    @PutMapping
    fun updateMediaDevice(
        @ModelAttribute mediaDeviceInput: MediaDeviceInput
    ): MediaDevice {
        return mediaDeviceService.createMediaDevice(mediaDeviceInput)
    }
}
