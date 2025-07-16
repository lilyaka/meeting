package com.revotech.meetingservice.business.meeting_room

import com.revotech.meetingservice.business.file_attachment.model.FileAttachment
import com.revotech.meetingservice.business.file_attachment.model.FileType
import com.revotech.meetingservice.business.file_attachment.repository.FileAttachmentRepository
import com.revotech.meetingservice.business.file_attachment.service.FileAttachmentService
import com.revotech.meetingservice.business.meeting_room.exception.MeetingRoomException
import com.revotech.meetingservice.business.meeting_room.graphql.type.MeetingRoomInput
import com.revotech.meetingservice.business.meeting_room.model.MeetingRoom
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
@Transactional
class MeetingRoomService(
    private val meetingRoomRepository: MeetingRoomRepository,
    protected val fileAttachmentService: FileAttachmentService,
    private val fileAttachmentRepository: FileAttachmentRepository
) {

    fun createMeetingRoom(
        meetingRoomInput: MeetingRoomInput
    ): MeetingRoom {
        //TODO: write other save images method
        val meetingRoom: MeetingRoom
        if (meetingRoomInput.id == null) {
            meetingRoom = meetingRoomRepository.save(meetingRoomInput.toMeetingRoom())
            insertFileAttach(meetingRoomInput, meetingRoom, true)
        } else {
            meetingRoom = meetingRoomRepository.findById(meetingRoomInput.id!!).orElseThrow {
                MeetingRoomException("MeetingRoomRotRound", "Meeting room not found")
            }
                .apply {
                    name = meetingRoomInput.name
                    address = meetingRoomInput.address
                    status = meetingRoomInput.status
                    norder = meetingRoomInput.norder
                }.let { meetingRoomRepository.save(it) }

            insertFileAttach(meetingRoomInput, meetingRoom, false)
        }
        return meetingRoom
    }

    fun insertFileAttach(meetingRoomInput: MeetingRoomInput, meetingRoom: MeetingRoom, isCreate: Boolean) {
        if (!isCreate) {
            fileAttachmentService.updateFiles(
                meetingRoom.id!!,
                FileType.ROOM_PHOTO,
                meetingRoomInput.idFilesDelete,
                meetingRoomInput.images
            )
        } else {
            fileAttachmentService.uploadAttachment(
                meetingRoomInput.images,
                meetingRoom.id!!,
                FileType.ROOM_PHOTO
            )
        }
    }

    fun getMeetingRoomById(meetingRoomId: String): MeetingRoom {
        val meetingRoom = meetingRoomRepository.findById(meetingRoomId)
            .orElseThrow { MeetingRoomException("MeetingRoomNotFound", "Meeting Room not found") }
        val images =
            fileAttachmentRepository.findByObjectIdAndObjectTypeAndIsDeletedFalse(meetingRoom.id!!, FileType.ROOM_PHOTO)
                .map {
                    FileAttachment(
                        id = it.id, name = it.name, objectType = it.objectType, size = it.size,
                        path = it.path, extension = it.extension, objectId = it.objectId
                    )
                }
        meetingRoom.images = images
        return meetingRoom
    }

    fun getAllActiveMeetingRooms(): List<MeetingRoom> {
        return meetingRoomRepository.findAllActive(MeetingRoomStatus.ACTIVE)
    }

    fun searchByKeyword(keyword: String?, pageable: Pageable): Page<MeetingRoom> {
        return meetingRoomRepository.searchText(keyword, pageable)
    }

    fun maxNOrder(): Int {
        return (meetingRoomRepository.getMaxNOrder() ?: 0) + 1
    }

    fun deleteMeetingRoom(id: String) {
        val meetingRoom = meetingRoomRepository.findById(id).orElseThrow {
            MeetingRoomException("MeetingRoomNotFound", "Meeting Room not found")
        }
        meetingRoom.isDeleted = true
        meetingRoomRepository.save(meetingRoom)
    }

    fun findMeetingRoomById(id: String): MeetingRoom {
        return meetingRoomRepository.findById(id).orElse(MeetingRoom(null, null, null, null, false, null))
    }
}
