package com.revotech.meetingservice.business.meeting.graphql.dataloader

import com.netflix.graphql.dgs.DgsDataLoader
import com.revotech.meetingservice.business.file_attachment.model.FileAttachment
import com.revotech.meetingservice.business.file_attachment.service.FileAttachmentService
import com.revotech.util.WebUtil
import org.dataloader.MappedBatchLoader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@DgsDataLoader(name = "fileAttachments")
class AttachmentDataLoader(private val fileAttachmentService: FileAttachmentService, private val webUtil: WebUtil) :
    MappedBatchLoader<MeetingKeyLoader, List<FileAttachment>> {
    override fun load(keys: MutableSet<MeetingKeyLoader>): CompletionStage<MutableMap<MeetingKeyLoader, List<FileAttachment>>> {
        return CompletableFuture.supplyAsync {
            keys.associateWith { key ->
                webUtil.changeTenant(key.tenantId) {
                    fileAttachmentService.findById(key.id)
                }
            }.toMutableMap()
        }
    }
}
