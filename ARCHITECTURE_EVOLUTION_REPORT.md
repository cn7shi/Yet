# 《Yet》开发小记：关于代码可读性的一点思考

## 我的原始思考

我主要学 Java，对 Compose 还不熟悉。做这个小记事本时，最初的代码把 UI、数据和各种操作全放在一起，我读着有些费劲，就问 AI：能不能拆开？

AI 给了我一版它称为 MVVM 的拆分。但我看完后觉得，虽然代码分开了，**对于数据的控制和对于 UI 的控制，还是混在一起。**

按照我的理解，修改、保存、清空数据是一类事情；控制弹窗开关、决定界面如何响应交互，是另一类事情。我希望把这两类职责拆开，让阅读和修改时需要关注的东西更集中。

于是我们又调整了一版：数据相关操作放在 ViewModel，当前页面内部的 UI 控制留在页面，再把较长的界面代码拆成小组件。这一版更符合我的理解，读起来也更清楚。

---

## AI 整理的学习笔记

以下结合代码梳理两次调整，并补充这次设计选择的适用范围。

### 第一次拆分：移出了存储代码，也移入了 UI 控制

AI 介绍了 MVVM，随后创建了一个 `NoteViewModel`，把一些状态和操作移了进去。

第一次拆分后，ViewModel 大致负责：

- 持有当前的笔记内容；
- 输入变化时更新内容并保存；
- 判断清空按钮是否显示；
- 控制确认弹窗的打开和关闭；
- 确认后清空内容、保存结果并关闭弹窗。

页面通过参数接收这些状态和回调：

```kotlin
NoteScreen(
    content = viewModel.content,
    onContentChange = viewModel::onContentChange,
    isClearVisible = viewModel.isClearButtonVisible,
    showClearDialog = viewModel.showClearDialog,
    onOpenClearDialog = viewModel::openClearDialog,
    onConfirmClear = viewModel::confirmClear,
    onDismissClearDialog = viewModel::dismissClearDialog
)
```

这次调整把存储操作从界面布局中移走了，但 ViewModel 同时承担了数据操作和弹窗交互控制。

这里值得继续讨论的，是这两类职责是否有必要放在一起。参数变多只是一个观察线索，不能单凭参数数量判断设计好坏。

### 进一步区分：数据的控制与 UI 的控制

在这个页面里，两类操作可以这样区分：

| 职责 | 当前代码中的例子 |
| --- | --- |
| 数据的控制 | 修改内容、保存内容、清空内容 |
| UI 的控制 | 打开确认弹窗、取消后关闭弹窗、确认后关闭弹窗 |

它们会相互配合，但可以分别表达。

例如，现在的清空流程是“点击按钮，再弹窗确认”。如果以后更换确认方式，清空数据和保存结果的操作仍然可以保持不变。

因此，可以让页面负责交互过程，在用户确认后调用一个独立的 `clearNote()`。这个方法只需要处理数据，不需要了解确认弹窗。

按钮显示条件也类似：当前只判断 `content.isNotEmpty()`，规则简单，而且只在页面中使用，直接保留在页面里就足够清楚。

### 第二次拆分：让相关职责各自集中

`NoteViewModel` 保留内容状态，以及修改、清空和保存操作：

```kotlin
class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences(
        "yet_prefs",
        Context.MODE_PRIVATE
    )

    var content by mutableStateOf(
        prefs.getString("note_content", "") ?: ""
    )
        private set

    fun onContentChange(newText: String) {
        content = newText
        prefs.edit()
            .putString("note_content", newText)
            .apply()
    }

    fun clearNote() {
        content = ""
        prefs.edit()
            .putString("note_content", "")
            .apply()
    }
}
```

`NoteScreen` 自己管理弹窗开关，用户确认后再调用清空操作：

```kotlin
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
            onDismiss = {
                showClearDialog = false
            }
        )
    }
}
```

这里的 `NoteInputField`、`ClearFab` 和 `ClearConfirmDialog` 是提取出来的界面组件，具体样式实现省略。

这样，主页面可以直接呈现整体结构和交互流程；需要修改某个组件的细节时，再进入对应函数。

调用页面时，只需要提供内容和两个数据操作回调：

```kotlin
NoteScreen(
    content = viewModel.content,
    onContentChange = viewModel::onContentChange,
    onClearNote = viewModel::clearNote
)
```

这次简化的价值，在于调用方不再需要了解页面内部如何管理弹窗。

阅读 ViewModel 时，可以集中看数据如何变化和保存；阅读页面时，可以集中看界面如何组织、交互如何进行。

### 这次思考可以推广到哪里

**拆文件不等于分清职责。**

代码从一个文件移到两个文件，只是组织形式发生了变化。是否改善可读性，还要看每部分负责的事情是否更集中、修改时是否更容易定位。

**区分数据操作和 UI 交互，不等于规定 ViewModel 只能处理数据。**

ViewModel 可以管理页面状态。第一次把弹窗状态放进去，并不意味着违反了 MVVM；它只是对当前这个简单页面没有带来明显收益。第一次实现也不能代表 MVVM 必须这样组织。

**是否需要存盘，不能单独决定状态的归属。**

弹窗开关也是状态，界面状态也可能需要恢复。更有用的问题是：谁使用它、谁修改它、它参与什么逻辑，以及需要保留多久。

当前弹窗只由这个页面控制，因此放在页面内部很自然。如果以后需要与其他页面或业务流程协调，可以再调整归属。

**小项目可以在解决实际问题后停止拆分。**

当前 ViewModel 仍然直接操作存储，没有继续拆出更多层。这是一种针对项目规模的取舍。

这次调整已经让数据操作和页面交互更容易分别理解。后续是否继续拆分，可以等新的阅读、修改或验证困难出现后再决定。
