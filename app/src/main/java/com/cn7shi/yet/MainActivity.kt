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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cn7shi.yet.ui.theme.YetTheme

/**
 * 页面枚举：定义底部导航的 4 个占位标签
 */
enum class Screen(val label: String, val icon: ImageVector) {
    PAGE_A("页面 A", Icons.Default.Home),
    PAGE_B("页面 B", Icons.AutoMirrored.Filled.List),
    PAGE_C("页面 C", Icons.Default.Favorite),
    PAGE_D("页面 D", Icons.Default.Settings)
}

/**
 * 1. Thin Activity 规范：MainActivity 彻底瘦身为纯粹的开机插头！
 */
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<NoteViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YetApp(viewModel = viewModel)
        }
    }
}

/**
 * 2. 整个 App 的根总成容器（一马平川，无深层嵌套）
 */
@Composable
fun YetApp(viewModel: NoteViewModel) {
    YetTheme {
        // 当前选中的 Tab 状态
        var currentScreen by remember { mutableStateOf(Screen.PAGE_A) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                YetBottomBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it }
                )
            }
        ) { innerPadding ->
            NavigationContent(
                currentScreen = currentScreen,
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

/**
 * 3. 独立底栏组件：负责渲染 4 个导航按钮
 */
@Composable
private fun YetBottomBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar {
        Screen.values().forEach { screen ->
            NavigationBarItem(
                selected = (currentScreen == screen),
                onClick = { onScreenSelected(screen) },
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) }
            )
        }
    }
}

/**
 * 4. 独立内容路由组件：根据选中的 Screen 分发显示具体页面
 */
@Composable
private fun NavigationContent(
    currentScreen: Screen,
    viewModel: NoteViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (currentScreen) {
            Screen.PAGE_A -> NoteScreen(
                content = viewModel.content,
                onContentChange = viewModel::onContentChange,
                onClearNote = viewModel::clearNote
            )
            Screen.PAGE_B -> PlaceholderScreen("这是占位页面 B")
            Screen.PAGE_C -> PlaceholderScreen("这是占位页面 C")
            Screen.PAGE_D -> PlaceholderScreen("这是占位页面 D")
        }
    }
}

/**
 * 占位页面组件：居中显示大标题
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}

/**
 * 业务页面：极简便签（保留此前完美的解耦设计）
 */
@Composable
fun NoteScreen(
    content: String,
    onContentChange: (String) -> Unit,
    onClearNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        NoteInputField(
            content = content,
            onContentChange = onContentChange
        )

        if (content.isNotEmpty()) {
            ClearFab(
                onClick = { showClearDialog = true },
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }

    if (showClearDialog) {
        ClearConfirmDialog(
            onConfirm = {
                onClearNote()
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false }
        )
    }
}

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

@Preview(showBackground = true)
@Composable
fun YetAppPreview() {
    YetTheme {
        PlaceholderScreen("Yet App Preview")
    }
}