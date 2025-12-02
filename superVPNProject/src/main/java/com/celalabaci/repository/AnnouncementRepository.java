package com.celalabaci.repository;

import com.celalabaci.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    // Sadece görünür olan duyuruları getirmek için bir metot
    List<Announcement> findByIsVisibleTrue();
}