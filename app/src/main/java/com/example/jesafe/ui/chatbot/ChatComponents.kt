package com.example.jesafe.ui.chatbot

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jesafe.R
import com.example.jesafe.ui.theme.DeepBlue
import com.example.jesafe.ui.theme.NavyBlue
import com.example.jesafe.ui.theme.SoftBlue
import kotlinx.coroutines.launch

private object AiChatScreenDefaults {
    const val DOT_ANIMATION_DURATION = 1200
    const val DOT_DELAY_1 = 0
    const val DOT_DELAY_2 = 150
    const val DOT_DELAY_3 = 300
    const val DOT_BOUNCE_START = 200
    const val DOT_BOUNCE_END = 400

    val DotSize = 7.dp
    val DotSpacing = 5.dp
    val BounceHeight = 4.dp

    // MessageBubble
    val MessageVerticalPadding = 8.dp
    val MessageHorizontalSpacing = 8.dp
    val TimestampFontSize = 10.sp
    val MaxMessageWidth = 280.dp
    val MessageShadowElevation = 2.dp
    val MessageShadowElevationSelected = 6.dp
    val MessageCornerRadius = 16.dp
    val MessageCornerRadiusSmall = 4.dp
    val MessageTextPadding = 14.dp
    val MessageTextFontSize = 15.sp
    val MessageTextLineHeight = 20.sp

    // ChatInputField
    val InputFieldPadding = 12.dp
    val InputFieldShadowElevation = 8.dp
    val InputFieldCornerRadius = 24.dp
    val InputFieldRowPadding = 8.dp
    val InputFieldIconContainerSize = 40.dp
    val InputFieldIconSize = 20.dp
    val InputFieldSpacerWidth = 8.dp

    // ContextMenu
    val ContextMenuCornerRadius = 12.dp
    val ContextMenuOffset = DpOffset(0.dp, (-8).dp)
    val ContextMenuItemHorizontalSpacing = 12.dp
    val ContextMenuIconSize = 20.dp
}

/**
 * 단일 채팅 메시지 버블을 표시합니다.
 * 메시지가 사용자의 것인지 AI의 것인지에 따라 모양이 바뀝니다.
 * AI 메시지가 비어 있으면(스트리밍 시작을 나타냄) 입력 중 표시기를 보여줍니다.
 *
 * @param message 표시할 메시지 객체입니다.
 * @param snackbarHostState "클립보드에 복사됨"과 같은 알림을 표시하는 데 사용되는 [SnackbarHostState]입니다.
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

    // When an AI message is empty, show the typing indicator.
    val isTypingIndicator = message.text.isEmpty() && !message.isUser

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
                    snackbarHostState.showSnackbar(context.getString(R.string.copied_to_clipboard))
                }
            },
            onShare = { text ->
                shareText(context, text)
                performHapticFeedback(view)
            },
            onDelete = {
                performHapticFeedback(view)
                scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.message_deleted))
                }
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = AiChatScreenDefaults.MessageVerticalPadding),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(AiChatScreenDefaults.MessageHorizontalSpacing)
        ) {
            if (message.isUser) {
                Text(message.timestamp, fontSize = AiChatScreenDefaults.TimestampFontSize, color = Color.Gray)
            }

            Box {
                Card(
                    modifier = Modifier
                        .widthIn(max = AiChatScreenDefaults.MaxMessageWidth)
                        .scale(scale)
                        .shadow(
                            elevation = if (isSelected) AiChatScreenDefaults.MessageShadowElevationSelected else AiChatScreenDefaults.MessageShadowElevation,
                            shape = RoundedCornerShape(
                                topStart = AiChatScreenDefaults.MessageCornerRadius,
                                topEnd = AiChatScreenDefaults.MessageCornerRadius,
                                bottomStart = if (message.isUser) AiChatScreenDefaults.MessageCornerRadius else AiChatScreenDefaults.MessageCornerRadiusSmall,
                                bottomEnd = if (message.isUser) AiChatScreenDefaults.MessageCornerRadiusSmall else AiChatScreenDefaults.MessageCornerRadius
                            )
                        )
                        .combinedClickable(
                            enabled = !isTypingIndicator, // Do not allow clicks while loading.
                            onClick = { },
                            onLongClick = {
                                isSelected = true
                                showMenu = true
                                performHapticFeedback(view)
                            }
                        ),
                    shape = RoundedCornerShape(
                        topStart = AiChatScreenDefaults.MessageCornerRadius, topEnd = AiChatScreenDefaults.MessageCornerRadius,
                        bottomStart = if (message.isUser) AiChatScreenDefaults.MessageCornerRadius else AiChatScreenDefaults.MessageCornerRadiusSmall,
                        bottomEnd = if (message.isUser) AiChatScreenDefaults.MessageCornerRadiusSmall else AiChatScreenDefaults.MessageCornerRadius
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
                    if (isTypingIndicator) {
                        TypingIndicator()
                    } else {
                        Text(
                            text = message.text,
                            color = if (message.isUser) Color.White else NavyBlue,
                            modifier = Modifier.padding(AiChatScreenDefaults.MessageTextPadding),
                            fontSize = AiChatScreenDefaults.MessageTextFontSize,
                            lineHeight = AiChatScreenDefaults.MessageTextLineHeight
                        )
                    }
                }

                if (!isTypingIndicator) {
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
            }

            if (!message.isUser) {
                Text(message.timestamp, fontSize = AiChatScreenDefaults.TimestampFontSize, color = Color.Gray)
            }
        }
    }
}

/**
 * [MessageBubble]에서 사용되는 애니메이션 입력 중 표시기입니다.
 * AI가 "입력 중"임을 나타내는 시각적 신호를 생성하는 세 개의 통통 튀는 점으로 구성됩니다.
 * 애니메이션은 계속 반복됩니다.
 *
 * @param modifier 표시기에 적용할 [Modifier]입니다.
 */
@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_indicator_transition")

    @Composable
    fun Dot(delay: Int) {
        val yOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = AiChatScreenDefaults.DOT_ANIMATION_DURATION
                    0f at delay
                    -AiChatScreenDefaults.BounceHeight.value at (delay + AiChatScreenDefaults.DOT_BOUNCE_START)
                    0f at (delay + AiChatScreenDefaults.DOT_BOUNCE_END)
                    0f at AiChatScreenDefaults.DOT_ANIMATION_DURATION
                }
            ), label = "dot_offset"
        )

        Box(
            modifier = Modifier
                .offset(y = yOffset.dp)
                .size(AiChatScreenDefaults.DotSize)
                .background(
                    color = NavyBlue.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        )
    }

    Row(
        modifier = modifier.padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(AiChatScreenDefaults.DotSpacing)
    ) {
        Dot(delay = AiChatScreenDefaults.DOT_DELAY_1)
        Dot(delay = AiChatScreenDefaults.DOT_DELAY_2)
        Dot(delay = AiChatScreenDefaults.DOT_DELAY_3)
    }
}

/**
 * 주어진 채팅 메시지에 대한 컨텍스트 메뉴를 표시합니다.
 * 메뉴는 메시지 복사, 공유, 삭제와 같은 옵션을 제공합니다.
 *
 * @param expanded 메뉴가 현재 표시되는지 여부입니다.
 * @param onDismiss 메뉴가 닫힐 때 실행되는 콜백입니다.
 * @param message 메뉴와 관련된 [Message] 객체입니다.
 * @param actions 각 메뉴 항목에 대한 로직을 포함하는 [MessageActions]입니다.
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
        offset = AiChatScreenDefaults.ContextMenuOffset,
        modifier = Modifier.background(Color.White, RoundedCornerShape(AiChatScreenDefaults.ContextMenuCornerRadius))
    ) {
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AiChatScreenDefaults.ContextMenuItemHorizontalSpacing)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_copy),
                        contentDescription = stringResource(R.string.copy),
                        tint = Color(0xFF2CA0F3),
                        modifier = Modifier.size(AiChatScreenDefaults.ContextMenuIconSize)
                    )
                    Text(
                        text = stringResource(R.string.copy),
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
            text = stringResource(R.string.share),
            onClick = {
                actions.onShare(message.text)
                onDismiss()
            }
        )

        if (message.text != stringResource(R.string.loading)) {
            MenuAction(
                icon = Icons.Default.Delete,
                text = stringResource(R.string.delete),
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
 * [MessageContextMenu]의 표준 항목을 만들기 위한 헬퍼 컴포저블입니다.
 *
 * @param icon 작업에 대해 표시할 아이콘입니다.
 * @param text 작업에 대한 텍스트 레이블입니다.
 * @param iconTint 아이콘의 틴트 색상입니다.
 * @param onClick 이 메뉴 항목을 클릭할 때 호출될 콜백입니다.
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
                horizontalArrangement = Arrangement.spacedBy(AiChatScreenDefaults.ContextMenuItemHorizontalSpacing)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = iconTint,
                    modifier = Modifier.size(AiChatScreenDefaults.ContextMenuIconSize)
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
 * 메시지에 대해 수행할 수 있는 작업의 콜백을 보유하는 데이터 클래스입니다.
 *
 * @param onCopy 복사 버튼을 클릭했을 때 수행할 작업입니다. 메시지 텍스트를 받습니다.
 * @param onShare 공유 버튼을 클릭했을 때 수행할 작업입니다. 메시지 텍스트를 받습니다.
 * @param onDelete 삭제 버튼을 클릭했을 때 수행할 작업입니다.
 */
private data class MessageActions(
    val onCopy: (String) -> Unit,
    val onShare: (String) -> Unit,
    val onDelete: () -> Unit
)

/**
 * 주어진 텍스트를 시스템 클립보드에 복사합니다.
 *
 * @param context Android [Context]입니다.
 * @param text 복사할 텍스트입니다.
 */
private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("message", text)
    clipboardManager.setPrimaryClip(clip)
}

/**
 * 주어진 텍스트를 공유하기 위해 시스템의 공유 시트를 엽니다.
 *
 * @param context Android [Context]입니다.
 * @param text 공유할 텍스트입니다.
 */
private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
}

/**
 * 햅틱 피드백 이벤트를 트리거합니다.
 *
 * @param view 햅틱 피드백을 수행할 [android.view.View]입니다.
 */
private fun performHapticFeedback(view: android.view.View) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    } else {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}

/**
 * 짧은 시간 동안 Toast 메시지를 표시합니다.
 *
 * @param context Android [Context]입니다.
 * @param message Toast에 표시할 메시지입니다.
 */
private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

/**
 * 채팅 메시지를 작성하고 보내기 위한 텍스트 입력 필드입니다.
 *
 * @param text 입력 필드의 현재 텍스트입니다.
 * @param onTextChange 텍스트가 변경될 때 호출되는 콜백입니다.
 * @param onSendMessage 보내기 버튼을 클릭하거나 보내기 작업이 트리거될 때 호출되는 콜백입니다.
 * @param enabled 입력 필드와 보내기 버튼의 활성화 상태를 제어합니다. false일 경우 필드가 비활성화되고 보내기 버튼을 클릭할 수 없습니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputField(
    text: String,
    onTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .background(Color.White)
            .padding(AiChatScreenDefaults.InputFieldPadding)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = AiChatScreenDefaults.InputFieldShadowElevation, shape = RoundedCornerShape(AiChatScreenDefaults.InputFieldCornerRadius)),
            shape = RoundedCornerShape(AiChatScreenDefaults.InputFieldCornerRadius),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = AiChatScreenDefaults.InputFieldRowPadding, vertical = AiChatScreenDefaults.InputFieldRowPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(AiChatScreenDefaults.InputFieldIconContainerSize)
                        .background(Color(0xFF2CA0F3), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_robot),
                        contentDescription = stringResource(R.string.ai),
                        tint = Color.White,
                        modifier = Modifier.size(AiChatScreenDefaults.InputFieldIconSize)
                    )
                }
                Spacer(Modifier.width(AiChatScreenDefaults.InputFieldSpacerWidth))
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    enabled = true,
                    placeholder = { Text(stringResource(R.string.enter_message), color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF2CA0F3)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { if (enabled) onSendMessage() }),
                    maxLines = 5
                )
                Spacer(Modifier.width(AiChatScreenDefaults.InputFieldSpacerWidth))
                if (text.isNotBlank()) {
                    IconButton(onClick = onSendMessage, enabled = enabled) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.send),
                            tint = if (enabled) Color(0xFF2CA0F3) else Color.Gray
                        )
                    }
                } else {
                    Icon(
                        painterResource(id = R.drawable.ic_mic),
                        contentDescription = stringResource(R.string.mic),
                        tint = Color.Gray,
                        modifier = Modifier.size(AiChatScreenDefaults.InputFieldIconSize)
                    )
                }
            }
        }
    }
}
