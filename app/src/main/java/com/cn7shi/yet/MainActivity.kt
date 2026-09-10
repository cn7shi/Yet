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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cn7shi.yet.ui.theme.YetTheme

class MainActivity : ComponentActivity() {
    // 注入大脑 ViewModel
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
                        isClearVisible = viewModel.isClearButtonVisible,
                        showClearDialog = viewModel.showClearDialog,
                        onOpenClearDialog = viewModel::openClearDialog,
                        onConfirmClear = viewModel::confirmClear,
                        onDismissClearDialog = viewModel::dismissClearDialog,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * 极简记事本主页面（容器组装器：总揽全局大纲）
 * 每一块具体功能都被拆成下面的独立子组件，层次极其清晰！
 */
@Composable
fun NoteScreen(
    content: String,
    onContentChange: (String) -> Unit,
    isClearVisible: Boolean,
    showClearDialog: Boolean,
    onOpenClearDialog: () -> Unit,
    onConfirmClear: () -> Unit,
    onDismissClearDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 1. 底层：纯文本打字区域
        NoteInputField(
            content = content,
            onContentChange = onContentChange
        )

        // 2. 顶层右下角：一键清空按钮（带状态控制）
        if (isClearVisible) {
            ClearFab(
                onClick = onOpenClearDialog,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }

    // 3. 二次确认弹窗
    if (showClearDialog) {
        ClearConfirmDialog(
            onConfirm = onConfirmClear,
            onDismiss = onDismissClearDialog
        )
    }
}

/**
 * 子组件 1：纯文本输入框（只负责打字视觉）
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
 * 子组件 2：悬浮清空小按钮（只负责按钮本身）
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
 * 子组件 3：二次确认弹窗（只负责弹窗本身）
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
 * IDE 预览器：因为解耦了，我们可以随时随地塞假数据预览不同状态！
 */
@Preview(showBackground = true)
@Composable
fun NoteScreenPreview() {
    YetTheme {
        NoteScreen(
            content = "这是一篇充满设计感的极简便签...",
            onContentChange = {},
            isClearVisible = true,
            showClearDialog = false,
            onOpenClearDialog = {},
            onConfirmClear = {},
            onDismissClearDialog = {}
        )
    }
}