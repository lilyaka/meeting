package com.revotech.meetingservice.business.meeting.graphql.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.revotech.dto.Organization
import com.revotech.meetingservice.business.file_attachment.model.FileAttachment
import com.revotech.meetingservice.business.meeting.MeetingService
import com.revotech.meetingservice.business.meeting.graphql.dataloader.*
import com.revotech.meetingservice.business.meeting.model.Meeting
import com.revotech.meetingservice.business.meeting_attendee.model.MeetingAttendee
import com.revotech.meetingservice.business.meeting_room.model.MeetingRoom
import com.revotech.util.WebUtil
import org.dataloader.DataLoader
import java.time.LocalDate
import java.util.concurrent.CompletableFuture

@DgsComponent
class MeetingDataFetcher(
    private val meetingService: MeetingService,
    private val webUtils: WebUtil
) {
    @DgsQuery
    fun getMeetingById(id: String): Meeting {
        return meetingService.getMeetingById(id)
    }

    @DgsQuery
    fun viewDetail(id: String): Meeting {
        return meetingService.viewDetailMeeting(id)
    }

    @DgsQuery
    fun listMeetingByStartEnd(startDate: LocalDate, endDate: LocalDate, isConfig: Boolean? = false): List<Meeting> {
        return meetingService.listMeetingByStartEnd(startDate, endDate, isConfig)
    }

    @DgsData(parentType = "Meeting")
    fun fileAttachments(dfe: DgsDataFetchingEnvironment): CompletableFuture<List<FileAttachment>> {
        val getAttachment: DataLoader<MeetingKeyLoader, List<FileAttachment>> =
            dfe.getDataLoader(AttachmentDataLoader::class.java)
        val meeting = dfe.getSource<Meeting>()
        return getAttachment.load(MeetingKeyLoader(webUtils.getTenantId(), meeting.id!!))
    }

    @DgsData(parentType = "Meeting")
    fun attendees(dfe: DgsDataFetchingEnvironment): CompletableFuture<List<MeetingAttendee>> {
        val getAttendee: DataLoader<MeetingKeyLoader, List<MeetingAttendee>> =
            dfe.getDataLoader(AttendeeDataLoader::class.java)
        val meeting = dfe.getSource<Meeting>()
        return getAttendee.load(MeetingKeyLoader(webUtils.getTenantId(), meeting.id!!))
    }

    @DgsData(parentType = "Meeting")
    fun meetingRoom(dfe: DgsDataFetchingEnvironment): CompletableFuture<List<MeetingRoom>> {
        val getRoom: DataLoader<MeetingKeyLoader, List<MeetingRoom>> =
            dfe.getDataLoader(MeetingRoomDataLoader::class.java)
        val meeting = dfe.getSource<Meeting>()
        return getRoom.load(MeetingKeyLoader(webUtils.getTenantId(), meeting.roomId!!))
    }

    @DgsData(parentType = "Meeting")
    fun isConflict(dfe: DgsDataFetchingEnvironment): CompletableFuture<Boolean> {
        val getConflict: DataLoader<MeetingKeyLoader, Boolean> =
            dfe.getDataLoader(IsConflictMeetingDataLoader::class.java)
        val meeting = dfe.getSource<Meeting>()
        return getConflict.load(MeetingKeyLoader(webUtils.getTenantId(), meeting.id!!))
    }

    @DgsData(parentType = "Meeting")
    fun conflictMeetings(dfe: DgsDataFetchingEnvironment): CompletableFuture<List<Meeting>> {
        val getConflict: DataLoader<MeetingKeyLoader, List<Meeting>> =
            dfe.getDataLoader(ConflictMeetingDataLoader::class.java)
        val meeting = dfe.getSource<Meeting>()
        return getConflict.load(MeetingKeyLoader(webUtils.getTenantId(), meeting.id!!))
    }

    @DgsData(parentType = "Meeting")
    fun organizations(dfe: DgsDataFetchingEnvironment): CompletableFuture<List<Organization>> {
        val getOrganizations: DataLoader<MeetingKeyLoader, List<Organization>> =
            dfe.getDataLoader(OrganizationsDataLoader::class.java)
        val meeting = dfe.getSource<Meeting>()
        return getOrganizations.load(MeetingKeyLoader(webUtils.getTenantId(),meeting.id!!, meeting.departmentIds!!))
    }

    @DgsQuery
    fun findAllByProjectIdAndIsDeletedFalse(startDate: LocalDate, endDate: LocalDate,projectId: String): List<Meeting> {
        return meetingService.findAllByProjectIdAndIsDeletedFalse(startDate, endDate, projectId)
    }
}
