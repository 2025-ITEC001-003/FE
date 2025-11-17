package com.example.jesafe.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jesafe.R
import com.example.jesafe.ui.theme.JeSafeTheme
import com.example.jesafe.viewmodel.AdditionalInfoViewModel
import com.example.jesafe.viewmodel.UpdateState
import kotlinx.coroutines.launch

/**
 * 신규 사용자의 추가 정보(출생년도, 성별, 기저질환)를 입력받는 화면입니다.
 *
 * 모든 정보가 유효하게 입력되면 '완료' 버튼이 활성화됩니다.
 * '완료' 버튼 클릭 시 [AdditionalInfoViewModel]을 통해 서버에 정보를 전송하고,
 * 성공하면 가입 완료 화면으로 이동합니다.
 *
 * @param navController 화면 이동을 관리하는 NavController.
 * @param name 이전 화면(로그인)에서 전달받은 사용자의 닉네임.
 * @param viewModel 추가 정보 저장 로직을 처리하는 [AdditionalInfoViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdditionalInfoScreen(
    navController: NavController,
    name: String,
    viewModel: AdditionalInfoViewModel = hiltViewModel()
) {
    var birthYear by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var hasUnderlyingDisease by remember { mutableStateOf<Boolean?>(null) }

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

    val isButtonEnabled = birthYear.isNotBlank() && gender.isNotBlank() && hasUnderlyingDisease != null

    val updateState by viewModel.updateState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UpdateState.Success -> {
                navController.navigate("registration_complete/$name") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            is UpdateState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(id = R.drawable.ic_arrow_back), contentDescription = "Back", tint = Color.DarkGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("정보를 입력해주세요!", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Black)

                Spacer(modifier = Modifier.height(48.dp))

                Text("출생년도", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = birthYear,
                    onValueChange = { birthYear = it },
                    placeholder = { Text("1980", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("성별", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = { gender = "남성" },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if(gender == "남성") Color(0xFF2CA0F3) else Color.White, contentColor = if(gender == "남성") Color.White else Color.Gray)
                    ) {
                        Text("남성")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { gender = "여성" },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if(gender == "여성") Color(0xFF2CA0F3) else Color.White, contentColor = if(gender == "여성") Color.White else Color.Gray)
                    ) {
                        Text("여성")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("기저질환", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = { hasUnderlyingDisease = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if(hasUnderlyingDisease == true) Color(0xFF2CA0F3) else Color.White, contentColor = if(hasUnderlyingDisease == true) Color.White else Color.Gray)
                    ) {
                        Text("유")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            hasUnderlyingDisease = false
                            showSpecificsInput = false
                            diseaseCategories.keys.forEach { diseaseCategories[it] = false }
                            specificDiseaseText = ""
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if(hasUnderlyingDisease == false) Color(0xFF2CA0F3) else Color.White, contentColor = if(hasUnderlyingDisease == false) Color.White else Color.Gray)
                    ) {
                        Text("무")
                    }
                }

                if (hasUnderlyingDisease == true) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("질환 종류를 선택하세요. (중복 선택 가능)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                        Text("정확한 질환명을 입력해주세요.", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = specificDiseaseText,
                            onValueChange = { specificDiseaseText = it },
                            label = { Text("질환명 입력 (예: 고혈압)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }

            Button(
                onClick = {
                    val diseaseInfo = if (hasUnderlyingDisease == true) {
                        val selectedCategories = diseaseCategories.filter { it.value }.keys.joinToString(", ")
                        if (specificDiseaseText.isNotBlank()) {
                            if (selectedCategories.isNotEmpty()) {
                                "$selectedCategories: $specificDiseaseText"
                            } else {
                                specificDiseaseText
                            }
                        } else {
                            selectedCategories.ifEmpty { null }
                        }
                    } else {
                        null
                    }
                    viewModel.saveAdditionalInfo(birthYear, gender, diseaseInfo)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2CA0F3)),
                enabled = isButtonEnabled && updateState != UpdateState.Loading
            ) {
                if (updateState == UpdateState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("완료", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun CheckboxRow(label: String, isChecked: Boolean, onChecked: () -> Unit, enabled: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onChecked)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onChecked() },
            enabled = enabled,
            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2CA0F3))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = if (enabled) Color.DarkGray else Color.Gray)
    }
}

@Preview(showBackground = true)
@Composable
fun AdditionalInfoScreenPreview() {
    JeSafeTheme {
        AdditionalInfoScreen(rememberNavController(), "홍길동")
    }
}
