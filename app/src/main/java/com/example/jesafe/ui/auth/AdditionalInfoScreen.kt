package com.example.jesafe.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jesafe.R
import com.example.jesafe.ui.theme.JeSafeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdditionalInfoScreen(navController: NavController, name: String) { // name 파라미터 추가
    var birthYear by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var hasUnderlyingDisease by remember { mutableStateOf<Boolean?>(null) }
    val isButtonEnabled = birthYear.isNotBlank() && gender.isNotBlank() && hasUnderlyingDisease != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(id = R.drawable.ic_arrow_back), contentDescription = "Back", tint = Color.DarkGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 24.dp),
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
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFEDF1F3),
                    focusedBorderColor = Color(0xFF2CA0F3)
                )
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
                    onClick = { hasUnderlyingDisease = false },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if(hasUnderlyingDisease == false) Color(0xFF2CA0F3) else Color.White, contentColor = if(hasUnderlyingDisease == false) Color.White else Color.Gray)
                ) {
                    Text("무")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { navController.navigate("registration_complete/$name") }, // 수정: 전달받은 이름을 다시 경로에 추가
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2CA0F3)),
                    enabled = isButtonEnabled
                ) {
                    Text("완료", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("계정이 있다면?", color = Color.Gray, fontSize = 14.sp)
                    TextButton(onClick = { navController.navigate("login") }) {
                        Text("로그인", color = Color(0xFF2CA0F3), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp)) // 간격 조정
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdditionalInfoScreenPreview() {
    JeSafeTheme {
        // Preview에서는 "홍길동"이라는 가짜 이름을 전달하여 테스트합니다.
        AdditionalInfoScreen(rememberNavController(), "홍길동")
    }
}
