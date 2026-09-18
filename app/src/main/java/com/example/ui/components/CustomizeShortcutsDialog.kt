/*
 * Copyright (c) 2026 FahimShahrier. All rights reserved.
 * Developed by FahimShahrier
 */

package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.CategoryEntity
import com.example.data.model.QuickAddTemplate
import com.example.ui.model.AppCurrency
import com.example.ui.model.CurrencyHelper
import com.example.ui.theme.Emerald700
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.util.Locale

/**
 * Dialog for managing (viewing, adding, editing, deleting, resetting) 1-Tap Quick Log shortcuts.
 */
@Composable
fun CustomizeShortcutsDialog(
    templates: List<QuickAddTemplate>,
    categories: List<CategoryEntity>,
    currency: AppCurrency,
    onSaveTemplate: (QuickAddTemplate) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit
) {
    var editingTemplate by remember { mutableStateOf<QuickAddTemplate?>(null) }
    var showTemplateEditor by remember { mutableStateOf(false) }
    var templateToDelete by remember { mutableStateOf<QuickAddTemplate?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .testTag("customize_shortcuts_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Emerald700.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Emerald700,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Customize 1-Tap Shortcuts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${templates.size} shortcuts configured",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_customize_shortcuts_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // Actions row: Add New & Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.testTag("btn_reset_shortcuts_default")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Defaults", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            editingTemplate = null
                            showTemplateEditor = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                        modifier = Modifier.testTag("btn_add_new_shortcut")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Shortcut", fontSize = 13.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of Shortcuts
                if (templates.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No quick-log shortcuts",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(templates, key = { it.id }) { template ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = template.color.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, template.color.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("shortcut_item_${template.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(template.color.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = template.icon,
                                            contentDescription = null,
                                            tint = template.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = template.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Fixed-width type badge so EXPENSE and INCOME are 100% aligned in each row
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (template.type == "EXPENSE") ExpenseRed.copy(alpha = 0.15f) else IncomeGreen.copy(alpha = 0.15f),
                                                modifier = Modifier.width(58.dp)
                                            ) {
                                                Text(
                                                    text = template.type,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (template.type == "EXPENSE") ExpenseRed else IncomeGreen,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(vertical = 2.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            Text(
                                                text = "${template.categoryName} • ${template.paymentMethod}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = CurrencyHelper.formatCompact(template.amount, currency),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (template.type == "EXPENSE") ExpenseRed else IncomeGreen
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    editingTemplate = template
                                                    showTemplateEditor = true
                                                },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .testTag("edit_shortcut_${template.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))

                                            IconButton(
                                                onClick = { templateToDelete = template },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .testTag("delete_shortcut_${template.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = ExpenseRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Done Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_done_customizing_shortcuts"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    // Template Add / Edit Sheet / Dialog
    if (showTemplateEditor) {
        EditShortcutTemplateDialog(
            initialTemplate = editingTemplate,
            categories = categories,
            currency = currency,
            onSave = { saved ->
                onSaveTemplate(saved)
                showTemplateEditor = false
                editingTemplate = null
            },
            onDismiss = {
                showTemplateEditor = false
                editingTemplate = null
            }
        )
    }

    // Delete confirmation dialog
    if (templateToDelete != null) {
        val target = templateToDelete!!
        AlertDialog(
            onDismissRequest = { templateToDelete = null },
            title = { Text("Delete Shortcut?") },
            text = { Text("Are you sure you want to delete '${target.title}' from your quick-log shortcuts?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTemplate(target.id)
                        templateToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { templateToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset defaults confirmation
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset to Default Shortcuts?") },
            text = { Text("This will restore the standard preset shortcuts (Coffee, Lunch, Bus Fare, Groceries, Fuel, Freelance).") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDefaults()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                ) {
                    Text("Reset", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Editor dialog for a single Quick-Add Shortcut.
 */
@Composable
private fun EditShortcutTemplateDialog(
    initialTemplate: QuickAddTemplate?,
    categories: List<CategoryEntity>,
    currency: AppCurrency,
    onSave: (QuickAddTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    val isEdit = initialTemplate != null
    var title by remember { mutableStateOf(initialTemplate?.title ?: "") }
    var amountText by remember {
        mutableStateOf(if (initialTemplate != null) String.format(Locale.US, "%.2f", initialTemplate.amount) else "")
    }
    var type by remember { mutableStateOf(initialTemplate?.type ?: "EXPENSE") }
    var categoryName by remember {
        mutableStateOf(initialTemplate?.categoryName ?: (categories.firstOrNull { it.type == type }?.name ?: "Food & Dining"))
    }
    var paymentMethod by remember { mutableStateOf(initialTemplate?.paymentMethod ?: "bKash") }
    var iconName by remember { mutableStateOf(initialTemplate?.iconName ?: "fastfood") }
    var selectedColorHex by remember { mutableLongStateOf(initialTemplate?.colorHex ?: 0xFFFF7043L) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val paymentMethods = listOf("bKash", "Nagad", "Card", "Cash", "Bank Transfer", "Crypto", "Other")
    val filteredCategories = categories.filter { it.type == type }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 640.dp)
                .testTag("edit_shortcut_template_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEdit) "Edit Quick Shortcut" else "New Quick Shortcut",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Type selector
                    item {
                        TabRow(
                            selectedTabIndex = if (type == "EXPENSE") 0 else 1,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                        ) {
                            Tab(
                                selected = type == "EXPENSE",
                                onClick = {
                                    type = "EXPENSE"
                                    categoryName = categories.firstOrNull { it.type == "EXPENSE" }?.name ?: "Food & Dining"
                                },
                                text = { Text("Expense", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = type == "INCOME",
                                onClick = {
                                    type = "INCOME"
                                    categoryName = categories.firstOrNull { it.type == "INCOME" }?.name ?: "Salary / Income"
                                },
                                text = { Text("Income", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    // Title
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                errorMessage = null
                            },
                            label = { Text("Shortcut Label / Title") },
                            placeholder = { Text("e.g. Evening Snack, Rickshaw Fare") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_shortcut_title"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Amount
                    item {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = {
                                amountText = it
                                errorMessage = null
                            },
                            label = { Text("Pre-filled Amount (${currency.symbol})") },
                            placeholder = { Text("e.g. 50") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_shortcut_amount"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Category
                    item {
                        Column {
                            Text(
                                text = "Category",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(filteredCategories) { cat ->
                                    val isSelected = categoryName.equals(cat.name, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            categoryName = cat.name
                                            iconName = cat.iconName
                                            selectedColorHex = cat.colorHex
                                        },
                                        label = { Text(cat.name, fontSize = 12.sp) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = CategoryIconHelper.getIcon(cat.iconName),
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Emerald700.copy(alpha = 0.2f),
                                            selectedLabelColor = Emerald700
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Payment Method
                    item {
                        Column {
                            Text(
                                text = "Payment Method",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(paymentMethods) { method ->
                                    val isSelected = paymentMethod.equals(method, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { paymentMethod = method },
                                        label = { Text(method, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Emerald700.copy(alpha = 0.2f),
                                            selectedLabelColor = Emerald700
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Icon Picker
                    item {
                        Column {
                            Text(
                                text = "Shortcut Icon",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(CategoryIconHelper.availableIcons) { (name, vector) ->
                                    val isSelected = iconName.equals(name, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) Emerald700.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = if (isSelected) BorderStroke(1.5.dp, Emerald700) else null,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clickable { iconName = name }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = vector,
                                                contentDescription = name,
                                                tint = if (isSelected) Emerald700 else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Color Picker
                    item {
                        Column {
                            Text(
                                text = "Shortcut Color",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val paletteColors = listOf(
                                0xFFE65100L, 0xFF8E24AAL, 0xFF1E88E5L, 0xFFD81B60L,
                                0xFF2E7D32L, 0xFF00897BL, 0xFF00ACC1L, 0xFFC2185BL,
                                0xFFE53935L, 0xFF5E35B1L, 0xFF039BE5L, 0xFF8D6E63L,
                                0xFF546E7AL, 0xFFFF9800L, 0xFF4CAF50L
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(paletteColors) { colorHex ->
                                    val isSelected = selectedColorHex == colorHex
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(colorHex),
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clickable { selectedColorHex = colorHex }
                                    ) {
                                        if (isSelected) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = ExpenseRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Save button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "Please enter a shortcut title"
                                return@Button
                            }
                            val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                            if (parsedAmount <= 0.0) {
                                errorMessage = "Please enter a valid amount"
                                return@Button
                            }
                            val newOrUpdated = QuickAddTemplate(
                                id = initialTemplate?.id ?: java.util.UUID.randomUUID().toString(),
                                title = title.trim(),
                                amount = parsedAmount,
                                type = type,
                                categoryName = categoryName,
                                paymentMethod = paymentMethod,
                                iconName = iconName,
                                colorHex = selectedColorHex
                            )
                            onSave(newOrUpdated)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700),
                        modifier = Modifier.testTag("btn_save_shortcut")
                    ) {
                        Text("Save Shortcut", color = Color.White)
                    }
                }
            }
        }
    }
}
