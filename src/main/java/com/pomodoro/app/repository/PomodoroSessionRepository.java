package com.pomodoro.app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.pomodoro.app.model.PomodoroSession;
import com.pomodoro.app.model.User;

@Repository
public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {
    List<PomodoroSession> findByUser(User user);
    
    List<PomodoroSession> findByUserAndCompletedTrue(User user);
    
    List<PomodoroSession> findByUserAndStartTimeBetween(User user, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(p) FROM PomodoroSession p WHERE p.user = ?1 AND p.completed = true AND p.sessionType = 'FOCUS' AND p.startTime BETWEEN ?2 AND ?3")
    Long countCompletedFocusSessionsByUserAndDateRange(User user, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT p FROM PomodoroSession p WHERE p.user = ?1 ORDER BY p.startTime DESC")
    List<PomodoroSession> findRecentSessionsByUser(User user);
}