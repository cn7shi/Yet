package com.cn7shi.yet

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
 * 极简记事本输入页面（纯 UI 积木）
 */
@Composable
fun NoteScreen(modifier: Modifier = Modifier) {
    // 1. 定义状态（State）：用来记忆当前用户输入的内容
    var content by remember { mutableStateOf("") }

    // 2. 文本输入框：纯文本输入，去掉花哨的边框和指示线
    TextField(
        value = content,
        onValueChange = { newText -> content = newText },
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