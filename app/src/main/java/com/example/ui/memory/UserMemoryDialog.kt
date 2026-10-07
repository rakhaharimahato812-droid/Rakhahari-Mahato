package com.example.ui.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.memory.entities.UserMemoryFactEntity
import com.example.data.memory.entities.UserProfileEntity
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavy
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun UserMemoryDialog(
    activeProfile: UserProfileEntity?,
    allProfiles: List<UserProfileEntity>,
    memoryFacts: List<UserMemoryFactEntity>,
    onSwitchProfile: (String) -> Unit,
    onCreateProfile: (String) -> Unit,
    onUpdateName: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onDeleteFact: (String) -> Unit,
    onAddFact: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var isEditingName by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(activeProfile?.name.orEmpty()) }
    var isCreatingProfile by remember { mutableStateOf(false) }
    var newProfileNameInput by remember { mutableStateOf("") }
    var isAddingFact by remember { mutableStateOf(false) }
    var newFactKeyInput by remember { mutableStateOf("") }
    var newFactValInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("FACT") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberNavy, RoundedCornerShape(16.dp))
                .border(1.5.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "JARVIS MEMORY ARCHITECTURE",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = JarvisCyanLight,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "মাল্টি-ইউজার স্থায়ী পারসিস্টেন্ট মেমোরি",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_memory_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                // Profile Selector / Switcher
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberSurface, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "অ্যাক্টিভ প্রোফাইল (ACTIVE PROFILES)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGold
                        )

                        IconButton(
                            onClick = { isCreatingProfile = !isCreatingProfile },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("create_profile_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Profile",
                                tint = JarvisCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Profile Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allProfiles.forEach { prof ->
                            val isSelected = prof.id == activeProfile?.id
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) JarvisDarkBlueHighlight() else CyberSurfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) JarvisCyan else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSwitchProfile(prof.id) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("profile_pill_${prof.id}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isSelected) JarvisCyanLight else TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = prof.name.ifBlank { "User ${prof.id.takeLast(4)}" },
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) JarvisCyanLight else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Create Profile Box
                    if (isCreatingProfile) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newProfileNameInput,
                                onValueChange = { newProfileNameInput = it },
                                placeholder = { Text("নতুন ব্যবহারকারীর নাম...", fontSize = 12.sp, color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("new_profile_input"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = CyberBlack,
                                    unfocusedContainerColor = CyberBlack,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedIndicatorColor = JarvisCyan,
                                    unfocusedIndicatorColor = JarvisCyan.copy(alpha = 0.3f)
                                )
                            )
                            Button(
                                onClick = {
                                    if (newProfileNameInput.isNotBlank()) {
                                        onCreateProfile(newProfileNameInput)
                                        newProfileNameInput = ""
                                        isCreatingProfile = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                                modifier = Modifier.testTag("confirm_create_profile_btn")
                            ) {
                                Text("তৈরি করুন", color = CyberBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Active User Name & Rename
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "সংরক্ষিত নাম:",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = if (activeProfile?.name.isNullOrBlank()) "এখনো শনাক্ত করা হয়নি (Not set yet)" else activeProfile!!.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeProfile?.name.isNullOrBlank()) NeonAmber else JarvisCyanLight
                            )
                        }

                        IconButton(
                            onClick = {
                                isEditingName = !isEditingName
                                nameInput = activeProfile?.name.orEmpty()
                            },
                            modifier = Modifier.testTag("edit_name_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Name",
                                tint = JarvisCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (isEditingName) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                placeholder = { Text("নাম লিখুন...", fontSize = 12.sp, color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("name_edit_input"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = CyberBlack,
                                    unfocusedContainerColor = CyberBlack,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedIndicatorColor = JarvisCyan,
                                    unfocusedIndicatorColor = JarvisCyan.copy(alpha = 0.3f)
                                )
                            )
                            Button(
                                onClick = {
                                    if (nameInput.isNotBlank()) {
                                        onUpdateName(nameInput)
                                        isEditingName = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisBlue),
                                modifier = Modifier.testTag("save_name_btn")
                            ) {
                                Text("সেভ", fontSize = 11.sp, color = TextPrimary)
                            }
                        }
                    }
                }

                // Memory Facts & Notes List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(CyberSurface, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "স্মৃতিভান্ডার (SAVED FACTS & PREFERENCES): ${memoryFacts.size}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyanLight
                        )

                        IconButton(
                            onClick = { isAddingFact = !isAddingFact },
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("add_fact_toggle_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add fact",
                                tint = JarvisCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (isAddingFact) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                value = newFactValInput,
                                onValueChange = { newFactValInput = it },
                                placeholder = { Text("নতুন তথ্য বা নোট লিখুন...", fontSize = 11.sp, color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = CyberBlack,
                                    unfocusedContainerColor = CyberBlack,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedIndicatorColor = JarvisCyan,
                                    unfocusedIndicatorColor = JarvisCyan.copy(alpha = 0.3f)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        if (newFactValInput.isNotBlank()) {
                                            onAddFact("NOTE", "custom_note", newFactValInput)
                                            newFactValInput = ""
                                            isAddingFact = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan),
                                    modifier = Modifier.testTag("save_fact_btn")
                                ) {
                                    Text("যুক্ত করুন", color = CyberBlack, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    if (memoryFacts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "কোনো তথ্য সংরক্ষিত নেই। কথা বলার সাথে সাথে মাহি নিজে থেকেই তথ্য নোট করে রাখবে।",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(memoryFacts, key = { it.id }) { fact ->
                                MemoryFactRow(
                                    fact = fact,
                                    onDelete = { onDeleteFact(fact.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryFactRow(
    fact: UserMemoryFactEntity,
    onDelete: () -> Unit
) {
    val categoryColor = when (fact.category) {
        "NAME" -> NeonGreen
        "PREFERENCE" -> NeonPink
        "TASK" -> NeonAmber
        "NOTE" -> JarvisCyan
        else -> NeonGold
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberBlack.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .border(0.5.dp, categoryColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(categoryColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = fact.category,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = fact.value,
                fontSize = 12.sp,
                color = TextPrimary,
                lineHeight = 16.sp
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .size(24.dp)
                .testTag("delete_fact_${fact.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun JarvisDarkBlueHighlight(): Color = Color(0xFF0F2B5C)
