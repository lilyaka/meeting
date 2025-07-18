package com.revotech.meetingservice.business.media_device.graphql.mutation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.revotech.meetingservice.business.media_device.MediaDeviceService

@DgsComponent
class MediaDeviceMutation(
    private val mediaDeviceService: MediaDeviceService
) {
    @DgsMutation
    fun deleteMediaDevice(id: String): Boolean {
        mediaDeviceService.deleteMediaDevice(id)
        return true
    }
}