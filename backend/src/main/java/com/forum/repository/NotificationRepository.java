package com.forum.repository;

import com.forum.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByTimestampDesc(Long userId);
    List<Notification> findByRecipientIdAndIsReadFalseOrderByTimestampDesc(Long userId);
}
