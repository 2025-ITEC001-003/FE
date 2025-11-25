package com.example.jesafe.ui.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jesafe.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    sessionId: String?,
    navController: NavController,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isSendingMessage by viewModel.isSendingMessage.collectAsState()
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var currentSessionId by remember { mutableStateOf(sessionId) }
    val newSessionId by viewModel.newSessionId.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(sessionId) {
        if (sessionId != "new" && sessionId != null) {
            viewModel.loadMessagesForSession(sessionId)
        } else {
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(newSessionId) {
        newSessionId?.let {
            currentSessionId = it
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    fun sendMessage() {
        if (text.isNotBlank()) {
            viewModel.sendMessage(
                text,
                if (currentSessionId == "new") null else currentSessionId
            )
            text = ""
            keyboardController?.hide()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())        ) {
            // 1. 채팅 리스트
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White) // 배경색
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { keyboardController?.hide() })
                    }
            ) {
                if (messages.isEmpty()) {
                    EmptyStateContent(
                        onSuggestionClick = { suggestion ->
                            text = suggestion
                            sendMessage()
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        // 🔥 중요: 입력창 높이만큼 아래에 여백을 줘서 마지막 메시지가 가려지지 않게 함
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            bottom = 100.dp, // 입력창 대략적인 높이 + 여유공간
                            top = 64.dp      // 상단 여백
                        )
                    ) {
                        items(messages, key = { it.id }) { message ->
                            MessageBubble(
                                message = message,
                                snackbarHostState = snackbarHostState
                            )
                        }
                    }
                }
            }

            // 2. 입력창 (화면 하단에 고정 + 반투명 효과를 위해 리스트 위에 배치)
            ChatInputField(
                modifier = Modifier
                    .align(Alignment.BottomCenter), // 🔥 여기에 있던 .padding(...) 코드를 삭제하세요!
                text = text,
                onTextChange = { text = it },
                onSendMessage = { sendMessage() },
                enabled = !isSendingMessage
            )

            // 3. 뒤로가기 버튼 (그대로 유지)
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(start = 16.dp, top = 50.dp) // 위치 약간 조정
                    .shadow(elevation = 4.dp, shape = CircleShape)
                    .background(color = Color.White, shape = CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(id = R.string.ai_safe_helper)
                )
            }
        }
    }
}

/**
 * 메시지가 없을 때 표시되는 추천 질문 영역
 * string.xml 에 정의된 문자열들을 사용
 */
@Composable
fun EmptyStateContent(
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_robot),
            contentDescription = stringResource(R.string.ai),
            tint = Color(0xFF2CA0F3),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.welcome_message),
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.ask_safety_questions),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.suggested_questions),
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        val suggestions = listOf(
            stringResource(R.string.suggestion1),
            stringResource(R.string.suggestion2),
            stringResource(R.string.suggestion3),
            stringResource(R.string.suggestion4)
        )

        suggestions.forEach { suggestion ->
            SuggestionCard(
                text = suggestion,
                onClick = { onSuggestionClick(suggestion) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * 추천 질문 카드 하나
 */
@Composable
fun SuggestionCard(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_chat),
                contentDescription = null,
                tint = Color(0xFF2CA0F3),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        }
    }
}
