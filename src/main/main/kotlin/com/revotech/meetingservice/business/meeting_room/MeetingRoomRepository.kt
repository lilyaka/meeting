package com.revotech.meetingservice.business.meeting_room

import com.revotech.meetingservice.business.meeting_room.model.MeetingRoom
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MeetingRoomRepository: JpaRepository<MeetingRoom, String> {

    @Query(
        value = """
        SELECT r FROM MeetingRoom r
         WHERE (:keyword is null or r.name LIKE %:keyword%)
         AND r.isDeleted = false 
    """
    )
    fun searchText(keyword: String?, pageable: Pageable): Page<MeetingRoom>

    @Query(value = """
        select max(mr.norder) from MeetingRoom mr where mr.isDeleted = false
    """)
    fun getMaxNOrder(): Int?

    @Query("SELECT m FROM MeetingRoom m " +
            "WHERE (m.status = :status)" +
            "AND m.isDeleted = false")
    fun findAllActive(@Param("status") status: MeetingRoomStatus): List<MeetingRoom>
}