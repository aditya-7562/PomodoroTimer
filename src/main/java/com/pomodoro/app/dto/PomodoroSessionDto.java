package com.pomodoro.app.dto;

import java.time.LocalDateTime;

import com.pomodoro.app.model.PomodoroSession.SessionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PomodoroSessionDto {

    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private SessionType sessionType;
    private Boolean completed;
    private String notes;
    private String taskDescription;
}