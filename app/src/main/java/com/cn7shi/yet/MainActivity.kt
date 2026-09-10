package com.cn7shi.yet

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cn7shi.yet.ui.theme.YetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // 把带安全边距的 modifier 规格书传递给我们的记事页面
                    NoteScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * 极简记事本输入页面（带本地持久化存储 + 一键清空）
 */
@Composable
fun NoteScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences("yet_prefs", Context.MODE_PRIVATE)
    }

    var content by remember {
        mutableStateOf(prefs.getString("note_content", "") ?: "")
    }

    // 控制是否展示清空二次确认弹窗的状态
    var showClearDialog by remember { mutableStateOf(false) }

    // 使用 Box 层叠容器：底层是输入框，右下角浮动清空按钮
    Box(modifier = modifier.fillMaxSize()) {
        // 1. 底层：全屏纯文本输入框
        TextField(
            value = content,
            onValueChange = { newText ->
                content = newText
                prefs.edit().putString("note_content", newText).apply()
            },
            placeholder = { Text("随时记录灵感...") },
            modifier = Modifier.fillMaxSize(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        // 2. 顶层右下角：一键清空悬浮按钮（只有输入框有字时才优雅展示，无字自动隐藏！）
        if (content.isNotEmpty()) {
            SmallFloatingActionButton(
                onClick = { showClearDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "清空便签"
                )
            }
        }
    }

    // 3. 二次确认弹窗：声明式弹窗机制，直接由布尔状态 showClearDialog 驱动
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空便签") },
            text = { Text("确定要撕掉当前这一页灵感吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        content = "" // 清空内存
                        prefs.edit().putString("note_content", "").apply() // 清空磁盘
                        showClearDialog = false // 关掉弹窗
                    }
                ) {
                    Text("清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * IDE 专属预览器
 */
@Preview(showBackground = true)
@Composable
fun NoteScreenPreview() {
    YetTheme {
        NoteScreen()
    }
}