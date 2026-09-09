package com.stephaneperez.angerona.fasti.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stephaneperez.angerona.fasti.R
import com.stephaneperez.angerona.fasti.data.FastiViewModel
import com.stephaneperez.angerona.fasti.ui.components.DayEventsList
import com.stephaneperez.angerona.fasti.ui.components.DeleteConfirmDialog
import com.stephaneperez.angerona.fasti.ui.components.EventEditDialog
import com.stephaneperez.angerona.fasti.ui.components.FastiToastHost
import com.stephaneperez.angerona.fasti.ui.components.MonthGrid
import com.stephaneperez.angerona.fasti.ui.components.MonthHeader
import com.stephaneperez.angerona.fasti.ui.theme.FastiColors
import java.time.LocalDate

@Composable
fun FastiScreen(viewModel: FastiViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val today = remember { LocalDate.now() }

    val datesWithEvents = state.events.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }.toSet()
    val selectedDayEvents = state.events.filter { it.date == state.selectedDate.toString() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FastiColors.ground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MonthHeader(
                month = state.visibleMonth,
                onPrevious = viewModel::goToPreviousMonth,
                onNext = viewModel::goToNextMonth,
                onToday = viewModel::goToToday,
            )

            MonthGrid(
                month = state.visibleMonth,
                selectedDate = state.selectedDate,
                today = today,
                datesWithEvents = datesWithEvents,
                onDateSelected = viewModel::selectDate,
                modifier = Modifier.padding(horizontal = 6.dp),
            )

            DayEventsList(
                events = selectedDayEvents,
                onEventClick = viewModel::startEditingEvent,
                onEventDelete = { viewModel.requestDelete(it.id) },
                modifier = Modifier.weight(1f).fillMaxSize(),
            )
        }

        FloatingActionButton(
            onClick = { viewModel.startNewEvent(state.selectedDate) },
            containerColor = FastiColors.accent,
            contentColor = FastiColors.surface,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(18.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = stringResource(R.string.cd_add_event),
                modifier = Modifier.size(20.dp),
            )
        }

        state.editingEvent?.let { editing ->
            EventEditDialog(
                editing = editing,
                isNew = editing.id == null,
                onTitleChanged = viewModel::updateEditingTitle,
                onTimesChanged = viewModel::updateEditingTimes,
                onDescriptionChanged = viewModel::updateEditingDescription,
                onCancel = viewModel::cancelEditingEvent,
                onConfirm = viewModel::confirmEditingEvent,
                onDelete = if (editing.id != null) {
                    { viewModel.requestDelete(editing.id); viewModel.cancelEditingEvent() }
                } else null,
            )
        }

        if (state.confirmDeleteId != null) {
            DeleteConfirmDialog(
                onCancel = viewModel::cancelDelete,
                onConfirm = viewModel::confirmDelete,
            )
        }

        FastiToastHost(message = state.toast, onDismissed = viewModel::consumeToast)
    }
}
