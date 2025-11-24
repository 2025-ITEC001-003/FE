package com.example.jesafe.ui.chatbot

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
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
}

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

    val isTypingIndicator = message.text == stringResource(R.string.loading)

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
                            enabled = !isTypingIndicator, // 로딩 중에는 클릭 비활성화
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
                    if (isTypingIndicator) {
                        TypingIndicator()
                    } else {
                        Text(
                            text = message.text,
                            color = if (message.isUser) Color.White else NavyBlue,
                            modifier = Modifier.padding(14.dp),
                            fontSize = 15.sp,
                            lineHeight = 20.sp
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

            if (!message.isUser && !isTypingIndicator) {
                Text(message.timestamp, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

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
                },
                repeatMode = RepeatMode.Restart
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
                        contentDescription = stringResource(R.string.copy),
                        tint = Color(0xFF2CA0F3),
                        modifier = Modifier.size(20.dp)
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

private data class MessageActions(
    val onCopy: (String) -> Unit,
    val onShare: (String) -> Unit,
    val onDelete: () -> Unit
)

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("message", text)
    clipboardManager.setPrimaryClip(clip)
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
}

private fun performHapticFeedback(view: android.view.View) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    } else {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
                        contentDescription = stringResource(R.string.ai),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.enter_message), color = Color.Gray) },
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
                            contentDescription = stringResource(R.string.send),
                            tint = Color(0xFF2CA0F3)
                        )
                    }
                } else {
                    Icon(
                        painterResource(id = R.drawable.ic_mic),
                        contentDescription = stringResource(R.string.mic),
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
