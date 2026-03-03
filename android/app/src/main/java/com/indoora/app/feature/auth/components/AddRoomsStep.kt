package com.indoora.app.feature.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoomsStep(
    homeName: String,
    rooms: MutableList<RoomData>,
    onAddRoom: () -> Unit,
    onRemoveRoom: (RoomData) -> Unit,
    createRoomsState: com.indoora.app.feature.auth.UiState<*>,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    var editingRoom by remember { mutableStateOf<RoomData?>(null) }
    val isLoading = createRoomsState is com.indoora.app.feature.auth.UiState.Loading

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Crea el Hogar del usuario que\nserá supervisado",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Campo nombre de la casa
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Nombre",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = homeName,
                            onValueChange = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.White.copy(alpha = 0.7f),
                                disabledBorderColor = Color.White.copy(alpha = 0.3f),
                                disabledContainerColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Lista de habitaciones
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Habitaciones",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Chips de habitaciones
                        rooms.forEach { room ->
                            RoomChip(
                                room = room,
                                onEdit = { editingRoom = room },
                                onRemove = { onRemoveRoom(room) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Botón añadir habitación
                        OutlinedButton(
                            onClick = onAddRoom,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.4f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Añadir habitación", fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones atrás y continuar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Atrás", color = Color.White, fontSize = 16.sp)
                }

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.3f),
                        disabledContainerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = rooms.isNotEmpty() && rooms.all { it.name.isNotBlank() } && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Continuar", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // Dialog para editar habitación
    if (editingRoom != null) {
        EditRoomDialog(
            room = editingRoom!!,
            onDismiss = { editingRoom = null },
            onSave = { editingRoom = null }
        )
    }
}

@Composable
private fun RoomChip(
    room: RoomData,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = room.name.ifBlank { "Sin nombre" },
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditRoomDialog(
    room: RoomData,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var name by remember { mutableStateOf(room.name) }
    var selectedType by remember { mutableStateOf(room.type) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF7B6BA8)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Tipo de habitación",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Campo nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Nombre de la habitación", color = Color.White.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                        cursorColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dropdown tipo de habitación
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Tipo", color = Color.White.copy(alpha = 0.5f)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                            focusedContainerColor = Color.White.copy(alpha = 0.1f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        RoomType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName, color = Color.Black) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar", color = Color.White.copy(alpha = 0.7f))
                    }

                    Button(
                        onClick = {
                            room.name = name
                            room.type = selectedType
                            onSave()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Guardar", color = Color.White)
                    }
                }
            }
        }
    }
}