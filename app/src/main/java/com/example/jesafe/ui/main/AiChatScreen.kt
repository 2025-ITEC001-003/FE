package com.example.jesafe.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jesafe.R
import com.example.jesafe.ui.theme.JeSafeTheme
import com.example.jesafe.ui.theme.OffWhite
import com.example.jesafe.ui.theme.LightGrey
import com.example.jesafe.ui.theme.LightSkyBlue
import com.example.jesafe.ui.theme.NavyBlue
import com.example.jesafe.ui.theme.SoftBlue
import com.example.jesafe.ui.theme.DeepBlue
import kotlinx.coroutines.launch

/**
 * 채팅 메시지를 나타내는 데이터 클래스
 *
 * @property id 메시지 고유 식별자
 * @property text 메시지 내용
 * @property isUser 사용자가 보낸 메시지 여부 (true: 사용자, false: AI)
 * @property timestamp 메시지 전송 시간
 */
data class Message(val id: Long, val text: String, val isUser: Boolean, val timestamp: String)

/**
 * AI 채팅 화면 메인 컴포저블
 *
 * 사용자와 AI 간의 대화를 표시하고 관리합니다.
 * 메시지가 없을 때는 추천 질문을 표시하고, 메시지가 있을 때는 대화 목록을 표시합니다.
 *
 * @param viewModel AI 채팅 뷰모델 (Hilt를 통해 자동 주입)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    fun sendMessage() {
        if (text.isNotBlank()) {
            viewModel.sendMessage(text)
            text = ""
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        containerColor = OffWhite,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AI 안전 도우미",
                        style = MaterialTheme.typography.titleLarge,
                        color = NavyBlue
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
                .background(OffWhite)
                .padding(innerPadding)
        ) {
            SubtleBackgroundPattern()
            
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    reverseLayout = true
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(messages.reversed(), key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            snackbarHostState = snackbarHostState
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

/**
 * 미세한 점(Dot) 패턴 배경
 *
 * Canvas를 사용하여 매우 옅은 점 패턴을 그립니다.
 * 배경을 밋밋하지 않게 만들어주는 디테일 요소입니다.
 */
@Composable
fun SubtleBackgroundPattern() {
    val density = LocalDensity.current
    Canvas(modifier = Modifier.fillMaxSize()) {
        val dotRadius = with(density) { 1.dp.toPx() }
        val spacing = with(density) { 20.dp.toPx() }
        val dotColor = LightGrey.copy(alpha = 0.1f)

        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(x, y)
                )
                x += spacing
            }
            y += spacing
        }
    }
}

/**
 * 메시지가 없을 때 표시되는 초기 화면
 *
 * AI 아이콘, 환영 메시지, 추천 질문 카드를 표시합니다.
 *
 * @param onSuggestionClick 추천 질문 클릭 시 호출되는 콜백
 */
@Composable
fun EmptyStateContent(onSuggestionClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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

/**
 * 추천 질문 카드 컴포저블
 *
 * 클릭 가능한 질문 카드를 표시합니다.
 *
 * @param text 표시할 질문 텍스트
 * @param onClick 카드 클릭 시 호출되는 콜백
 */
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

/**
 * 메시지 버블 컴포저블
 *
 * 사용자 또는 AI의 메시지를 말풍선 형태로 표시합니다.
 * 길게 누르면 복사, 공유, 삭제 옵션이 있는 컨텍스트 메뉴가 나타납니다.
 * 선택 시 시각적 피드백(스케일 애니메이션, 색상 변경, 그림자 효과)을 제공합니다.
 *
 * @param message 표시할 메시지 객체
 * @param snackbarHostState 스낵바 표시를 위한 HostState
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    var isSelected by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 0.98f else 1f,
        label = "message_scale"
    )

    val messageActions = remember {
        MessageActions(
            onCopy = { text ->
                copyToClipboard(context, text)
                performHapticFeedback(view)
                scope.launch {
                    snackbarHostState.showSnackbar("📋 클립보드에 복사되었어요")
                }
            },
            onShare = { text ->
                shareText(context, text)
                performHapticFeedback(view)
            },
            onDelete = {
                performHapticFeedback(view)
                scope.launch {
                    snackbarHostState.showSnackbar("🗑️ 메시지가 삭제되었습니다")
                }
            }
        )
    }

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

            Box {
                Card(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .scale(scale)
                        .shadow(
                            elevation = if (isSelected) 6.dp else 2.dp,
                            shape = RoundedCornerShape(
                                topStart = 16.dp, topEnd = 16.dp,
                                bottomStart = if (message.isUser) 16.dp else 4.dp,
                                bottomEnd = if (message.isUser) 4.dp else 16.dp
                            )
                        )
                        .combinedClickable(
                            onClick = { },
                            onLongClick = {
                                isSelected = true
                                showMenu = true
                                performHapticFeedback(view)
                            }
                        ),
                    shape = RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isUser) 4.dp else 16.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isSelected && message.isUser -> DeepBlue
                            isSelected && !message.isUser -> Color(0xFFE0E0E0)
                            message.isUser -> SoftBlue
                            else -> Color.White
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Text(
                        text = message.text,
                        color = if (message.isUser) Color.White else NavyBlue,
                        modifier = Modifier.padding(14.dp),
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                }

                MessageContextMenu(
                    expanded = showMenu,
                    onDismiss = {
                        showMenu = false
                        isSelected = false
                    },
                    message = message,
                    actions = messageActions
                )
            }

            if (!message.isUser && message.text != "...") {
                Text(message.timestamp, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

/**
 * 메시지 컨텍스트 메뉴
 *
 * 메시지를 길게 눌렀을 때 표시되는 드롭다운 메뉴입니다.
 * 복사, 공유, 삭제 액션을 제공합니다.
 *
 * @param expanded 메뉴 표시 여부
 * @param onDismiss 메뉴 닫기 콜백
 * @param message 대상 메시지
 * @param actions 메시지 액션 콜백 모음
 */
@Composable
private fun MessageContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    message: Message,
    actions: MessageActions
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(0.dp, (-8).dp),
        modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
    ) {
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_copy),
                        contentDescription = "복사하기",
                        tint = Color(0xFF2CA0F3),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "복사하기",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            },
            onClick = {
                actions.onCopy(message.text)
                onDismiss()
            }
        )

        MenuAction(
            icon = Icons.Default.Share,
            text = "공유하기",
            onClick = {
                actions.onShare(message.text)
                onDismiss()
            }
        )

        if (!message.text.contains("...")) {
            MenuAction(
                icon = Icons.Default.Delete,
                text = "삭제하기",
                iconTint = Color(0xFFE53935),
                onClick = {
                    actions.onDelete()
                    onDismiss()
                }
            )
        }
    }
}

/**
 * 메뉴 아이템 컴포넌트
 *
 * ImageVector 아이콘을 사용하는 드롭다운 메뉴 아이템입니다.
 *
 * @param icon 표시할 아이콘 (ImageVector)
 * @param text 메뉴 아이템 텍스트
 * @param iconTint 아이콘 색상 (기본값: 파란색)
 * @param onClick 클릭 시 호출되는 콜백
 */
@Composable
private fun MenuAction(
    icon: ImageVector,
    text: String,
    iconTint: Color = Color(0xFF2CA0F3),
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (iconTint == Color(0xFFE53935)) Color(0xFFE53935) else Color.Black
                )
            }
        },
        onClick = onClick
    )
}

/**
 * 메시지 액션을 담는 데이터 클래스
 *
 * 메시지에 대한 사용자 액션(복사, 공유, 삭제)의 콜백을 포함합니다.
 *
 * @property onCopy 메시지 복사 액션
 * @property onShare 메시지 공유 액션
 * @property onDelete 메시지 삭제 액션
 */
private data class MessageActions(
    val onCopy: (String) -> Unit,
    val onShare: (String) -> Unit,
    val onDelete: () -> Unit
)

/**
 * 클립보드에 텍스트를 복사합니다.
 *
 * @param context Android 컨텍스트
 * @param text 복사할 텍스트
 */
private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("message", text)
    clipboardManager.setPrimaryClip(clip)
}

/**
 * 텍스트를 다른 앱과 공유합니다.
 *
 * Android 공유 시트를 통해 텍스트를 공유할 수 있는 앱 목록을 표시합니다.
 *
 * @param context Android 컨텍스트
 * @param text 공유할 텍스트
 */
private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "공유하기"))
}

/**
 * 햅틱 피드백을 실행합니다.
 *
 * Android R(API 30) 이상에서는 CONFIRM, 그 이전 버전에서는 LONG_PRESS 피드백을 사용합니다.
 *
 * @param view 햅틱 피드백을 수행할 뷰
 */
private fun performHapticFeedback(view: android.view.View) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    } else {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}

/**
 * 토스트 메시지를 표시합니다.
 *
 * @param context Android 컨텍스트
 * @param message 표시할 메시지
 */
private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

/**
 * 채팅 입력 필드 컴포저블
 *
 * 하단에 고정되는 메시지 입력 필드입니다.
 * AI 아이콘, 텍스트 입력 필드, 전송/마이크 버튼으로 구성됩니다.
 * 텍스트가 입력되면 마이크 아이콘이 전송 버튼으로 변경됩니다.
 *
 * @param text 현재 입력된 텍스트
 * @param onTextChange 텍스트 변경 시 호출되는 콜백
 * @param onSendMessage 메시지 전송 시 호출되는 콜백
 */
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

/**
 * AI 채팅 화면 미리보기
 */
@Preview(showBackground = true)
@Composable
fun AiChatScreenPreview() {
    JeSafeTheme {
        AiChatScreen()
    }
}
