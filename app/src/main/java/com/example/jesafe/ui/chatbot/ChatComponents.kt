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
    val InputFieldPadding = 24.dp
    val InputFieldShadowElevation = 8.dp
    val InputFieldCornerRadius = 24.dp
    val InputFieldRowPadding = 8.dp
    val InputFieldIconContainerSize = 40.dp
    val InputFieldIconSize = 24.dp
    val InputFieldSpacerWidth = 8.dp

    // ContextMenu
    val ContextMenuCornerRadius = 12.dp
    val ContextMenuOffset = DpOffset(0.dp, (-8).dp)
    val ContextMenuItemHorizontalSpacing = 12.dp
    val ContextMenuIconSize = 20.dp
}

/**
 * Displays a single chat message bubble.
 * It adapts its appearance based on whether the message is from the user or the AI.
 * When the AI message is empty (i.e., streaming is about to start), it shows a typing indicator.
 *
 * @param message The message to display.
 * @param snackbarHostState The [SnackbarHostState] to show notifications (e.g., "Copied to clipboard").
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
 * An animated typing indicator used in [MessageBubble].
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

/**
 * A text input field for composing and sending chat messages.
 *
 * @param text The current text in the input field.
 * @param onTextChange A callback invoked when the text changes.
 * @param onSendMessage A callback invoked when the send button is clicked or send action is triggered.
 * @param enabled Controls the enabled state of the input field and send button. When false, the field is disabled and the send button is not clickable.
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
                    enabled = enabled,
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
                    keyboardActions = KeyboardActions(onSend = { if (enabled) onSendMessage() }),
                    maxLines = 5
                )
                Spacer(Modifier.width(AiChatScreenDefaults.InputFieldSpacerWidth))
                if (text.isNotBlank()) {
                    IconButton(onClick = onSendMessage, enabled = enabled) {
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
                        modifier = Modifier.size(AiChatScreenDefaults.InputFieldIconSize)
                    )
                }
            }
        }
    }
}
