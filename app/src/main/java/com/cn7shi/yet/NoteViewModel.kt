package com.cn7shi.yet

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

/**
 * 记事本业务领域层（纯业务大脑 / Service + DAO）
 * 遵循严格分层哲学：
 * - 只管真正的业务核心数据（content）与持久化
 * - 绝不感知任何具体的 UI 呈现方式（弹窗、按钮等属于表现层家务事）
 */
class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("yet_prefs", Context.MODE_PRIVATE)

    // 真正的业务数据状态（只读暴露，外部无法非法篡改）
    var content by mutableStateOf(prefs.getString("note_content", "") ?: "")
        private set

    // 业务动作 1：打字保存
    fun onContentChange(newText: String) {
        content = newText
        prefs.edit().putString("note_content", newText).apply()
    }

    // 业务动作 2：清空笔记业务
    fun clearNote() {
        content = ""
        prefs.edit().putString("note_content", "").apply()
    }
}
