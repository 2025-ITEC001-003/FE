package com.example.jesafe.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.jesafe.ui.navigation.Routes
import com.example.jesafe.ui.theme.JeSafeTheme
import com.example.jesafe.viewmodel.AdditionalInfoViewModel

/**
 * 신규 사용자의 추가 정보 (1단계: 출생년도, 성별)를 입력받는 화면입니다.
 *
 * 화면 진입 시 전달받은 사용자 이름을 ViewModel에 저장합니다.
 * 모든 정보가 유효하게 입력되면 '다음' 버튼이 활성화됩니다.
 * '다음' 버튼 클릭 시 ViewModel에 데이터를 저장하고 다음 화면으로 이동합니다.
 *
 * @param navController 화면 이동을 관리하는 NavController.
 * @param name 이전 화면(로그인)에서 전달받은 사용자의 닉네임.
 * @param viewModel 단계별 사용자 정보를 관리하는 공유 ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdditionalInfoScreen(
    navController: NavController,
    name: String,
    viewModel: AdditionalInfoViewModel
) {
    var birthYear by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }

    // 화면이 처음 시작될 때 사용자 이름을 ViewModel에 설정
    LaunchedEffect(key1 = name) {
        viewModel.setUserName(name)
    }

    val isButtonEnabled = birthYear.length == 4 && gender.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back", // TODO: Move to strings.xml
                            tint = Color.DarkGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("거의 다 왔어요!\n안전한 여행을 위해 알려주세요", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Black)

                Spacer(modifier = Modifier.height(48.dp))

                Text("출생년도", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = birthYear,
                    onValueChange = { if (it.length <= 4) birthYear = it },
                    placeholder = { Text("YYYY", color = Color.Gray) },
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

                Spacer(modifier = Modifier.height(100.dp))
            }

            Button(
                onClick = {
                    viewModel.saveBirthAndGender(birthYear, gender)
                    navController.navigate(Routes.ADDITIONAL_INFO_CONTACT)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp) // 여백 먼저 적용
                    .padding(bottom = 24.dp)
                    .fillMaxWidth()
                    .height(56.dp), // 높이는 나중에
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2CA0F3)),
                enabled = isButtonEnabled
            ) {
                Text("다음", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AdditionalInfoScreenPreview() {
    JeSafeTheme {
        // Preview에서는 viewModel을 주입할 수 없으므로, hiltViewModel()을 직접 호출합니다.
        // 실제 앱 동작에는 영향을 미치지 않습니다.
        AdditionalInfoScreen(navController = rememberNavController(), name = "홍길동", viewModel = hiltViewModel())
    }
}
