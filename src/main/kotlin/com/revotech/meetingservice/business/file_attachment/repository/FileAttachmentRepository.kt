package com.revotech.meetingservice.business.file_attachment.repository

import com.revotech.meetingservice.business.file_attachment.model.FileAttachment
import com.revotech.meetingservice.business.file_attachment.model.FileType
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface FileAttachmentRepository : JpaRepository<FileAttachment, String> {
    fun findByObjectIdAndIsDeleted(objectId: String, isDeleted: Boolean): List<FileAttachment>

    fun findByObjectIdAndIsDeletedFalse(objectId: String): List<FileAttachment>

    fun findByObjectIdAndObjectTypeAndIsDeletedFalse(objectId: String, objectType: FileType): List<FileAttachment>

    @Modifying
    @Transactional
    @Query("update FileAttachment fa set fa.isDeleted = true where fa.id in :ids")
    fun updateIsDeleteFiles(ids: List<String?>)
}
