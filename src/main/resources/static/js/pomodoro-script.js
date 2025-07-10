// DOM Elements
const timerDisplay = document.getElementById('timer');
const sessionTypeDisplay = document.getElementById('session-type');
const startButton = document.getElementById('start-btn');
const resetButton = document.getElementById('reset-btn');
const pomodoroCountDisplay = document.getElementById('pomodoro-count');
const progressBar = document.getElementById('progress-bar');
const taskDescriptionInput = document.getElementById('task-description');
const notesContainer = document.getElementById('notes-container');
const sessionNotesTextarea = document.getElementById('session-notes');
const saveNotesButton = document.getElementById('save-notes-btn');

// Timer State
let minutes;
let seconds = 0;
let isRunning = false;
let timerInterval;
let sessionType = 'FOCUS'; // 'FOCUS', 'SHORT_BREAK', 'LONG_BREAK'
let pomodoroCount = todayCompletedPomodoros || 0;
let currentSessionId = null;

// Audio Feedback using Tone.js
const synth = new Tone.Synth().toDestination();
const playSound = () => {
    synth.triggerAttackRelease("C5", "8n");
    setTimeout(() => synth.triggerAttackRelease("G4", "8n"), 200);
    setTimeout(() => synth.triggerAttackRelease("E4", "8n"), 400);
};

// Initialize timer with user settings
function initializeTimer() {
    // Set initial time based on session type
    if (sessionType === 'FOCUS') {
        minutes = userSettings.focusDurationMinutes;
        sessionTypeDisplay.textContent = 'Focus Session';
    } else if (sessionType === 'SHORT_BREAK') {
        minutes = userSettings.shortBreakDurationMinutes;
        sessionTypeDisplay.textContent = 'Short Break';
    } else if (sessionType === 'LONG_BREAK') {
        minutes = userSettings.longBreakDurationMinutes;
        sessionTypeDisplay.textContent = 'Long Break';
    }
    
    seconds = 0;
    updateTimerDisplay();
    updatePomodoroCount();
}

// Update the timer display
function updateTimerDisplay() {
    const displayMinutes = String(minutes).padStart(2, '0');
    const displaySeconds = String(seconds).padStart(2, '0');
    timerDisplay.textContent = `${displayMinutes}:${displaySeconds}`;
    
    // Add pulsing effect when timer is running
    if (isRunning) {
        timerDisplay.classList.add('pulse');
    } else {
        timerDisplay.classList.remove('pulse');
    }
    
    // Update document title
    document.title = `${displayMinutes}:${displaySeconds} - ${sessionTypeDisplay.textContent}`;
}

// Update the pomodoro count display
function updatePomodoroCount() {
    pomodoroCountDisplay.textContent = pomodoroCount;
    
    // Update progress bar
    const progressPercentage = (pomodoroCount / userSettings.dailyPomodoroTarget) * 100;
    progressBar.style.width = `${Math.min(progressPercentage, 100)}%`;
}

// Start the timer
function startTimer() {
    if (isRunning) return;
    
    isRunning = true;
    startButton.textContent = 'Pause';
    
    // If starting a new session, create it on the server
    if (!currentSessionId) {
        const taskDescription = taskDescriptionInput.value.trim();
        createSession(sessionType, taskDescription);
    }
    
    timerInterval = setInterval(() => {
        if (seconds === 0) {
            if (minutes === 0) {
                completeTimer();
                return;
            }
            minutes--;
            seconds = 59;
        } else {
            seconds--;
        }
        updateTimerDisplay();
    }, 1000);
}

// Pause the timer
function pauseTimer() {
    if (!isRunning) return;
    
    isRunning = false;
    startButton.textContent = 'Start';
    clearInterval(timerInterval);
    updateTimerDisplay();
}

// Reset the timer
function resetTimer() {
    pauseTimer();
    initializeTimer();
    
    // Reset current session
    currentSessionId = null;
    notesContainer.classList.add('hidden');
    sessionNotesTextarea.value = '';
    taskDescriptionInput.value = '';
}

// Complete the timer
function completeTimer() {
    pauseTimer();
    playSound();
    showNotification();
    
    // Complete the session on the server
    if (currentSessionId) {
        completeSession(currentSessionId);
        
        // Show notes container for focus sessions
        if (sessionType === 'FOCUS') {
            notesContainer.classList.remove('hidden');
        }
    }
    
    // Update pomodoro count if it was a focus session
    if (sessionType === 'FOCUS') {
        pomodoroCount++;
        updatePomodoroCount();
    }
    
    // Determine next session type
    if (sessionType === 'FOCUS') {
        // Check if it's time for a long break
        if (pomodoroCount % userSettings.pomodorosUntilLongBreak === 0) {
            sessionType = 'LONG_BREAK';
        } else {
            sessionType = 'SHORT_BREAK';
        }
        
        // Auto-start break if enabled
        initializeTimer();
        if (userSettings.autoStartBreaks) {
            setTimeout(() => startTimer(), 1000);
        }
    } else {
        // After a break, go back to focus mode
        sessionType = 'FOCUS';
        
        // Auto-start pomodoro if enabled
        initializeTimer();
        if (userSettings.autoStartPomodoros) {
            setTimeout(() => startTimer(), 1000);
        }
    }
    
    // Reset current session ID for the next session
    currentSessionId = null;
}

// Show browser notification
function showNotification() {
    if (Notification.permission === 'granted') {
        const title = sessionType === 'FOCUS' ? 
            'Focus session completed!' : 
            'Break time is over!';
        
        const message = sessionType === 'FOCUS' ? 
            'Time for a break!' : 
            'Ready to focus again?';
        
        new Notification(title, { body: message });
    } else if (Notification.permission !== 'denied') {
        Notification.requestPermission();
    }
}

// API calls to the server

// Create a new session
function createSession(sessionType, taskDescription) {
    fetch('/pomodoro/start', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
            'sessionType': sessionType,
            'taskDescription': taskDescription || ''
        })
    })
    .then(response => response.json())
    .then(data => {
        currentSessionId = data.id;
        console.log('Session created:', data);
    })
    .catch(error => console.error('Error creating session:', error));
}

// Complete a session
function completeSession(sessionId) {
    fetch(`/pomodoro/complete/${sessionId}`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        console.log('Session completed:', data);
    })
    .catch(error => console.error('Error completing session:', error));
}

// Update session notes
function updateSessionNotes(sessionId, notes) {
    fetch(`/pomodoro/notes/${sessionId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'text/plain',
        },
        body: notes
    })
    .then(response => response.json())
    .then(data => {
        console.log('Notes updated:', data);
    })
    .catch(error => console.error('Error updating notes:', error));
}

// Event Listeners
startButton.addEventListener('click', () => {
    if (isRunning) {
        pauseTimer();
    } else {
        startTimer();
    }
});

resetButton.addEventListener('click', resetTimer);

saveNotesButton.addEventListener('click', () => {
    const notes = sessionNotesTextarea.value.trim();
    if (currentSessionId && notes) {
        updateSessionNotes(currentSessionId, notes);
    }
});

// Request notification permission
document.addEventListener('DOMContentLoaded', () => {
    if (Notification.permission !== 'granted' && Notification.permission !== 'denied') {
        Notification.requestPermission();
    }
});

// Initialize the timer on page load
initializeTimer();