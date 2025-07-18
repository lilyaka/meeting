package com.revotech.meetingservice.business.file_attachment.service

import com.revotech.client.FileServiceClient
import com.revotech.meetingservice.business.file_attachment.model.FileAttachment
import com.revotech.meetingservice.business.file_attachment.model.FileType
import com.revotech.meetingservice.business.file_attachment.repository.FileAttachmentRepository
import com.revotech.util.WebUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile

@Service
class FileAttachmentService(
    private val webUtil: WebUtil,
    private val fileServiceClient: FileServiceClient,
    private val fileAttachmentRepository: FileAttachmentRepository
) {
    private val log = LoggerFactory.getLogger(this.javaClass)
    fun uploadAttachment(
        files: List<MultipartFile>?,
        objectId: String,
        objectType: FileType
    ): List<FileAttachment>? {
        return files?.mapNotNull {
            if (it.isEmpty || it.size<0) return null
            try {
                val filePath = fileServiceClient.upload(
                    webUtil.getHeaders(),
                    it,
                    it.originalFilename,
                    getPath(objectId)
                )

                val fileAttachment = FileAttachment(
                    name = it.originalFilename ?: "",
                    extension = it.originalFilename?.let { s -> StringUtils.getFilenameExtension(s) } ?: "",
                    path = filePath,
                    size = it.size,
                    objectId = objectId,
                    objectType = objectType
                )
                fileAttachmentRepository.save(fileAttachment)
            } catch (e: Exception) {
                log.warn("Fail to upload file ${it.originalFilename}", e)
                null
            }
        }
    }

    fun updateFiles(objectId: String, objectType: FileType, idFilesDelete: List<String>?,
                     files: List<MultipartFile>?){
        if (idFilesDelete?.size!! > 0){
            fileAttachmentRepository.updateIsDeleteFiles(idFilesDelete)
        }

        if (files?.size!! > 0){
            uploadAttachment(files, objectId, objectType)
        }
    }

    private fun getPath(objectId: String): String {
        return "meeting/$objectId"
    }

    fun findById(id: String): List<FileAttachment> {
        return fileAttachmentRepository.findByObjectIdAndIsDeletedFalse(id)
    }

}
