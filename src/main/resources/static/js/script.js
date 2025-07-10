// DOM Elements
const timer = document.getElementById('timer');
const sessionTypeElement = document.getElementById('session-type');
const startBtn = document.getElementById('start-btn');
const resetBtn = document.getElementById('reset-btn');
const pomodoroCount = document.getElementById('pomodoro-count');
const focusTimeInput = document.getElementById('focus-time');
const breakTimeInput = document.getElementById('break-time');

// Timer State
let minutes;
let seconds;
let isRunning = false;
let timerInterval;
let currentSessionType = 'focus'; // 'focus' or 'break'
let pomodoroCountValue = 0;

// Audio setup with Tone.js
const synth = new Tone.Synth().toDestination();
const playSound = () => {
    // Play a simple chime sound
    synth.triggerAttackRelease("C5", "8n");
    setTimeout(() => synth.triggerAttackRelease("G5", "8n"), 200);
    setTimeout(() => synth.triggerAttackRelease("E5", "8n"), 400);
};

// Initialize timer
function initTimer() {
    // Try to load saved state
    loadTimerState();
    
    // If no saved state, set default values
    if (minutes === undefined) {
        minutes = parseInt(focusTimeInput.value);
        seconds = 0;
    }
    
    updateTimerDisplay();
    updateSessionTypeDisplay();
    updatePomodoroCountDisplay();
}

// Update timer display
function updateTimerDisplay() {
    const displayMinutes = String(minutes).padStart(2, '0');
    const displaySeconds = String(seconds).padStart(2, '0');
    timer.textContent = `${displayMinutes}:${displaySeconds}`;
    
    // Add pulsing effect when timer is running
    if (isRunning) {
        timer.classList.add('pulse');
    } else {
        timer.classList.remove('pulse');
    }
    
    // Update page title
    document.title = `${displayMinutes}:${displaySeconds} - ${currentSessionType === 'focus' ? 'Focus' : 'Break'}`;
}

// Update session type display
function updateSessionTypeDisplay() {
    sessionTypeElement.textContent = currentSessionType === 'focus' ? 'Focus Session' : 'Break Time';
}

// Update the pomodoro count display
function updatePomodoroCountDisplay() {
    document.getElementById('pomodoro-count').textContent = pomodoroCountValue;
}

// Alias for backward compatibility
function updatePomodoroCount() {
    updatePomodoroCountDisplay();
}

// Start/Pause timer
function toggleTimer() {
    if (isRunning) {
        pauseTimer();
    } else {
        startTimer();
    }
}

// Start timer
function startTimer() {
    isRunning = true;
    startBtn.innerHTML = `
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zM7 8a1 1 0 012 0v4a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v4a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd" />
        </svg>
        Pause
    `;
    startBtn.classList.remove('bg-green-500', 'hover:bg-green-600');
    startBtn.classList.add('bg-yellow-500', 'hover:bg-yellow-600');
    timer.classList.add('timer-active');
    
    timerInterval = setInterval(updateTimer, 1000);
    saveTimerState();
}

// Pause timer
function pauseTimer() {
    isRunning = false;
    startBtn.innerHTML = `
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z" clip-rule="evenodd" />
        </svg>
        Start
    `;
    startBtn.classList.remove('bg-yellow-500', 'hover:bg-yellow-600');
    startBtn.classList.add('bg-green-500', 'hover:bg-green-600');
    timer.classList.remove('timer-active');
    
    clearInterval(timerInterval);
    saveTimerState();
}

// Reset timer
function resetTimer() {
    pauseTimer();
    
    // Reset to focus time
    currentSessionType = 'focus';
    minutes = parseInt(focusTimeInput.value);
    seconds = 0;
    
    updateTimerDisplay();
    updateSessionTypeDisplay();
    saveTimerState();
}

// Update timer every second
function updateTimer() {
    if (seconds > 0) {
        seconds--;
    } else if (minutes > 0) {
        minutes--;
        seconds = 59;
    } else {
        // Timer completed
        completeTimer();
        return;
    }
    
    updateTimerDisplay();
    saveTimerState();
}

// Handle timer completion
function completeTimer() {
    pauseTimer();
    playSound();
    showNotification();
    
    if (currentSessionType === 'focus') {
        // Completed a focus session
        pomodoroCountValue++;
        updatePomodoroCountDisplay();
        
        // Switch to break
        currentSessionType = 'break';
        minutes = parseInt(breakTimeInput.value);
    } else {
        // Completed a break session, switch to focus
        currentSessionType = 'focus';
        minutes = parseInt(focusTimeInput.value);
    }
    
    seconds = 0;
    updateTimerDisplay();
    updateSessionTypeDisplay();
    saveTimerState();
}

// Show browser notification
function showNotification() {
    if (Notification.permission === 'granted') {
        const title = currentSessionType === 'focus' ? 'Break Time!' : 'Focus Time!';
        const message = currentSessionType === 'focus' ? 'Great job! Take a short break.' : 'Break is over. Time to focus!';
        
        new Notification(title, {
            body: message,
            icon: '/favicon.ico'
        });
    } else if (Notification.permission !== 'denied') {
        Notification.requestPermission();
    }
}

// Save timer state to localStorage
function saveTimerState() {
    const timerState = {
        minutes,
        seconds,
        isRunning,
        currentSessionType,
        pomodoroCountValue,
        focusTime: parseInt(focusTimeInput.value),
        breakTime: parseInt(breakTimeInput.value)
    };
    
    localStorage.setItem('pomodoroTimerState', JSON.stringify(timerState));
}

// Load timer state from localStorage
function loadTimerState() {
    const savedState = localStorage.getItem('pomodoroTimerState');
    
    if (savedState) {
        const timerState = JSON.parse(savedState);
        
        minutes = timerState.minutes;
        seconds = timerState.seconds;
        isRunning = timerState.isRunning;
        currentSessionType = timerState.currentSessionType;
        pomodoroCountValue = timerState.pomodoroCountValue || 0;
        
        focusTimeInput.value = timerState.focusTime;
        breakTimeInput.value = timerState.breakTime;
        
        // Update displays
        updateSessionTypeDisplay();
        updateTimerDisplay();
        updatePomodoroCountDisplay();
        
        // Restart timer if it was running
        if (isRunning) {
            startTimer();
        }
    }
}

// Event Listeners
startBtn.addEventListener('click', toggleTimer);
resetBtn.addEventListener('click', resetTimer);

focusTimeInput.addEventListener('change', () => {
    if (currentSessionType === 'focus' && !isRunning) {
        minutes = parseInt(focusTimeInput.value);
        seconds = 0;
        updateTimerDisplay();
        saveTimerState();
    }
});

breakTimeInput.addEventListener('change', () => {
    if (currentSessionType === 'break' && !isRunning) {
        minutes = parseInt(breakTimeInput.value);
        seconds = 0;
        updateTimerDisplay();
        saveTimerState();
    }
});

// Request notification permission
document.addEventListener('DOMContentLoaded', () => {
    if (Notification.permission !== 'granted' && Notification.permission !== 'denied') {
        Notification.requestPermission();
    }
});

// Initialize the app
initTimer();