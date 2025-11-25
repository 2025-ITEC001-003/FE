package com.example.jesafe.ui.chatbot

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jesafe.R
import com.example.jesafe.data.local.ChatSessionWithPreview
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * AI 채팅 세션 목록을 보여주는 화면 컴포저블.
 *
 * - 날짜별로 세션을 그룹화하여 섹션 헤더와 함께 표시한다.
 * - FloatingActionButton을 통해 새로운 채팅을 생성할 수 있다.
 * - 세션 롱클릭 시 카드 전체가 회색으로 하이라이트되고,
 *   해당 카드 바로 아래에 삭제 메뉴가 표시된다.
 *
 * @param navController 채팅 상세 화면으로 이동하기 위한 NavController
 * @param viewModel 채팅 세션 목록을 제공하는 [AiChatViewModel]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    navController: NavController,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val groupedSessions = sessions.groupBy { formatDateHeader(it.session.lastUpdated) }

    var sessionToDelete by remember { mutableStateOf<ChatSessionWithPreview?>(null) }

    if (sessionToDelete != null) {
        DeleteConfirmationDialog(
            session = sessionToDelete!!,
            onConfirm = {
                viewModel.deleteSession(sessionToDelete!!.session.id)
                sessionToDelete = null
            },
            onDismiss = { sessionToDelete = null }
        )
    }

    Scaffold(
        containerColor = Color.White,
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    groupedSessions.entries.forEachIndexed { index, (date, sessionsInGroup) ->

                        // 날짜 섹션마다 위쪽 여백 추가
                        stickyHeader {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                            ) {
                                // 첫 섹션은 살짝, 그 다음 섹션부터는 좀 더 넓게
                                Spacer(modifier = Modifier.height(if (index == 0) 8.dp else 16.dp))
                                DateHeader(date)
                            }
                        }

                        items(
                            items = sessionsInGroup,
                            key = { it.session.id }
                        ) { session ->
                            SessionItem(
                                sessionWithLastMessage = session,
                                onClick = {
                                    navController.navigate("chatDetail/${session.session.id}")
                                },
                                onDeleteRequest = {
                                    sessionToDelete = session
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 채팅 세션 삭제 전 사용자에게 확인을 받기 위한 다이얼로그.
 *
 * @param session 삭제 대상 채팅 세션 정보
 * @param onConfirm 삭제 확정 시 호출되는 콜백
 * @param onDismiss 다이얼로그를 닫을 때 호출되는 콜백
 */
@Composable
fun DeleteConfirmationDialog(
    session: ChatSessionWithPreview,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("채팅 삭제") },
        text = { Text("'${session.session.title}' 채팅을 정말로 삭제하시겠습니까?") },
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

/**
 * 채팅 세션 목록에서 날짜 섹션을 구분하기 위한 헤더 텍스트.
 *
 * @param date 섹션에 표시할 날짜 문자열 (예: "오늘", "어제", "2025.11.25")
 */
@Composable
fun DateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White) // 스크롤해도 깔끔하게
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 왼쪽 라인
            Divider(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            // 가운데 알약 모양 날짜 배지
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 오른쪽 라인
            Divider(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}

/**
 * 채팅 세션이 하나도 없을 때 표시되는 빈 상태 화면.
 *
 * @param onNewChatClick 새 채팅 생성 액션을 실행하는 콜백
 */
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
    sessionWithLastMessage: ChatSessionWithPreview,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val view = LocalView.current
    var isSelected by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        Color.Transparent
    }

    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null, // No visual ripple, selection is shown by bg color
                    onClick = {
                        isSelected = false
                        showMenu = false
                        onClick()
                    },
                    onLongClick = {
                        isSelected = true
                        showMenu = true
                        performHapticFeedback(view)
                    }
                )
                .background(backgroundColor) // Highlight for selection
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = sessionWithLastMessage.session.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = sessionWithLastMessage.lastMessage ?: "...",
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fadingEdge(
                            Brush.horizontalGradient(
                                0.8f to Color.Black,
                                1.0f to Color.Transparent
                            )
                        )
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = SimpleDateFormat(
                        "a hh:mm",
                        Locale.KOREA
                    ).format(Date(sessionWithLastMessage.session.lastUpdated)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SessionContextMenu(
                expanded = showMenu,
                onDismiss = {
                    showMenu = false
                    isSelected = false
                },
                onDelete = {
                    onDeleteRequest()
                    showMenu = false
                    isSelected = false
                }
            )
        }

        Divider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}

/**
 * 세션 롱클릭 시 표시되는 컨텍스트 메뉴.
 *
 * 현재는 삭제 옵션만 제공하며, SessionItem 내부에서
 * 카드 아래에 배치되어 사용된다.
 *
 * @param expanded 메뉴 표시 여부
 * @param onDismiss 메뉴가 닫힐 때 호출되는 콜백
 * @param onDelete 삭제 항목 선택 시 호출되는 콜백
 */
@Composable
private fun SessionContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(0.dp, 0.dp),
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
    ) {
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "삭제",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            onClick = onDelete
        )
    }
}

/**
 * 오른쪽 끝으로 갈수록 투명해지는 페이딩 효과를 적용하는 Modifier 확장 함수.
 *
 * - 긴 텍스트가 잘리는 구간을 자연스럽게 보이도록 하기 위해 사용한다.
 *
 * @param brush 콘텐츠에 적용할 그라디언트 브러시
 * @return 페이딩 효과가 적용된 [Modifier]
 */
private fun Modifier.fadingEdge(brush: Brush): Modifier =
    this.graphicsLayer(alpha = 0.99f)
        .drawWithContent {
            drawContent()
            drawRect(
                brush = brush,
                blendMode = BlendMode.DstIn,
            )
        }

/**
 * 롱클릭 시 촉각 피드백을 발생시키는 유틸 함수.
 *
 * - Android 11(R) 이상에서는 [HapticFeedbackConstants.CONFIRM]를 사용하고,
 *   그 이전 버전에서는 [HapticFeedbackConstants.LONG_PRESS]를 사용한다.
 *
 * @param view 촉각 피드백을 발생시킬 대상 뷰
 */
private fun performHapticFeedback(view: android.view.View) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    } else {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}

/**
 * 타임스탬프를 채팅 목록 헤더에 사용할 날짜 문자열로 변환한다.
 *
 * - 오늘 날짜인 경우 "오늘"
 * - 어제 날짜인 경우 "어제"
 * - 그 외에는 "yyyy.MM.dd" 형식으로 반환한다.
 *
 * @param timestamp 밀리초 단위의 UTC 타임스탬프
 * @return 포맷팅된 날짜 문자열
 */
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

/**
 * 두 [Calendar] 객체가 같은 날짜(연도 및 연중 일 수 기준)인지 비교한다.
 *
 * @param cal1 첫 번째 캘린더
 * @param cal2 두 번째 캘린더
 * @return 같은 날이면 `true`, 아니면 `false`
 */
fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

/**
 * 첫 번째 [Calendar]가 두 번째 [Calendar]의 바로 전날(어제)인지 판별한다.
 *
 * @param cal1 비교 대상 날짜 (어제인지 확인하고 싶은 날짜)
 * @param cal2 기준 날짜 (보통 오늘 날짜)
 * @return cal1이 cal2의 전날이면 `true`, 아니면 `false`
 */
fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
    val cal1Clone = cal1.clone() as Calendar
    cal1Clone.add(Calendar.DAY_OF_YEAR, 1)
    return isSameDay(cal1Clone, cal2)
}
