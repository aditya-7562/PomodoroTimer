package com.pomodoro.app.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.time.temporal.ChronoUnit;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pomodoro.app.dto.UserSettingsDto;
import com.pomodoro.app.model.PomodoroSession;
import com.pomodoro.app.model.User;
import com.pomodoro.app.service.PomodoroSessionService;
import com.pomodoro.app.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PomodoroSessionService sessionService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User appUser = (User) authentication.getPrincipal();

        // Get today's completed focus sessions count
        Long todayCompletedSessions = sessionService.countCompletedFocusSessionsToday(appUser);

        // Get recent sessions
        List<PomodoroSession> recentSessions = sessionService.getRecentSessions(appUser);

        // Get sessions for the last 7 days
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);
        List<PomodoroSession> weekSessions = sessionService.getUserSessionsByDateRange(appUser, sevenDaysAgo, today);

        // Calculate today's focus time (in minutes)
        int todayFocusTime = 0;
        for (PomodoroSession session : weekSessions) {
            if (session.getSessionType() == PomodoroSession.SessionType.FOCUS &&
                    session.getCompleted() &&
                    session.getStartTime().toLocalDate().equals(today)) {
                todayFocusTime += session.getDurationMinutes();
            }
        }

        // Calculate weekly focus time (in minutes)
        int weeklyFocusTime = 0;
        for (PomodoroSession session : weekSessions) {
            if (session.getSessionType() == PomodoroSession.SessionType.FOCUS && session.getCompleted()) {
                weeklyFocusTime += session.getDurationMinutes();
            }
        }

        // Calculate weekly completed sessions
        long weeklyCompletedSessions = weekSessions.stream()
                .filter(s -> s.getSessionType() == PomodoroSession.SessionType.FOCUS && s.getCompleted())
                .count();

        // Calculate daily target percentage
        double dailyTargetPercentage = 0;
        if (appUser.getDailyTarget() > 0) {
            dailyTargetPercentage = (todayCompletedSessions * 100.0) / appUser.getDailyTarget();
            if (dailyTargetPercentage > 100) {
                dailyTargetPercentage = 100;
            }
        }

        // Prepare weekly data for chart
        String[] weeklyLabels = new String[7];
        int[] weeklyData = new int[7];

        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(6 - i);
            weeklyLabels[i] = date.getDayOfWeek().toString().substring(0, 3);
            final LocalDate currentDate = date;

            int dailyMinutes = 0;
            for (PomodoroSession session : weekSessions) {
                if (session.getSessionType() == PomodoroSession.SessionType.FOCUS &&
                        session.getCompleted() &&
                        session.getStartTime().toLocalDate().equals(currentDate)) {
                    dailyMinutes += session.getDurationMinutes();
                }
            }
            weeklyData[i] = dailyMinutes;
        }

        model.addAttribute("user", appUser);
        model.addAttribute("todayCompletedSessions", todayCompletedSessions);
        model.addAttribute("todayFocusTime", todayFocusTime);
        model.addAttribute("weeklyFocusTime", weeklyFocusTime);
        model.addAttribute("weeklyCompletedSessions", weeklyCompletedSessions);
        model.addAttribute("dailyTargetPercentage", dailyTargetPercentage);
        model.addAttribute("weeklyLabels", weeklyLabels);
        model.addAttribute("weeklyData", weeklyData);
        model.addAttribute("recentSessions", recentSessions);

        return "user/dashboard";
    }

    @GetMapping("/settings")
    public String showSettings(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        UserSettingsDto settingsDto = new UserSettingsDto();
        settingsDto.setFocusDuration(user.getFocusDuration());
        settingsDto.setBreakDuration(user.getBreakDuration());
        settingsDto.setLongBreakDuration(user.getLongBreakDuration());
        settingsDto.setPomodorosUntilLongBreak(user.getPomodorosUntilLongBreak());
        settingsDto.setAutoStartBreaks(user.getAutoStartBreaks());
        settingsDto.setAutoStartPomodoros(user.getAutoStartPomodoros());
        settingsDto.setDailyTarget(user.getDailyTarget());

        model.addAttribute("userSettingsDto", settingsDto);
        model.addAttribute("user", user);

        return "user/settings";
    }

    @PostMapping("/settings")
    public String updateSettings(@Valid @ModelAttribute("userSettingsDto") UserSettingsDto settingsDto,
            BindingResult result, Authentication authentication) {
        if (result.hasErrors()) {
            return "user/settings";
        }

        User user = (User) authentication.getPrincipal();

        // Update user settings
        User updatedSettings = new User();
        updatedSettings.setFocusDuration(settingsDto.getFocusDuration());
        updatedSettings.setBreakDuration(settingsDto.getBreakDuration());
        updatedSettings.setLongBreakDuration(settingsDto.getLongBreakDuration());
        updatedSettings.setPomodorosUntilLongBreak(settingsDto.getPomodorosUntilLongBreak());
        updatedSettings.setAutoStartBreaks(settingsDto.getAutoStartBreaks());
        updatedSettings.setAutoStartPomodoros(settingsDto.getAutoStartPomodoros());
        updatedSettings.setDailyTarget(settingsDto.getDailyTarget());

        userService.updateUserSettings(user, updatedSettings);

        return "redirect:/user/settings?success";
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);

        // Fetch all completed focus sessions
        List<PomodoroSession> allSessions = sessionService.getUserSessions(user);
        List<PomodoroSession> completedFocusSessions = allSessions.stream()
                .filter(s -> s.getSessionType() == PomodoroSession.SessionType.FOCUS && s.getCompleted())
                .toList();

        // 1. Total Focus Sessions
        int totalFocusSessions = completedFocusSessions.size();

        // 2. Total Focus Time (convert to hours)
        int totalMinutes = completedFocusSessions.stream()
                .mapToInt(PomodoroSession::getDurationMinutes)
                .sum();
        int totalFocusHours = totalMinutes / 60;

        // 3. Average Daily Sessions
        if (!completedFocusSessions.isEmpty()) {
            LocalDate first = completedFocusSessions.stream()
                    .map(s -> s.getStartTime().toLocalDate())
                    .min(LocalDate::compareTo).orElse(LocalDate.now());

            LocalDate last = completedFocusSessions.stream()
                    .map(s -> s.getStartTime().toLocalDate())
                    .max(LocalDate::compareTo).orElse(LocalDate.now());

            long days = ChronoUnit.DAYS.between(first, last) + 1; // avoid division by zero
            double avgDailySessions = (double) totalFocusSessions / days;
            model.addAttribute("avgDailySessions", String.format("%.1f", avgDailySessions));
        } else {
            model.addAttribute("avgDailySessions", 0);
        }

        model.addAttribute("totalFocusSessions", totalFocusSessions);
        model.addAttribute("totalFocusHours", totalFocusHours);

        return "user/profile";
    }

}