package com.revotech.meetingservice.business.meeting

import com.revotech.meetingservice.business.meeting.dto.ExportMeetingDTO
import com.revotech.meetingservice.business.meeting.dto.SummaryMonthHostProjection
import com.revotech.meetingservice.business.meeting.dto.SummaryWeekHostProjection
import com.revotech.meetingservice.business.meeting.model.Meeting
import com.revotech.meetingservice.business.meeting.model.StatusMeetingEnum
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MeetingRepository : JpaRepository<Meeting, String> {

    fun findAllByIsDeletedFalse(): List<Meeting>

    @Query(
        """
        SELECT m FROM Meeting m
         WHERE m.id <> :id
           AND m.isDeleted = false
         AND exists (
            SELECT 1 FROM Meeting m1
             WHERE m1.id = :id
               AND m1.isDeleted = false
               AND (m1.roomId = m.roomId OR m1.hostId = m.hostId)
               AND (m1.startTime between m.startTime and m.endTime OR m1.endTime between m.startTime and m.endTime
                 OR m.startTime between m1.startTime and m1.endTime OR m.endTime between m1.startTime and m1.endTime)
         )
         """
    )
    fun findConflictMeeting(id: String): List<Meeting>

    @Query(
        """
        SELECT count(m) > 0
         FROM Meeting m
         WHERE m.id <> :id
           AND m.isDeleted = false
         AND exists (
            SELECT 1 FROM Meeting m1
             WHERE m1.id = :id
               AND m1.isDeleted = false
               AND (m1.roomId = m.roomId OR m1.hostId = m.hostId)
               AND (m1.startTime between m.startTime and m.endTime OR m1.endTime between m.startTime and m.endTime
                 OR m.startTime between m1.startTime and m1.endTime OR m.endTime between m1.startTime and m1.endTime)
         )
         """
    )
    fun isConflictMeeting(id: String): Boolean

    @Query(
        """
    SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
    FROM Meeting m
    WHERE :startTime BETWEEN m.startTime AND m.endTime
      AND m.roomId = :roomId
      AND m.isDeleted = false
      AND (:isUpdate = false OR (:isUpdate = true AND :meetingId = m.id))
"""
    )
    fun checkOverlapWithoutEndTime(
        @Param("startTime") startTime: LocalDateTime,
        @Param("roomId") roomId: String,
        @Param("meetingId") meetingId: String?,
        @Param("isUpdate") isUpdate: Boolean
    ): Boolean

    @Query(
        """
    SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
    FROM Meeting m
    WHERE m.startTime < :endTime AND m.endTime > :startTime
      AND m.roomId = :roomId
      AND m.isDeleted = false
      AND (:isUpdate = false OR (:isUpdate = true AND :meetingId = m.id))
"""
    )
    fun checkOverlapWithEndTime(
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime,
        @Param("roomId") roomId: String,
        @Param("meetingId") meetingId: String?,
        @Param("isUpdate") isUpdate: Boolean
    ): Boolean


    @Query(
        """
    SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
    FROM Meeting m
    WHERE :startTime BETWEEN m.startTime AND m.endTime
      AND m.hostId = :hostId
      AND m.isDeleted = false
      AND (:isUpdate = false OR (:isUpdate = true AND :meetingId = m.id))
"""
    )
    fun existsOverlapByHostWithoutEndTime(
        @Param("startTime") startTime: LocalDateTime,
        @Param("hostId") hostId: String,
        @Param("meetingId") meetingId: String?,
        @Param("isUpdate") isUpdate: Boolean
    ): Boolean

    @Query(
        """
    SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
    FROM Meeting m
    WHERE m.startTime < :endTime AND m.endTime > :startTime
      AND m.hostId = :hostId
      AND m.isDeleted = false
      AND (:isUpdate = false OR (:isUpdate = true AND :meetingId = m.id))
"""
    )
    fun existsOverlapByHostWithEndTime(
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime,
        @Param("hostId") hostId: String,
        @Param("meetingId") meetingId: String?,
        @Param("isUpdate") isUpdate: Boolean
    ): Boolean


    @Query("select mt from Meeting mt where  :startDate <= mt.startTime and  :endDate >= mt.startTime and mt.isDeleted = false order by mt.startTime")
    fun listMeetingByStartEnd(startDate: LocalDateTime, endDate: LocalDateTime): List<Meeting>

    @Query("select mt from Meeting mt where  :startDate <= mt.startTime and  :endDate >= mt.startTime and mt.isDeleted = false and mt.isPrivate = false and (mt.status = 'APPROVED' or mt.status = 'CANCEL') order by mt.startTime")
    fun listMeetingByStartEndAndStatus(startDate: LocalDateTime, endDate: LocalDateTime): List<Meeting>

    @Query(
        """
      select mt 
        from Meeting mt 
        join MeetingAttendee ma on ma.meetingId = mt.id 
        where  :startDate <= mt.startTime and  :endDate >= mt.startTime and mt.isDeleted = false and mt.isPrivate = true 
        and (mt.status = 'APPROVED' or mt.status = 'CANCEL') 
        and ma.userId = :userId
        order by mt.startTime  
    """
    )
    fun listMeetingByStartEndAndStatusAndUserId(
        userId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<Meeting>


    fun findByIsBaseAndIsDeleted(isBase: Boolean = true, isDeleted: Boolean = false): List<Meeting>

    @Query("select mt from Meeting mt where  :startDate <= mt.startTime and  :endDate >= mt.startTime and mt.isDeleted = false and mt.projectId= :projectId")
    fun findAllByProjectIdAndIsDeletedFalse(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        projectId: String
    ): List<Meeting>

    @Query(
        """select new com.revotech.meetingservice.business.meeting.dto.ExportMeetingDTO(
                m.id,m.content,m.startTime,m.endTime,mr.name,ma.userId,ma.isHost,m.onlineUrl,m.guest,m.setup,m.note,m.status
            )
            from Meeting m left join MeetingRoom mr on m.roomId=mr.id and mr.isDeleted=false 
                left join MeetingAttendee ma on ma.meetingId=m.id and ma.isDeleted=false
            where 
            :startDate <= m.startTime and  :endDate >= m.startTime and m.isDeleted = false and (m.status = 'APPROVED' or m.status = 'CANCEL')
            """
    )
    fun listMeetingByStartEndAndStatusMoreDetail(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<ExportMeetingDTO>


    @Query(
        """
        SELECT *
        FROM crosstab(
             'SELECT host_id, extract(MONTH FROM start_time) AS month, COUNT(*) AS meeting_count
              FROM mt_meeting
              WHERE EXTRACT(YEAR FROM start_time) = '||:year||'
              AND status=$$'||:status||'$$
              AND is_deleted=false
              GROUP BY host_id, extract(MONTH FROM start_time)
              ORDER BY host_id, month',
             'VALUES (1::numeric), (2::numeric), (3::numeric), (4::numeric), (5::numeric), (6::numeric),
                (7::numeric), (8::numeric),(9::numeric),(10::numeric),(11::numeric),(12::numeric)'
             ) AS ct (
                      hostId varchar,
                      month1 bigint,
                      month2 bigint,
                      month3 bigint,
                      month4 bigint,
                      month5 bigint,
                      month6 bigint,
                      month7 bigint,
                      month8 bigint,
                      month9 bigint,
                      month10 bigint,
                      month11 bigint,
                      month12 bigint
            )
    """, nativeQuery = true
    )
    fun summaryHostByMonth(
        year: Int,
        status: String = StatusMeetingEnum.APPROVED.name
    ): List<SummaryMonthHostProjection>

    @Query(
        """
        SELECT *
        FROM crosstab(
             'SELECT host_id, extract(WEEK FROM start_time) AS week, COUNT(*) AS meeting_count
              FROM mt_meeting
              WHERE EXTRACT(ISOYEAR FROM start_time) = '||:year||'
              AND status=$$'||:status||'$$
             AND is_deleted=false
              GROUP BY host_id, extract(WEEK FROM start_time)
              ORDER BY host_id, week',
             'VALUES (1::numeric), (2::numeric), (3::numeric), (4::numeric), (5::numeric), (6::numeric),
                      (7::numeric), (8::numeric), (9::numeric), (10::numeric), (11::numeric), (12::numeric),
                      (13::numeric), (14::numeric), (15::numeric), (16::numeric), (17::numeric), (18::numeric),
                      (19::numeric), (20::numeric), (21::numeric), (22::numeric), (23::numeric), (24::numeric),
                      (25::numeric), (26::numeric), (27::numeric), (28::numeric), (29::numeric), (30::numeric),
                      (31::numeric), (32::numeric), (33::numeric), (34::numeric), (35::numeric), (36::numeric),
                      (37::numeric), (38::numeric), (39::numeric), (40::numeric), (41::numeric), (42::numeric),
                      (43::numeric), (44::numeric), (45::numeric), (46::numeric), (47::numeric), (48::numeric),
                      (49::numeric), (50::numeric), (51::numeric), (52::numeric),(53::numeric)'
     ) AS ct (
              hostId varchar,
              week1 bigint, week2 bigint, week3 bigint, week4 bigint, week5 bigint, week6 bigint,
              week7 bigint, week8 bigint, week9 bigint, week10 bigint, week11 bigint, week12 bigint,
              week13 bigint, week14 bigint, week15 bigint, week16 bigint, week17 bigint, week18 bigint,
              week19 bigint, week20 bigint, week21 bigint, week22 bigint, week23 bigint, week24 bigint,
              week25 bigint, week26 bigint, week27 bigint, week28 bigint, week29 bigint, week30 bigint,
              week31 bigint, week32 bigint, week33 bigint, week34 bigint, week35 bigint, week36 bigint,
              week37 bigint, week38 bigint, week39 bigint, week40 bigint, week41 bigint, week42 bigint,
              week43 bigint, week44 bigint, week45 bigint, week46 bigint, week47 bigint, week48 bigint,
              week49 bigint, week50 bigint, week51 bigint, week52 bigint,week53 bigint);
    """, nativeQuery = true
    )
    fun summaryHostByWeek(year: Int, status: String = StatusMeetingEnum.APPROVED.name): List<SummaryWeekHostProjection>

}
