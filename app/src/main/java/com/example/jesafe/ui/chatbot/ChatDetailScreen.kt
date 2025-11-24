package com.example.jesafe.ui.chatbot

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    sessionId: String?,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var currentSessionId by remember { mutableStateOf(sessionId) }
    val newSessionId by viewModel.newSessionId.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(currentSessionId) {
        if (currentSessionId != "new" && currentSessionId != null) {
            viewModel.loadMessagesForSession(currentSessionId!!)
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
            viewModel.sendMessage(text, if (currentSessionId == "new") null else currentSessionId)
            text = ""
            keyboardController?.hide()
        }
    }

    Scaffold(
//        topBar = {
//            TopAppBar(title = { Text("Chat") })
//        },
        bottomBar = {
            ChatInputField(
                text = text,
                onTextChange = { text = it },
                onSendMessage = { sendMessage() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                    })
                }
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}
