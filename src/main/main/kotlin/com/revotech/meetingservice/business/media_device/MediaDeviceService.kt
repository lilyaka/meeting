package com.revotech.meetingservice.business.media_device

import com.revotech.meetingservice.business.file_attachment.model.FileAttachment
import com.revotech.meetingservice.business.file_attachment.model.FileType
import com.revotech.meetingservice.business.file_attachment.repository.FileAttachmentRepository
import com.revotech.meetingservice.business.file_attachment.service.FileAttachmentService
import com.revotech.meetingservice.business.media_device.dto.MediaDeviceInput
import com.revotech.meetingservice.business.media_device.exception.MediaDeviceException
import com.revotech.meetingservice.business.meeting_room.MeetingRoomStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class MediaDeviceService(
    private val mediaDeviceRepository: MediaDeviceRepository,
    protected val fileAttachmentService: FileAttachmentService,
    private val fileAttachmentRepository: FileAttachmentRepository
) {
    fun createMediaDevice(
        mediaDeviceInput: MediaDeviceInput
    ): MediaDevice {
        //TODO: write other save images method
        val mediaDevice: MediaDevice
        if (mediaDeviceInput.id == null) {
            mediaDevice = mediaDeviceRepository.save(mediaDeviceInput.toMediaDevice())
            insertFileAttach(mediaDeviceInput, mediaDevice, true)
        } else {
            mediaDevice = mediaDeviceRepository.findById(mediaDeviceInput.id!!).orElseThrow {
                MediaDeviceException("MediaDeviceRotRound", "Meeting room not found")
            }
                .apply {
                    name = mediaDeviceInput.name
                    description = mediaDeviceInput.description
                    status = mediaDeviceInput.status
                    norder = mediaDeviceInput.norder
                    mediaOrDevice = mediaDeviceInput.mediaOrDevice
                }.let { mediaDeviceRepository.save(it) }

            insertFileAttach(mediaDeviceInput, mediaDevice, false)
        }
        return mediaDevice
    }

    fun insertFileAttach(mediaDeviceInput: MediaDeviceInput, mediaDevice: MediaDevice, isCreate: Boolean) {
        if (!isCreate) {
            fileAttachmentService.updateFiles(
                mediaDevice.id!!,
                FileType.ROOM_PHOTO,
                mediaDeviceInput.idFilesDelete,
                mediaDeviceInput.images
            )
        } else {
            fileAttachmentService.uploadAttachment(
                mediaDeviceInput.images,
                mediaDevice.id!!,
                FileType.ROOM_PHOTO
            )
        }
    }

    fun getMediaDeviceById(mediaDeviceId: String): MediaDevice {
        val mediaDevice = mediaDeviceRepository.findById(mediaDeviceId)
            .orElseThrow { MediaDeviceException("MediaDeviceNotFound", "Meeting Room not found") }
        val images =
            fileAttachmentRepository.findByObjectIdAndObjectTypeAndIsDeletedFalse(mediaDevice.id!!, FileType.ROOM_PHOTO)
                .map {
                    FileAttachment(
                        id = it.id, name = it.name, objectType = it.objectType, size = it.size,
                        path = it.path, extension = it.extension, objectId = it.objectId
                    )
                }
        mediaDevice.images = images
        return mediaDevice
    }

    fun getAllActiveMediaDevices(): List<MediaDevice> {
        return mediaDeviceRepository.findAllActive(MeetingRoomStatus.ACTIVE)
    }

    fun searchByKeyword(keyword: String?, pageable: Pageable): Page<MediaDevice> {
        return mediaDeviceRepository.searchText(keyword, pageable)
    }

    fun maxNOrder(): Int {
        return (mediaDeviceRepository.getMaxNOrder() ?: 0) + 1
    }

    fun deleteMediaDevice(id: String) {
        val mediaDevice = mediaDeviceRepository.findById(id).orElseThrow {
            MediaDeviceException("MediaDeviceNotFound", "Meeting Room not found")
        }
        mediaDevice.isDeleted = true
        mediaDeviceRepository.save(mediaDevice)
    }

    fun findMediaDeviceById(id: String): MediaDevice {
        return mediaDeviceRepository.findById(id)
            .orElse(MediaDevice(null, null, null, false, MeetingRoomStatus.ACTIVE, false))
    }
}