package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.ConsultationBooking
import com.example.myapplication.model.ConsultationSlot
import com.example.myapplication.model.Expert

private enum class ConsultationsMode {
    Experts,
    MyConsultations
}

@Composable
fun ConsultationsScreen(
    currentUserId: Int,
    experts: List<Expert>,
    slots: List<ConsultationSlot>,
    bookings: List<ConsultationBooking>,
    onBookSlot: (Int) -> Boolean,
    modifier: Modifier = Modifier
) {
    var mode by rememberSaveable { mutableStateOf(ConsultationsMode.Experts) }
    var selectedExpertId by rememberSaveable { mutableStateOf<Int?>(null) }
    var statusMessage by rememberSaveable { mutableStateOf("") }

    val expertById = experts.associateBy { it.id }
    val bookedSlotIds = bookings.map { it.slotId }.toSet()
    val myBookings = bookings.filter { it.userId == currentUserId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Consultations",
            modifier = Modifier.padding(top = 24.dp),
            fontWeight = FontWeight.Bold
        )

        Row(modifier = Modifier.padding(top = 12.dp)) {
            Button(onClick = { mode = ConsultationsMode.Experts }) {
                Text("Experts")
            }
            Button(
                onClick = { mode = ConsultationsMode.MyConsultations },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("My consultations")
            }
        }

        if (statusMessage.isNotBlank()) {
            Text(
                text = statusMessage,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        if (mode == ConsultationsMode.Experts) {
            val selectedExpert = experts.find { it.id == selectedExpertId }
            if (selectedExpert == null) {
                experts.forEach { expert ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = expert.name, fontWeight = FontWeight.SemiBold)
                            Text(text = expert.title, modifier = Modifier.padding(top = 2.dp))
                            Button(
                                onClick = { selectedExpertId = expert.id },
                                modifier = Modifier.padding(top = 10.dp)
                            ) {
                                Text("Book consultation")
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = selectedExpert.name,
                    modifier = Modifier.padding(top = 16.dp),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Expertise: ${selectedExpert.expertise}",
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = "Available slots:",
                    modifier = Modifier.padding(top = 12.dp),
                    fontWeight = FontWeight.SemiBold
                )

                val expertSlots = slots
                    .filter { it.expertId == selectedExpert.id }
                    .sortedBy { it.startTime }

                expertSlots.forEach { slot ->
                    val isBooked = bookedSlotIds.contains(slot.id)
                    Button(
                        onClick = {
                            val booked = onBookSlot(slot.id)
                            statusMessage = if (booked) {
                                "Consultation booked."
                            } else {
                                "Slot is no longer available."
                            }
                        },
                        enabled = !isBooked,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("${slot.startTime} - ${slot.endTime}")
                    }
                }

                Button(
                    onClick = { selectedExpertId = null },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("Back to experts")
                }
            }
        } else {
            if (myBookings.isEmpty()) {
                Text(
                    text = "You have no consultations booked.",
                    modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                )
            } else {
                myBookings.forEach { booking ->
                    val slot = slots.find { it.id == booking.slotId } ?: return@forEach
                    val expert = expertById[slot.expertId]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = expert?.name ?: "Unknown expert",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${slot.startTime} - ${slot.endTime}",
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                text = "Consultation booked",
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
