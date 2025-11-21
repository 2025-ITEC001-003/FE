package com.example.jesafe.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jesafe.ui.theme.JeSafeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnderlyingDiseaseScreen(navController: NavController) {
    var hasDisease by remember { mutableStateOf<Boolean?>(null) }
    val diseaseCategories = remember {
        mutableStateMapOf(
            "심혈관계 질환" to false,
            "호흡기 질환" to false,
            "내분비 질환" to false,
            "신경계 질환" to false,
            "정신 질환" to false,
            "기타" to false
        )
    }
    var specificDiseaseText by remember { mutableStateOf("") }
    var showSpecificsInput by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("기저질환 관리") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White, titleContentColor = Color.Black)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val result = if (hasDisease == true) {
                        val selectedCategories = diseaseCategories.filter { it.value }.keys.joinToString(", ")
                        if (specificDiseaseText.isNotBlank()) {
                            "$selectedCategories: $specificDiseaseText"
                        } else if (selectedCategories.isNotEmpty()) {
                            selectedCategories
                        } else {
                            "유"
                        }
                    } else if (hasDisease == false) {
                        ""
                    } else {
                        null
                    }

                    if (result != null) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("newDiseaseInfo", result)
                    }
                    navController.navigateUp()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0A2A61)
                )
            ) {
                Text("저장", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("기저질환이 있습니까?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioSelectRow(text = "유", selected = hasDisease == true) {
                    hasDisease = true
                    showSpecificsInput = false
                }
                RadioSelectRow(text = "무", selected = hasDisease == false) {
                    hasDisease = false
                    showSpecificsInput = false
                    diseaseCategories.keys.forEach { diseaseCategories[it] = false }
                }
            }

            if (hasDisease == true) {
                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("질환 종류를 선택하세요. (중복 선택 가능)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (showSpecificsInput) {
                        TextButton(onClick = { showSpecificsInput = false }) {
                            Text("수정")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                diseaseCategories.keys.forEach { category ->
                    if (!showSpecificsInput || diseaseCategories.getValue(category)) {
                        CheckboxRow(
                            label = category,
                            isChecked = diseaseCategories.getValue(category),
                            onChecked = {
                                if (!showSpecificsInput) {
                                    diseaseCategories[category] = !diseaseCategories.getValue(category)
                                }
                            },
                            enabled = !showSpecificsInput
                        )
                    }
                }

                val anyCategorySelected = diseaseCategories.containsValue(true)
                if (anyCategorySelected && !showSpecificsInput) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showSpecificsInput = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("선택 완료")
                    }
                }

                if (showSpecificsInput) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(24.dp))

                    Text("정확한 질환명을 입력해주세요.", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = specificDiseaseText,
                        onValueChange = { specificDiseaseText = it },
                        label = { Text("질환명 입력") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun RadioSelectRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .selectable(selected = selected, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text)
    }
}

@Composable
private fun CheckboxRow(label: String, isChecked: Boolean, onChecked: () -> Unit, enabled: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onChecked)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isChecked, onCheckedChange = { onChecked() }, enabled = enabled)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}

@Preview(showBackground = true)
@Composable
fun UnderlyingDiseaseScreenPreview() {
    JeSafeTheme {
        UnderlyingDiseaseScreen(rememberNavController())
    }
}
