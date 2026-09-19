package com.stephaneperez.angerona.fasti.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stephaneperez.angerona.fasti.R
import com.stephaneperez.angerona.fasti.notification.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

data class FastiUiState(
    val visibleMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val events: List<CalendarEvent> = emptyList(),
    /** Non-null while the add/edit event sheet is open. Null id = new event. */
    val editingEvent: EditingEvent? = null,
    val confirmDeleteId: String? = null,
    val toast: String = "",
    val loading: Boolean = true,
)

data class EditingEvent(
    val id: String?,
    val date: LocalDate,
    val title: String,
    val startTime: String?,
    val endTime: String?,
    val description: String,
    /** Minutes before [startTime] to fire a reminder notification; null = no reminder.
     * Only meaningful once [startTime] is set — see [CalendarEvent]. */
    val reminderMinutesBefore: Int? = null,
)

class FastiViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CalendarRepository(application)

    private val _uiState = MutableStateFlow(FastiUiState())
    val uiState: StateFlow<FastiUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val context = getApplication<Application>()
            when (val result = repository.load()) {
                is LoadResult.Success -> {
                    _uiState.update { it.copy(events = result.data.events, loading = false) }
                    // AlarmManager alarms don't survive an app force-stop or a period
                    // without a reboot to trigger BootReceiver either — re-arming them
                    // on every cold start is cheap and keeps them consistent with
                    // whatever's actually in the calendar.
                    ReminderScheduler.rescheduleAll(context, result.data.events)
                }
                LoadResult.ReadFailed -> _uiState.update { it.copy(loading = false) }
                LoadResult.TooLarge -> _uiState.update {
                    it.copy(loading = false, toast = context.getString(R.string.toast_file_too_large))
                }
                LoadResult.NotEncrypted, LoadResult.DecryptFailed, LoadResult.CorruptJson -> _uiState.update {
                    it.copy(loading = false, toast = context.getString(R.string.toast_load_failed))
                }
            }
        }
    }

    // ---- Navigation --------------------------------------------------

    fun goToPreviousMonth() {
        _uiState.update { it.copy(visibleMonth = it.visibleMonth.minusMonths(1)) }
    }

    fun goToNextMonth() {
        _uiState.update { it.copy(visibleMonth = it.visibleMonth.plusMonths(1)) }
    }

    fun goToToday() {
        val today = LocalDate.now()
        _uiState.update { it.copy(visibleMonth = YearMonth.from(today), selectedDate = today) }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    /** Jumps the visible month grid to [month] — used when opening from a reminder notification. */
    fun goToMonth(month: YearMonth) {
        _uiState.update { it.copy(visibleMonth = month) }
    }

    // ---- Event editing --------------------------------------------------

    fun startNewEvent(date: LocalDate) {
        _uiState.update {
            it.copy(editingEvent = EditingEvent(id = null, date = date, title = "", startTime = null, endTime = null, description = "", reminderMinutesBefore = null))
        }
    }

    fun startEditingEvent(event: CalendarEvent) {
        _uiState.update {
            it.copy(
                editingEvent = EditingEvent(
                    id = event.id,
                    date = LocalDate.parse(event.date),
                    title = event.title,
                    startTime = event.startTime,
                    endTime = event.endTime,
                    description = event.description,
                    reminderMinutesBefore = event.reminderMinutesBefore,
                )
            )
        }
    }

    fun updateEditingTitle(title: String) {
        _uiState.update { it.copy(editingEvent = it.editingEvent?.copy(title = title)) }
    }

    fun updateEditingTimes(startTime: String?, endTime: String?) {
        _uiState.update {
            it.copy(
                editingEvent = it.editingEvent?.copy(
                    startTime = startTime,
                    endTime = endTime,
                    // A reminder counts back from the start time — if that's cleared,
                    // any reminder choice made against it no longer means anything.
                    reminderMinutesBefore = if (startTime == null) null else it.editingEvent.reminderMinutesBefore,
                )
            )
        }
    }

    fun updateEditingDescription(description: String) {
        _uiState.update { it.copy(editingEvent = it.editingEvent?.copy(description = description)) }
    }

    fun updateEditingReminder(minutesBefore: Int?) {
        _uiState.update { it.copy(editingEvent = it.editingEvent?.copy(reminderMinutesBefore = minutesBefore)) }
    }

    fun cancelEditingEvent() {
        _uiState.update { it.copy(editingEvent = null) }
    }

    /** Saves the currently-edited event (new or existing) and persists the whole calendar. */
    fun confirmEditingEvent() {
        val editing = _uiState.value.editingEvent ?: return
        if (editing.title.isBlank()) return

        val savedEvent = CalendarEvent(
            id = editing.id ?: UUID.randomUUID().toString(),
            title = editing.title.trim(),
            date = editing.date.toString(),
            startTime = editing.startTime,
            endTime = editing.endTime,
            description = editing.description,
            reminderMinutesBefore = editing.reminderMinutesBefore,
        )

        val current = _uiState.value.events
        val updated = if (editing.id == null) {
            current + savedEvent
        } else {
            current.map { event -> if (event.id == editing.id) savedEvent else event }
        }

        _uiState.update { it.copy(events = updated, editingEvent = null) }
        persist(updated)
        ReminderScheduler.schedule(getApplication(), savedEvent)
    }

    fun requestDelete(eventId: String) {
        _uiState.update { it.copy(confirmDeleteId = eventId) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(confirmDeleteId = null) }
    }

    fun confirmDelete() {
        val id = _uiState.value.confirmDeleteId ?: return
        val updated = _uiState.value.events.filterNot { it.id == id }
        _uiState.update { it.copy(events = updated, confirmDeleteId = null) }
        persist(updated)
        ReminderScheduler.cancel(getApplication(), id)
    }

    private fun persist(events: List<CalendarEvent>) {
        viewModelScope.launch {
            val success = repository.save(CalendarData(events = events))
            if (!success) {
                val context = getApplication<Application>()
                _uiState.update { it.copy(toast = context.getString(R.string.toast_save_failed)) }
            }
        }
    }

    fun consumeToast() {
        _uiState.update { it.copy(toast = "") }
    }
}
