package com.example.jesafe.ui.main

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jesafe.R
import com.example.jesafe.ui.theme.JeSafeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class Message(val id: Long, val text: String, val isUser: Boolean, val timestamp: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen() {
    var messages by remember { mutableStateOf(listOf<Message>()) }
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    fun sendMessage() {
        if (text.isNotBlank()) {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.KOREA)
            timeFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")

            val currentTime = timeFormat.format(Date())
            val userMessage = Message(System.currentTimeMillis(), text, true, currentTime)
            val thinkingMessage = Message(System.currentTimeMillis() + 1, "...", false, "")

            messages = messages + userMessage + thinkingMessage
            text = ""

            coroutineScope.launch {
                listState.animateScrollToItem(0)
                delay(1000)
                val aiResponse = Message(
                    id = thinkingMessage.id,
                    text = "AI의 샘플 답변입니다. 실제로는 여기에 LLM의 응답이 들어갑니다.",
                    isUser = false,
                    timestamp = timeFormat.format(Date())
                )
                messages = messages.map { if (it.id == thinkingMessage.id) aiResponse else it }
                listState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AI 안전 도우미",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            ChatInputField(
                text = text,
                onTextChange = { text = it },
                onSendMessage = { sendMessage() }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (messages.isEmpty()) {
                // 초기 화면
                EmptyStateContent(
                    onSuggestionClick = { suggestion ->
                        text = suggestion
                        sendMessage()
                    }
                )
            } else {
                // 메시지 목록
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    reverseLayout = true
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(messages.reversed(), key = { it.id }) { message ->
                        MessageBubble(message)
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
fun EmptyStateContent(onSuggestionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // AI 아이콘
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF2CA0F3).copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_robot),
                contentDescription = "AI",
                tint = Color(0xFF2CA0F3),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 환영 메시지
        Text(
            text = "안전 AI 도우미입니다",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "교통 안전과 관련된 질문을 해보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 제안 질문들
        Text(
            text = "추천 질문",
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        val suggestions = listOf(
            "이 지역은 안전한가요?",
            "야간 운전 시 주의사항은?",
            "사고 다발 지역은 어디인가요?",
            "비오는 날 안전 운전 팁"
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

@Composable
fun SuggestionCard(text: String, onClick: () -> Unit) {
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
                .fillMaxWidth()
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

@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (message.isUser) {
                Text(message.timestamp, fontSize = 10.sp, color = Color.Gray)
            }
            Card(
                modifier = Modifier.widthIn(max = 280.dp), // 수정: 말풍선 최대 너비 제한
                shape = RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 0.dp,
                    bottomEnd = if (message.isUser) 0.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isUser) Color(0xFF2CA0F3) else Color.LightGray.copy(
                        alpha = 0.3f
                    )
                )
            ) {
                Text(
                    text = message.text,
                    color = if (message.isUser) Color.White else Color.Black,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 15.sp
                )
            }
            if (!message.isUser && message.text != "...") {
                Text(message.timestamp, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputField(
    text: String,
    onTextChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(Color.White)
            .padding(24.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF2CA0F3), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_robot),
                        contentDescription = "Bot",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("메시지를 입력하세요...", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF2CA0F3)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSendMessage() }),
                    maxLines = 5
                )
                Spacer(Modifier.width(8.dp))
                // 텍스트 입력 여부에 따라 아이콘 변경
                if (text.isNotBlank()) {
                    IconButton(onClick = onSendMessage) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color(0xFF2CA0F3)
                        )
                    }
                } else {
                    Icon(
                        painterResource(id = R.drawable.ic_mic),
                        contentDescription = "Mic",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AiChatScreenPreview() {
    JeSafeTheme {
        AiChatScreen()
    }
}
