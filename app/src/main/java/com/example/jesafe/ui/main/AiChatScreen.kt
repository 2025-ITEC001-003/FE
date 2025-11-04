package com.example.jesafe.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Message(val text: String, val isUser: Boolean, val timestamp: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen() {
    var messages by remember { mutableStateOf(listOf<Message>()) }
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    fun sendMessage() {
        if (text.isNotBlank()) {
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val newMessages = messages + Message(text, true, currentTime)
            messages = newMessages + Message("...", false, currentTime)
            text = "" // 입력창 초기화
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            ChatInputField(
                text = text,
                onTextChange = { text = it },
                onSendMessage = { sendMessage() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                MessageBubble(message)
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (message.isUser) {
                Text(message.timestamp, fontSize = 10.sp, color = Color.Gray)
            }
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp, topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 0.dp,
                    bottomEnd = if (message.isUser) 0.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isUser) Color(0xFF2CA0F3) else Color.LightGray.copy(alpha = 0.3f)
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
            .padding(8.dp)
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
                    Icon(painterResource(id = R.drawable.ic_robot), contentDescription = "Bot", tint = Color.White, modifier = Modifier.size(24.dp))
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
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF2CA0F3))
                    }
                } else {
                    Icon(painterResource(id = R.drawable.ic_mic), contentDescription = "Mic", tint = Color.Gray, modifier = Modifier.size(24.dp))
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
