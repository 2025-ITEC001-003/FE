package com.example.jesafe.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.jesafe.R
import com.example.jesafe.ui.theme.JeSafeTheme
import kotlinx.coroutines.delay

@Composable
fun RegistrationCompleteScreen(navController: NavController, name: String) { // name 파라미터 추가

    LaunchedEffect(Unit) {
        delay(3000)
        navController.navigate("login") {
            // MyInfoScreen(startDestination)은 남기고 그 위의 모든 화면을 제거합니다.
            popUpTo(navController.graph.startDestinationId) { inclusive = false }
            launchSingleTop = true
        }
    }

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "회원가입 완료!",
                fontSize = 20.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "${name}님,\n환영합니다!", // 수정: 변수를 중괄호로 감싸서 명확하게 함
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 50.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_notebook_pencil),
                contentDescription = null,
                modifier = Modifier.size(110.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationCompleteScreenPreview() {
    JeSafeTheme {
        // Preview에서는 "홍길동"이라는 가짜 이름을 전달하여 테스트합니다.
        RegistrationCompleteScreen(navController = rememberNavController(), "홍길동")
    }
}
