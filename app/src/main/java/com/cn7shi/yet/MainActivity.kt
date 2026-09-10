package com.cn7shi.yet

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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
 * 极简记事本输入页面（带本地持久化存储）
 */
@Composable
fun NoteScreen(modifier: Modifier = Modifier) {
    // 1. 获取安卓上下文 Context（操作本地磁盘的系统句柄）
    val context = LocalContext.current

    // 2. 打开或创建本地持久化文件 "yet_prefs"（私有模式，仅本 App 能访问）
    val prefs = remember {
        context.getSharedPreferences("yet_prefs", Context.MODE_PRIVATE)
    }

    // 3. 打开 App 时先从磁盘读上次保存的内容；若无则为空字符串
    var content by remember {
        mutableStateOf(prefs.getString("note_content", "") ?: "")
    }

    // 4. 文本输入框：纯文本输入，去掉花哨的边框和指示线
    TextField(
        value = content,
        onValueChange = { newText ->
            content = newText // 刷新 UI 状态
            prefs.edit().putString("note_content", newText).apply() // 异步持久化到磁盘
        },
        placeholder = { Text("随时记录灵感...") },
        modifier = modifier.fillMaxSize(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
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