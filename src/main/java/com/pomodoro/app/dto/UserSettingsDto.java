package com.pomodoro.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsDto {

    @NotNull(message = "Focus duration is required")
    @Min(value = 1, message = "Focus duration must be at least 1 minute")
    @Max(value = 120, message = "Focus duration must be at most 120 minutes")
    private Integer focusDuration = 25;

    @NotNull(message = "Break duration is required")
    @Min(value = 1, message = "Break duration must be at least 1 minute")
    @Max(value = 30, message = "Break duration must be at most 30 minutes")
    private Integer breakDuration = 5;

    @NotNull(message = "Long break duration is required")
    @Min(value = 1, message = "Long break duration must be at least 1 minute")
    @Max(value = 60, message = "Long break duration must be at most 60 minutes")
    private Integer longBreakDuration = 15;

    @NotNull(message = "Pomodoros until long break is required")
    @Min(value = 1, message = "Pomodoros until long break must be at least 1")
    @Max(value = 10, message = "Pomodoros until long break must be at most 10")
    private Integer pomodorosUntilLongBreak = 4;

    @NotNull(message = "Auto start breaks is required")
    private Boolean autoStartBreaks = false;

    @NotNull(message = "Auto start pomodoros is required")
    private Boolean autoStartPomodoros = false;

    @NotNull(message = "Daily target is required")
    @Min(value = 1, message = "Daily target must be at least 1")
    @Max(value = 20, message = "Daily target must be at most 20")
    private Integer dailyTarget = 8;
}