package com.cn7shi.yet

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

/**
 * 记事本业务模型层（大脑 / Service 层）
 * 核心职责：
 * 1. 管理核心数据状态（内存）
 * 2. 负责与 SharedPreferences 通信（磁盘持久化）
 * 3. 彻底与 UI 视觉排版解耦
 */
class NoteViewModel(application: Application) : AndroidViewModel(application) {

    // 私有持久化句柄（封装在内部，不让 UI 层知道底层存储细节）
    private val prefs = application.getSharedPreferences("yet_prefs", Context.MODE_PRIVATE)

    // 1. 记事本当前的文本内容（UI 只读，只能通过 onContentChange 修改）
    var content by mutableStateOf(prefs.getString("note_content", "") ?: "")
        private set

    // 2. 二次确认弹窗的开关状态
    var showClearDialog by mutableStateOf(false)
        private set

    // 3. 计算属性：右下角清空按钮是否可见（内容非空时可见）
    val isClearButtonVisible: Boolean
        get() = content.isNotEmpty()

    // 4. 业务动作：用户打字
    fun onContentChange(newText: String) {
        content = newText
        prefs.edit().putString("note_content", newText).apply()
    }

    // 5. 业务动作：打开确认弹窗
    fun openClearDialog() {
        showClearDialog = true
    }

    // 6. 业务动作：关闭确认弹窗
    fun dismissClearDialog() {
        showClearDialog = false
    }

    // 7. 业务动作：确认清空便签
    fun confirmClear() {
        content = ""
        prefs.edit().putString("note_content", "").apply()
        showClearDialog = false
    }
}
