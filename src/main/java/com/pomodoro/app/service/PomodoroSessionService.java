package com.pomodoro.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pomodoro.app.model.PomodoroSession;
import com.pomodoro.app.model.User;
import com.pomodoro.app.repository.PomodoroSessionRepository;

@Service
public class PomodoroSessionService {

    @Autowired
    private PomodoroSessionRepository sessionRepository;

    public PomodoroSession startSession(User user, PomodoroSession.SessionType sessionType, String taskDescription) {
        PomodoroSession session = new PomodoroSession();
        session.setUser(user);
        session.setSessionType(sessionType);
        session.setStartTime(LocalDateTime.now());
        session.setTaskDescription(taskDescription);
        
        // Set duration based on session type and user preferences
        if (sessionType == PomodoroSession.SessionType.FOCUS) {
            session.setDurationMinutes(user.getFocusDuration());
        } else if (sessionType == PomodoroSession.SessionType.SHORT_BREAK) {
            session.setDurationMinutes(user.getBreakDuration());
        } else if (sessionType == PomodoroSession.SessionType.LONG_BREAK) {
            session.setDurationMinutes(user.getLongBreakDuration());
        }
        
        return sessionRepository.save(session);
    }

    public PomodoroSession completeSession(Long sessionId) {
        Optional<PomodoroSession> optionalSession = sessionRepository.findById(sessionId);
        if (optionalSession.isPresent()) {
            PomodoroSession session = optionalSession.get();
            session.setCompleted(true);
            session.setEndTime(LocalDateTime.now());
            return sessionRepository.save(session);
        }
        throw new IllegalArgumentException("Session not found with id: " + sessionId);
    }

    public PomodoroSession updateSessionNotes(Long sessionId, String notes) {
        Optional<PomodoroSession> optionalSession = sessionRepository.findById(sessionId);
        if (optionalSession.isPresent()) {
            PomodoroSession session = optionalSession.get();
            session.setNotes(notes);
            return sessionRepository.save(session);
        }
        throw new IllegalArgumentException("Session not found with id: " + sessionId);
    }

    public List<PomodoroSession> getUserSessions(User user) {
        return sessionRepository.findByUser(user);
    }

    public List<PomodoroSession> getCompletedUserSessions(User user) {
        return sessionRepository.findByUserAndCompletedTrue(user);
    }

    public List<PomodoroSession> getUserSessionsByDateRange(User user, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        return sessionRepository.findByUserAndStartTimeBetween(user, startDateTime, endDateTime);
    }

    public Long countCompletedFocusSessionsToday(User user) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        return sessionRepository.countCompletedFocusSessionsByUserAndDateRange(user, startOfDay, endOfDay);
    }

    public List<PomodoroSession> getRecentSessions(User user) {
        return sessionRepository.findRecentSessionsByUser(user);
    }
}