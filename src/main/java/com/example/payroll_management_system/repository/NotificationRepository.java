package com.example.payroll_management_system.repository;

import com.example.payroll_management_system.dto.responseDto.NotificationRespDto;
import com.example.payroll_management_system.model.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // isRead is optional - null gives read and unread notifications
    @Query("""
        select new com.example.payroll_management_system.dto.responseDto.NotificationRespDto(
        n.id,
        n.title,
        n.type,
        n.isRead,
        n.createdAt
        )
        from Notification n
        JOIN n.user u
        where u.id = ?1 AND (?2 IS NULL OR n.isRead = ?2)
        order by n.createdAt desc
        """)
    List<NotificationRespDto> getNotificationByUserId(Long userId, Boolean isRead, Pageable pageable);
}
