package com.pomodoro.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pomodoro.app.dto.PomodoroSessionDto;
import com.pomodoro.app.model.PomodoroSession;
import com.pomodoro.app.model.User;
import com.pomodoro.app.service.PomodoroSessionService;

@Controller
@RequestMapping("/pomodoro")
public class PomodoroController {

    @Autowired
    private PomodoroSessionService sessionService;

    @GetMapping
    public String pomodoroTimer(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        
        // Add today's completed pomodoro sessions count to the model
        Long todayCompletedSessions = sessionService.countCompletedFocusSessionsToday(user);
        model.addAttribute("todayCompletedSessions", todayCompletedSessions);
        
        return "pomodoro";
    }

    @PostMapping("/start")
    @ResponseBody
    public ResponseEntity<PomodoroSessionDto> startSession(
            @RequestParam PomodoroSession.SessionType sessionType,
            @RequestParam(required = false) String taskDescription,
            Authentication authentication) {
        
        // ✅ Log authentication info for debugging
        System.out.println("Authentication = " + authentication);

        User user = (User) authentication.getPrincipal();
        PomodoroSession session = sessionService.startSession(user, sessionType, taskDescription);
        
        PomodoroSessionDto sessionDto = convertToDto(session);
        return ResponseEntity.ok(sessionDto);
    }

    @PostMapping("/complete/{sessionId}")
    @ResponseBody
    public ResponseEntity<PomodoroSessionDto> completeSession(
            @PathVariable Long sessionId) {
        
        PomodoroSession session = sessionService.completeSession(sessionId);
        PomodoroSessionDto sessionDto = convertToDto(session);
        return ResponseEntity.ok(sessionDto);
    }

    @PostMapping("/notes/{sessionId}")
    @ResponseBody
    public ResponseEntity<PomodoroSessionDto> updateSessionNotes(
            @PathVariable Long sessionId,
            @RequestBody String notes) {
        
        PomodoroSession session = sessionService.updateSessionNotes(sessionId, notes);
        PomodoroSessionDto sessionDto = convertToDto(session);
        return ResponseEntity.ok(sessionDto);
    }

    // Helper method to convert PomodoroSession to PomodoroSessionDto
    private PomodoroSessionDto convertToDto(PomodoroSession session) {
        PomodoroSessionDto dto = new PomodoroSessionDto();
        dto.setId(session.getId());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setDurationMinutes(session.getDurationMinutes());
        dto.setSessionType(session.getSessionType());
        dto.setCompleted(session.getCompleted());
        dto.setNotes(session.getNotes());
        dto.setTaskDescription(session.getTaskDescription());
        return dto;
    }
}