package com.cn7shi.yet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cn7shi.yet.ui.theme.YetTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<NoteViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NoteScreen(
                        content = viewModel.content,
                        onContentChange = viewModel::onContentChange,
                        onClearNote = viewModel::clearNote,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * 极简记事本页面（纯 UI 表现层）
 * 核心设计：
 * - 接收业务数据 content 和业务动作回调
 * - 内部自主管理 UI 交互控制状态（如弹窗开关 showDialog）
 */
@Composable
fun NoteScreen(
    content: String,
    onContentChange: (String) -> Unit,
    onClearNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 弹窗属于 UI 交互的内部瞬态（UI Ephemeral State），由视图层自己打理！
    var showClearDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. 底层：纯文本打字区域
        NoteInputField(
            content = content,
            onContentChange = onContentChange
        )

        // 2. 顶层右下角：一键清空按钮（有字时才可见）
        if (content.isNotEmpty()) {
            ClearFab(
                onClick = { showClearDialog = true },
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }

    // 3. 页面内部的二次确认弹窗
    if (showClearDialog) {
        ClearConfirmDialog(
            onConfirm = {
                onClearNote() // 触发真正的底层清空业务
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false }
        )
    }
}

/**
 * 子积木 1：纯文本输入框
 */
@Composable
private fun NoteInputField(
    content: String,
    onContentChange: (String) -> Unit
) {
    TextField(
        value = content,
        onValueChange = onContentChange,
        placeholder = { Text("随时记录灵感...") },
        modifier = Modifier.fillMaxSize(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

/**
 * 子积木 2：悬浮清空小按钮
 */
@Composable
private fun ClearFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier.padding(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "清空便签"
        )
    }
}

/**
 * 子积木 3：二次确认弹窗
 */
@Composable
private fun ClearConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("清空便签") },
        text = { Text("确定要撕掉当前这一页灵感吗？此操作无法撤销。") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("清空", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * IDE 预览器
 */
@Preview(showBackground = true)
@Composable
fun NoteScreenPreview() {
    YetTheme {
        NoteScreen(
            content = "这是一篇充满设计感的极简便签...",
            onContentChange = {},
            onClearNote = {}
        )
    }
}