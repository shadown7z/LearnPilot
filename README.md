# LearnPilot

**智能学习策略规划器 · 原生 Android 应用**

把学习目标转成今天能执行的任务，再用实际学习记录和回忆结果安排复习。适合备考、面试复习和日常自学。

当前为本地运行的 MVP，仅提供 Android 客户端，无需注册、远程后端或 API Key。方法推荐和计划生成使用本地规则，不调用大模型。

<p align="center">
  <img src="icon-preview.png" width="112" height="112" alt="LearnPilot 应用图标：展开的书页与上升的学习路线">
</p>

## 功能

| 页面 | 已实现能力 |
| --- | --- |
| 总览 | 查看当前目标、剩余天数和学习概况，进入主要流程 |
| 诊断 | 分三步填写目标、截止日期、每日可用时间、科目、基础水平和薄弱点 |
| 计划 | 根据科目优先级和时间预算生成今日任务，查看任务步骤与建议方法 |
| 今日 | 开始、暂停和继续计时，完成任务后记录实际分钟数及掌握程度 |
| 复习 | 查看到期及逾期条目，根据「不会 / 部分 / 独立答对」更新下次日期 |
| 方法 | 浏览 15 种学习方法及匹配理由；点击查看详情，长按复制完整方法名称 |
| 方法详情 | 查看原理、操作步骤、示例、适用场景、误区、实践建议和边界 |
| 分析 | 汇总已完成任务、实际时长及各科目记录，对比计划与实际用时 |
| 设置 | 切换深色模式，查看本地数据数量，二次确认后清空数据 |

底部导航包含「总览、诊断、今日、复习、更多」。从「更多」进入子页面后可逐级返回，顶部返回按钮与系统返回共用页面栈；方法列表的滚动状态在进入详情再返回时保留。

## 开始使用

1. 打开「诊断」，填写目标、晚于今天的截止日期和每日学习时长（20–720 分钟）。
2. 添加科目，选择基础水平和主要薄弱点，保存诊断。
3. 进入「更多 → 计划」生成今日任务，在「今日」开始学习计时。
4. 完成任务，记录掌握程度；应用自动创建复习条目。
5. 到期后进入「复习」，先独立回忆，再如实标记结果。
6. 在「更多 → 方法」查看方法详情，在「更多 → 分析」回顾真实记录。

应用首次启动不预填个人成绩或完成记录，没有目标、任务或复习数据时会显示相应的空状态。

## 技术栈与环境要求

| 项目 | 当前配置 |
| --- | --- |
| 语言 | Kotlin 2.3.21 |
| UI | Jetpack Compose、Material 3，Compose BOM 2025.12.00 |
| 构建 | Android Gradle Plugin 9.4.1、Gradle Wrapper 9.6.0 |
| Gradle Daemon JVM | JDK 25，由 `gradle/gradle-daemon-jvm.properties` 声明 |
| Java 编译目标 | Java 17，与运行 Gradle 的 JDK 版本分别配置 |
| Android SDK | compileSdk 37、targetSdk 37 |
| 最低系统 | Android 8.0（API 26） |
| 应用 ID | `com.atlaes.learnpilot` |
| 持久化 | SharedPreferences + JSON |
| 单元测试 | JUnit 4.13.2 |

使用支持当前 AGP 的 Android Studio，安装 Android SDK Platform 37 及 Platform-Tools。无需单独安装全局 Gradle，优先使用仓库自带的 Wrapper。

## 获取与运行

以下命令在 Windows PowerShell 中执行。

```powershell
git clone https://github.com/shadown7z/LearnPilot.git
Set-Location LearnPilot
```

### Android Studio

1. 打开克隆后的项目根目录。
2. 在 SDK Manager 中检查 SDK Platform 37 和 Platform-Tools，并接受所需许可证。
3. 配置 Gradle 使用 JDK 25；项目也声明了 JVM toolchain 自动获取地址，首次获取需要网络。
4. 等待 Gradle 同步，选择 `app` 和 Android 8.0 及以上设备，点击 Run。

Android Studio 会生成本机 `local.properties`。该文件包含 SDK 路径，不提交到仓库；命令行构建时也可以通过有效的 `ANDROID_HOME` 指定 SDK。

### 命令行构建

先将 `JAVA_HOME` 指向本机可用的 JDK 25，并配置 Android SDK。建议将 Gradle 用户目录放在项目内，便于统一管理下载和缓存：

```powershell
$env:GRADLE_USER_HOME = Join-Path $PWD ".gradle-user-home"
.\gradlew.bat --version
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

首次构建需要下载 Gradle、插件及 Maven 依赖，需要访问 Google Maven、Maven Central、Gradle Plugin Portal 等仓库。无需将 `local.properties`、缓存或生成的 APK 提交到 Git。

Debug APK 构建输出：

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 安装与调试

开启手机的开发者选项与 USB 调试，连接后允许电脑调试；也可以使用 Android Emulator。以下命令要求 SDK 的 `platform-tools` 已加入 `PATH`，否则请使用 `adb.exe` 的实际路径：

```powershell
adb devices
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.atlaes.learnpilot/.MainActivity
adb logcat
```

正式发布前需要自行配置 Release 签名，并检查备份策略、版本号和发布要求。当前仓库未配置发布密钥。

## 核心逻辑

- **方法推荐**：基础分 25，匹配任务类型加 40，匹配薄弱点加 25；基础较弱时，部分方法额外加 10，最终不超过 100。每条推荐附匹配理由。
- **任务生成**：按 `重要程度 × (6 − 基础水平)` 排序，使用每日可用时间的 85% 作为本次生成预算；每科最多生成一项任务，每项 20–45 分钟，剩余不足 20 分钟时不再分配。
- **复习调度**：「不会」安排 1 天后，「部分」安排 2 天后；「独立答对」按当前复习次数选择 1、3、7、14、30、45 天的间隔。

这些规则属于启发式实现，匹配分数不代表学习成功概率。当前计划生成器主要通过科目名称识别数学、英语和 408，其他科目使用默认记忆类型；方法页基于当前目标的第一门科目计算排序。

## 数据与隐私

目标、当前目标 ID、任务、复习记录和主题偏好保存在应用私有 SharedPreferences `learnpilot_v1` 中，以 JSON 序列化。应用不提供账号系统、云同步、广告或统计服务，Manifest 未声明网络权限。

当前 Manifest 启用了 Android 系统备份，数据可能依照系统及设备备份设置参与备份或设备迁移。清空数据会删除应用内保存的学习记录，当前尚无手动导出恢复功能；异常 JSON 读取失败时会回退为空状态。

## 项目结构

```text
LearnPilot/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/atlaes/learnpilot/
│       │   │   ├── MainActivity.kt       # Compose 页面、页面栈及交互
│       │   │   ├── core/Planner.kt      # 数据模型、方法内容及策略计算
│       │   │   └── data/LocalStore.kt   # JSON 序列化与本地存储
│       │   └── res/                     # 主题、图标及 Android 资源
│       ├── test/                       # 本地 JUnit 测试
│       └── androidTest/                # Android 仪器测试入口
├── gradle/                             # 版本目录、Wrapper 与 JVM 配置
├── tools/generate_icon.py               # 原创图标资源生成脚本
├── icon-preview.png
├── .agents/skills/edge-to-edge/          # 项目级开发辅助 Skill
├── skills-lock.json
└── README.md
```

UI 与交互集中在 `MainActivity.kt`；策略计算和方法内容独立于 UI，便于测试及后续拆分。项目级 Skill 来源为 [android/skills](https://github.com/android/skills)，仅辅助开发，不参与应用运行。

## 图标资源

主图标以展开的书页和上升路线表达学习与进步，使用深蓝、白色和薄荷绿。已提供自适应图标前景、背景及 mdpi 至 xxxhdpi 的 WebP 资源，Manifest 同时配置普通和圆形图标。

- 前景：`app/src/main/res/drawable-nodpi/ic_learnpilot_foreground.png`
- 背景：`app/src/main/res/values/icon_background.xml`
- 自适应配置：`app/src/main/res/mipmap-anydpi/ic_launcher.xml`、`ic_launcher_round.xml`
- 各密度资源：`app/src/main/res/mipmap-*/`
- 仓库预览：`icon-preview.png`

若需重新生成，使用 Python 3 和 Pillow。以下虚拟环境位于项目内，不影响 Android 构建：

```powershell
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install Pillow
.\.venv\Scripts\python.exe tools\generate_icon.py
```

## 测试与验收

```powershell
# 策略与方法数据单元测试
.\gradlew.bat :app:testDebugUnitTest

# 编译并生成 Debug APK
.\gradlew.bat :app:assembleDebug

# 有可用设备时运行现有仪器测试
.\gradlew.bat :app:connectedDebugAndroidTest
```

`PlannerTest` 包含 5 项业务测试，覆盖方法推荐差异、任务时长预算、复习间隔、无效输入和所有方法的详情数据。测试报告生成在 `app/build/reports/tests/testDebugUnitTest/index.html`。

仓库当前的仪器测试仍为基础模板，尚未覆盖完整页面交互。验收时还需在设备上检查：

- 「更多 → 方法 → 详情」的顶部返回、系统返回和列表位置恢复。
- 方法卡片点击进入对应详情，长按只复制方法名称并显示提示。
- 诊断保存、计划生成、计时记录、到期复习及重启后的数据读取。
- 深色模式、长文本滚动、大字体及不同系统导航方式下的显示。

## 当前边界

- 仅实现 Android 本地 MVP，尚未提供完整月度路线、题库、导入导出、拖动排序和后台提醒。
- 计时状态保存在当前页面的内存中，切换页面或重建 Activity 后不能保证继续计时；不支持可靠的后台计时恢复。
- 页面返回栈未持久化；进程重建后会回到总览。方法列表位置保留面向当前 Activity 内的详情往返。
- 每日时间仅为本次生成预算，重新计算任务时未扣除当天已完成任务的时间。
- 本地存储适用于小规模个人数据；尚无数据库迁移、数据损坏恢复和跨设备同步机制。

后续优先完善计时与页面状态恢复、计划重算的时间预算和导航交互测试，再扩展其他功能。
