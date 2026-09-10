# 《Yet》极简记事本 —— 现代 Android + Jetpack Compose 结对学习手册

> **定位**：专为 Java 开发者打造的现代 Android 实战笔记。  
> **项目名称**：Yet（极简灵感记事本）  
> **包名**：`com.cn7shi.yet`  
> **技术栈**：Kotlin + Jetpack Compose + Material 3  

---

## 目录
1. [项目结构拆解：Android 工程 vs 典型 Java/Maven 工程](#1-项目结构拆解android-工程-vs-典型-javamaven-工程)
2. [安卓四大组件扫盲：它们是做什么的？](#2-安卓四大组件扫盲它们是做什么的)
3. [为什么现代 Android 推崇“单 Activity + Compose”？](#3-为什么现代-android-推崇单-activity--compose)
4. [Activity 生命周期通俗科普：草稿保存的关键时机](#4-activity-生命周期通俗科普草稿保存的关键时机)
5. [Kotlin vs Java 语法对照：以 MainActivity.kt 为例](#5-kotlin-vs-java-语法对照以-mainactivitykt-为例)
6. [敏捷实战推进路线图（5 步走）](#6-敏捷实战推进路线图5-步走)

---

## 1. 项目结构拆解：Android 工程 vs 典型 Java/Maven 工程

如果你熟悉 Java 后端（如 Spring Boot + Maven），可以这样对应理解：

| Android 目录/文件 | Java/Maven 对应物 | 作用解释 |
| :--- | :--- | :--- |
| `settings.gradle.kts` | 多模块根 `pom.xml` 的 `<modules>` | 声明整个工程包含哪些子模块（如 `:app`）及依赖仓库（Google, MavenCentral）。 |
| `build.gradle.kts` (根目录) | 父级 `pom.xml` 的 `<dependencyManagement>` | 定义全局通用的构建插件版本。 |
| `app/build.gradle.kts` | 子模块的 `pom.xml` | **最常修改的文件**。配置打包 SDK 版本、应用包名、声明本项目需要的具体第三方依赖库。 |
| `app/src/main/AndroidManifest.xml` | `web.xml` 或 `application.yml` | **应用的总管清单**。向操作系统登记四大组件、申请系统权限（联网、存储等）。 |
| `app/src/main/java/` | `src/main/java/` | 放置所有业务源代码（虽然叫 `java/`，现在里面放 `.kt` 文件）。 |
| `app/src/main/res/` | `src/main/resources/` | 放置静态资源（App 图标、文字字符串 `strings.xml` 等）。 |

---

## 2. 安卓四大组件扫盲：它们是做什么的？

Android 系统为了统一管理硬件资源（内存、电量），将所有应用的功能划分为四种标准化容器，统称**四大组件**：

1. **Activity（活动窗口）**：
   - **形象比喻**：电脑上的一个“应用程序窗口”或前端的一个“全屏页面”。
   - **核心职责**：用户看得到、点得到、能产生交互的界面载体。每个有界面的 App 至少有一个入口 Activity。
2. **Service（后台服务）**：
   - **形象比喻**：Java 里的后台守护线程或后台任务进程（没有 UI 界面）。
   - **核心职责**：后台长时间播放音乐、下载大文件、持续同步数据。
3. **BroadcastReceiver（广播接收器）**：
   - **形象比喻**：发布-订阅模式中的“事件监听器”（Spring 的 `@EventListener`）。
   - **核心职责**：接收来自操作系统或其它 App 的系统级广播通知（如：手机开机、电量不足、网络断开等）。
4. **ContentProvider（内容提供者）**：
   - **形象比喻**：对外暴露的标准化 REST API / 数据访问接口（DAO）。
   - **核心职责**：在不同 App 之间安全共享数据（例如：微信要读取你手机通讯录里的联系人）。

---

## 3. 为什么现代 Android 推崇“单 Activity + Compose”？

在十年前，Android 的传统做法是“每个页面对应一个 Activity”（比如从列表页跳转到详情页，启动一个新的 Activity）：

### 传统多 Activity 的痛点：
* **过于重量级**：每个 Activity 创建销毁都要经过 Android 系统的 WindowManager 进行昂贵的跨进程通信。
* **数据传递极其繁琐**：Activity 之间传递对象必须实现 `Serializable` 或 `Parcelable`，无法直接传内存引用。
* **页面动画与转场生硬**：原生 Activity 切换动画很难做到丝滑自由定制。

### 现代“单 Activity + Compose”方案：
* **全剧只有一个 `MainActivity`**：把它当作一个“干净的浏览器窗口”或“单页应用容器”（SPA 思想，类似 Vue/React 单页应用）。
* **UI 全部用纯函数编写**：页面跳转只是在这个窗口里用 Compose 动态切换不同的函数渲染，性能极高、状态共享非常自然。

---

## 4. Activity 生命周期通俗科普：草稿保存的关键时机

由于手机内存有限，操作系统随时可能根据电量、来电或前后台切换把你的 Activity 冻结甚至杀掉。理解生命周期，是保证“记事本打字不丢”的关键底线。

### 核心阶段：
```
[onCreate]   -> 窗口刚创建（只做一次：加载页面框架、初始化依赖）
    ↓
[onStart]    -> 窗口变可见了，但还没完全聚焦
    ↓
[onResume]   -> 【活跃状态】用户开始在屏幕上疯狂打字输入
    ↓
[onPause]    -> ⚠️【关键临界点】用户按了 Home 键、来电话了、分屏了，失去焦点
    ↓
[onStop]     -> 完全退到后台不可见
    ↓
[onDestroy]  -> 窗口被彻底销毁释放
```

### 💡 为什么保存草稿要在 `onPause` 做？
- 从 `onPause` 开始，App 就脱离了前台。
- 现代 Android 系统在极端缺内存时，**可以在 `onStop` 阶段直接在后台杀死进程，而不会调用 `onDestroy`**！
- 因此：**草稿自动保存、或者轻量持久化，最安全的时机永远是在失去焦点的瞬间完成。**

---

## 5. Kotlin vs Java 语法对照：以 MainActivity.kt 为例

来看 Android Studio 为我们生成的源码拆解：

```kotlin
package com.cn7shi.yet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cn7shi.yet.ui.theme.YetTheme

// 1. 继承语法：Kotlin 用冒号 : 代替 Java 的 extends 和 implements
class MainActivity : ComponentActivity() {

    // 2. 方法重写：Kotlin 把 override 变成了硬关键字，放在 fun 前面
    // 3. 空安全：Bundle? 代表这个参数可能为 null（Java 的 @Nullable）
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 开启沉浸式全屏布局（沉入状态栏和导航栏底部）
        
        // 4. 高阶函数与尾随闭包：setContent { ... } 
        // 相当于 Java 里把一个 Lambda 表达式当作最后一个参数传入方法
        setContent {
            YetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// 5. 顶层函数（Top-level function）：Kotlin 函数不需要写在 class 内部！
// 6. @Composable 注解：告诉编译器“这个函数是用来描述 UI 的”
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    // 7. 字符串模板：$name 相当于 Java 中的 "Hello " + name + "!"
    // 8. 默认参数：modifier: Modifier = Modifier，Java 必须写方法重载才能实现
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

// 9. 预览功能：仅供 Android Studio 侧边栏实时预览渲染使用，不影响最终打包运行
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YetTheme {
        Greeting("Android")
    }
}
```

---

## 6. 敏捷实战推进路线图（5 步走）

- [x] **Step 1: 创建工程与基础知识筑基**
  - [x] 选择 Empty Activity (Jetpack Compose) 模板
  - [x] 建立本地 Git 仓库与分支管理规范（掌握 origin, main, -u 的面向对象本质）
  - [x] 建立《Yet》结对学习知识手册 (`LEARNING_NOTES.md`)
  - [x] 在模拟器/真机上点亮屏幕（运行 Hello World）
- [x] **Step 2: 极简纯文本输入页面**
  - [x] 深刻理解 Modifier 的本质：强类型“施工规格说明书（Spec Object）”
  - [x] 掌握 UI 隔离（UI Isolation）哲学：为什么 Preview 相当于 UI 的单元测试
  - [x] 实现纯文本全屏输入框 `NoteScreen` 与响应式状态 `mutableStateOf`
- [x] **Step 3: 本地持久化存储**
  - [x] 深刻理解 RAM（运行桌面） vs ROM/Flash（档案柜）的存储边界
  - [x] 掌握 `Context` 句柄与 `MODE_PRIVATE` 沙盒安全隔离
  - [x] 实现 `SharedPreferences` 异步双层刷盘（`apply()` vs `commit()`）
  - [x] 梳理 `SharedPreferences` 与 `Room` 关系型数据库的选型边界
- [ ] **Step 4: 一键清空/重置交互**（打通事件监听与确认机制）
- [ ] **Step 5: 极简 UI 设计重构与 Google Play 签名打包**（生成 .aab 发布包）

---

## 7. 进阶核心：Modifier 施工规格书与 Compose 状态

### 为什么说 Modifier 是一份【施工规格书】？
在 Java 业务开发中，我们常用建造者模式配置一个规范：
```java
WidgetSpec spec = new WidgetSpec().fillMaxSize().padding(16);
```
Compose 的 `Modifier` 也是一样：
- `Text`、`TextField` 是真正的实体积木。
- `Modifier` 是贴在积木上的强类型施工要求清单。
- 父容器（`Scaffold`）计算出刘海屏/状态栏的安全间距，写入规格书传给子组件 `NoteScreen(modifier)`；子组件在规格书上追加自己的全屏要求 `.fillMaxSize()`，然后贴给 `TextField` 施工。这样彻底实现了组件的解耦与多终端复用。

---

## 8. 本地持久化核心：SharedPreferences vs Room

### 1. RAM（内存）与 ROM/Flash（闪存）的物理界限
* **RAM（8GB/12GB）—— 办公桌面**：只有当前前台运行的 App 占用，进程被杀（Kill）后系统立刻清扫收回，断电即失。
* **ROM/Flash（256GB/512GB）—— 档案柜**：关机断电不丢。系统分为两块：
  * `shared_prefs/` 与 `files/`：核心数据区，系统绝不敢随意删除，除非用户清除数据或卸载。
  * `cache/`：临时缓存区，手机闪存吃紧时系统会强制无情抹除。

### 2. SharedPreferences 物理实现与线程机制
* **物理位置**：`/data/data/com.cn7shi.yet/shared_prefs/yet_prefs.xml`
* **`MODE_PRIVATE`**：基于 Linux UID 的应用沙盒保护，其他 App 无法跨界读取。
* **`apply()` vs `commit()`**：
  * `commit()`：同步刷盘，会阻塞 UI 渲染主线程，引起掉帧。
  * `apply()`：瞬间更新内存快照，并把任务排队到后台异步写入磁盘，UI 流畅丝滑。
* **生命周期安全保障**：在 Activity `onStop()` 进程彻底回收前，主线程会确保后台未完成的 `apply()` 刷盘排队全部执行完毕，草稿绝对不丢。

### 3. SharedPreferences vs Room 选型
* **SharedPreferences**：适合单篇草稿、用户设置开关、登录 Token，0 配置、毫秒级响应。
* **Room（SQLite ORM）**：相当于 Spring Data JPA + MySQL，适合多条笔记管理、标题/时间/标签多字段过滤、全文检索与分页。
