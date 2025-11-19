package com.example.jesafe.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
 * 신규 사용자의 추가 정보 (2단계: 이메일, 전화번호)를 입력받는 화면입니다.
 *
 * 모든 정보가 유효하게 입력되면 '다음' 버튼이 활성화됩니다.
 * '다음' 버튼 클릭 시 ViewModel에 데이터를 저장하고 다음 화면으로 이동합니다.
 *
 * @param navController 화면 이동을 관리하는 NavController.
 * @param viewModel 단계별 사용자 정보를 관리하는 공유 ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdditionalInfoContactScreen(
    navController: NavController,
    viewModel: AdditionalInfoViewModel
) {
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    val isEmailValid = email.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPhoneNumberValid = phoneNumber.isNotEmpty() // TODO: Add more specific phone number validation

    val isButtonEnabled = isEmailValid && isPhoneNumberValid

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
                Text("연락처를 알려주세요", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.Black)

                Spacer(modifier = Modifier.height(48.dp))

                Text("이메일", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("example@mail.com", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(10.dp),
                    isError = email.isNotEmpty() && !isEmailValid
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("전화번호", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    placeholder = { Text("010-1234-5678", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(100.dp))
            }

            Button(
                onClick = {
                    viewModel.saveContact(email, phoneNumber)
                    navController.navigate(Routes.ADDITIONAL_INFO_DISEASE)
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
fun AdditionalInfoContactScreenPreview() {
    JeSafeTheme {
        AdditionalInfoContactScreen(navController = rememberNavController(), viewModel = hiltViewModel())
    }
}
