package com.revotech.meetingservice.business.file_attachment.model

import com.revotech.audit.ActivityInfo
import jakarta.persistence.*

@Entity
@Table(name = "mt_file_attachment")
class FileAttachment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    var objectId: String,
    @Enumerated(value = EnumType.STRING)
    var objectType: FileType,
    var extension: String,
    var name: String,
    var path: String,
    var size: Long,
    var isDeleted: Boolean? = false,
) : ActivityInfo()

enum class FileType {
    ROOM_PHOTO,
    DOCUMENTS_PREPARED,
    REPORT_DOCUMENTS
}