package com.example.jesafe.ui.chatbot

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jesafe.R
import com.example.jesafe.data.local.ChatSessionEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AiChatScreen(
    navController: NavController,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val groupedSessions = sessions.groupBy { formatDateHeader(it.lastUpdated) }

    var sessionToDelete by remember { mutableStateOf<ChatSessionEntity?>(null) }

    if (sessionToDelete != null) {
        DeleteConfirmationDialog(
            session = sessionToDelete!!,
            onConfirm = {
                viewModel.deleteSession(sessionToDelete!!.id)
                sessionToDelete = null
            },
            onDismiss = { sessionToDelete = null }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("chatDetail/new") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Chat", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (sessions.isEmpty()) {
                EmptyChatState(onNewChatClick = { navController.navigate("chatDetail/new") })
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    groupedSessions.forEach { (date, sessionsInGroup) ->
                        stickyHeader {
                            DateHeader(date)
                        }
                        items(sessionsInGroup) { session ->
                            SessionItem(
                                session = session,
                                onClick = { navController.navigate("chatDetail/${session.id}") },
                                onLongClick = { sessionToDelete = session }
                            )
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    session: ChatSessionEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("채팅 삭제") },
        text = { Text("'${session.title}' 채팅을 정말로 삭제하시겠습니까?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("삭제", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
fun DateHeader(date: String) {
    Text(
        text = date,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}


@Composable
fun EmptyChatState(onNewChatClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF2CA0F3).copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_robot),
                contentDescription = stringResource(R.string.ai),
                tint = Color(0xFF2CA0F3),
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "아직 대화가 없습니다.")
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNewChatClick) {
            Text(text = "새로운 채팅을 시작해보세요.")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionItem(
    session: ChatSessionEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = session.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            // Placeholder for last message preview
            Text(
                text = "...", 
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = SimpleDateFormat("a hh:mm", Locale.KOREA).format(Date(session.lastUpdated)),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

fun formatDateHeader(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
    val messageDate = Calendar.getInstance().apply { timeInMillis = timestamp }
    val today = Calendar.getInstance()

    return when {
        isSameDay(messageDate, today) -> "오늘"
        isYesterday(messageDate, today) -> "어제"
        else -> sdf.format(Date(timestamp))
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
    val cal1Clone = cal1.clone() as Calendar
    cal1Clone.add(Calendar.DAY_OF_YEAR, 1)
    return isSameDay(cal1Clone, cal2)
}
