package com.revotech.meetingservice.business.media_device.graphql.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.revotech.graphql.GraphqlUtil
import com.revotech.graphql.type.CustomPageable
import com.revotech.meetingservice.business.media_device.MediaDevice
import com.revotech.meetingservice.business.media_device.MediaDeviceService
import graphql.relay.Connection

@DgsComponent
class MediaDeviceDataFetcher(
    private val mediaDeviceService: MediaDeviceService
) {
    @DgsQuery
    fun getMediaDeviceById(id: String): MediaDevice {
        return mediaDeviceService.getMediaDeviceById(id)
    }

    @DgsQuery
    fun getAllActiveMediaDevices(): List<MediaDevice> {
        return mediaDeviceService.getAllActiveMediaDevices()
    }

    @DgsQuery
    fun searchTextMediaDevice(
        keyword: String?,
        pageable: CustomPageable?,
    ): Connection<out MediaDevice> {
        val page = GraphqlUtil.toPageable(pageable)
        val mediaDevices = mediaDeviceService.searchByKeyword(keyword, page)
        return GraphqlUtil.createConnection(mediaDevices)
    }

    @DgsQuery
    fun maxNOrderMediaDevice(): Int {
        return mediaDeviceService.maxNOrder()
    }
}