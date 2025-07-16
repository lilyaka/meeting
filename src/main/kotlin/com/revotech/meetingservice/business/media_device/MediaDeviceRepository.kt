package com.revotech.meetingservice.business.media_device

import com.revotech.meetingservice.business.meeting_room.MeetingRoomStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MediaDeviceRepository : JpaRepository<MediaDevice, String> {
    @Query(
        value = """
        SELECT r FROM MediaDevice r
         WHERE (:keyword is null or r.name LIKE %:keyword%)
         AND r.isDeleted = false 
    """
    )
    fun searchText(keyword: String?, pageable: Pageable): Page<MediaDevice>

    @Query(
        value = """
        select max(mr.norder) from MediaDevice mr where mr.isDeleted = false
    """
    )
    fun getMaxNOrder(): Int?

    @Query(
        "SELECT m FROM MediaDevice m " +
                "WHERE (m.status = :status)" +
                "AND m.isDeleted = false"
    )
    fun findAllActive(@Param("status") status: MeetingRoomStatus): List<MediaDevice>
}