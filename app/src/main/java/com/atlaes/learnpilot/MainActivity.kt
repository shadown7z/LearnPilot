package com.atlaes.learnpilot

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atlaes.learnpilot.core.*
import com.atlaes.learnpilot.data.LocalStore
import com.atlaes.learnpilot.data.Snapshot
import kotlinx.coroutines.delay
import java.time.LocalDate

private val ink = Color(0xFF18263D)
private val blue = Color(0xFF2455A4)
private val mint = Color(0xFF1D907D)
private val pale = Color(0xFFF5F7FA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = LocalStore(this)
        setContent {
            var data by remember { mutableStateOf(store.load()) }
            var pageStack by remember { mutableStateOf(listOf("总览")) }
            var notice by remember { mutableStateOf("") }
            val page = pageStack.last()
            val rootPage = pageStack.first()
            val methodsListState = rememberLazyListState()
            val goal = data.goals.find { it.id == data.activeId }
            fun update(next: Snapshot) { data = next; store.save(next) }
            fun navigateRoot(next: String) { pageStack = listOf(next) }
            fun pushPage(next: String) { pageStack = pageStack + next }
            fun popPage() { if (pageStack.size > 1) pageStack = pageStack.dropLast(1) }
            val colors = if (data.dark) darkColorScheme(primary = Color(0xFF9DBEFF), secondary = Color(0xFF78D4BF)) else lightColorScheme(primary = blue, secondary = mint, background = pale, surface = Color.White, onSurface = ink)
            MaterialTheme(colorScheme = colors, typography = Typography()) {
                BackHandler(enabled = pageStack.size > 1) { popPage() }
                Scaffold(
                    topBar = { Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconMark()
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) { Text("LearnPilot", fontWeight = FontWeight.Bold, fontSize = 21.sp); Text("让每一分钟都产生进步", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            Text(LocalDate.now().toString(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } },
                    bottomBar = { NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        listOf("总览" to Icons.Filled.Home, "诊断" to Icons.Filled.Psychology, "今日" to Icons.Filled.Checklist, "复习" to Icons.Filled.Refresh, "更多" to Icons.Filled.Menu).forEach { (name, symbol) ->
                            NavigationBarItem(selected = rootPage == name, onClick = { navigateRoot(name) }, icon = { Icon(symbol, contentDescription = name) }, label = { Text(name) })
                        }
                    } },
                    snackbarHost = { if (notice.isNotEmpty()) LaunchedEffect(notice) { delay(2800); notice = "" }; if (notice.isNotEmpty()) Snackbar(Modifier.padding(12.dp)) { Text(notice) } }
                ) { padding ->
                    AnimatedContent(page, label = "page", modifier = Modifier.fillMaxSize().padding(padding)) { current ->
                        when (current) {
                            "总览" -> Dashboard(data, goal, { navigateRoot(it) })
                            "诊断" -> Diagnosis(goal, { g ->
                                val revised = data.goals.filterNot { it.id == g.id } + g
                                update(data.copy(goals = revised, activeId = g.id, tasks = data.tasks.filterNot { it.subjectId in (goal?.subjects?.map { s -> s.id } ?: emptyList()) }))
                                notice = "诊断已保存，可以生成计划"; navigateRoot("计划")
                            }, { notice = it })
                            "计划" -> PlanPage(data, goal, pageStack.size > 1, ::popPage, { next -> update(next) }, { notice = it })
                            "今日" -> TodayPage(data, goal, { next -> update(next) }, { notice = it })
                            "复习" -> ReviewPage(data, { next -> update(next) }, { notice = it })
                            "方法" -> MethodsPage(goal, methodsListState, pageStack.size > 1, ::popPage, { methodId -> pushPage("方法详情:$methodId") }, { notice = it })
                            "分析" -> AnalyticsPage(data, goal, pageStack.size > 1, ::popPage)
                            "设置" -> SettingsPage(data, { next -> update(next) }, { store.clear(); data = Snapshot(); navigateRoot("总览") }, { notice = it }, pageStack.size > 1, ::popPage)
                            else -> if (page.startsWith("方法详情:")) {
                                MethodDetailPage(page.removePrefix("方法详情:"), ::popPage)
                            } else MorePage(::pushPage, pageStack.size > 1, ::popPage)
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun IconMark() { Box(Modifier.size(34.dp).background(blue, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.AutoStories, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp)) } }
@Composable private fun PageTitle(title: String, subtitle: String, showBack: Boolean = false, onBack: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(bottom = 18.dp), verticalAlignment = Alignment.Top) {
        if (showBack) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp).padding(end = 4.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回上一级")
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 27.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@Composable private fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) { Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), tonalElevation = 1.dp, shadowElevation = 1.dp, content = { Column(Modifier.padding(18.dp), content = content) }) }
@Composable private fun Pill(text: String, color: Color = blue) { Surface(color = color.copy(alpha = .1f), shape = RoundedCornerShape(50)) { Text(text, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium) } }
@Composable private fun Empty(title: String, detail: String, action: String? = null, onClick: () -> Unit = {}) { Panel { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp); Spacer(Modifier.height(6.dp)); Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp); if (action != null) { Spacer(Modifier.height(14.dp)); Button(onClick = onClick) { Text(action) } } } }

@Composable private fun Dashboard(data: Snapshot, goal: Goal?, navigate: (String) -> Unit) {
    val today = LocalDate.now().toString()
    val todayTasks = data.tasks.filter { it.due == today }
    val done = todayTasks.count { it.done }
    val due = data.reviews.count { it.nextDate <= today }
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { PageTitle("今天先做什么", "先诊断，再执行；用真实记录调整策略") }
        if (goal == null) item { Empty("建立第一个学习目标", "填写科目、时间与截止日期，系统会据此生成可执行任务。", "开始诊断") { navigate("诊断") } }
        else {
            item { Panel { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Pill(goal.kind); Spacer(Modifier.height(10.dp)); Text(goal.name, fontSize = 21.sp, fontWeight = FontWeight.Bold); Text("距目标还有 ${Planner.daysLeft(goal)} 天", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) }; Text("${goal.minutesPerDay} 分/天", fontWeight = FontWeight.Bold, color = blue) } } }
            item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Metric("今日任务", "$done/${todayTasks.size}", Modifier.weight(1f)); Metric("待复习", "$due", Modifier.weight(1f)); Metric("实际学习", "${data.tasks.filter { it.done && it.due == today }.sumOf { it.actualMinutes }} 分", Modifier.weight(1f)) } }
            item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Button(onClick = { navigate("今日") }, modifier = Modifier.weight(1f)) { Text("开始学习") }; OutlinedButton(onClick = { navigate("计划") }, modifier = Modifier.weight(1f)) { Text("查看计划") } } }
            item { Text("今日优先事项", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            if (todayTasks.isEmpty()) item { Empty("还没有今日计划", "根据当前诊断生成任务，示例科目不会被当作已完成记录。", "生成计划") { navigate("计划") } }
            else items(todayTasks.take(3)) { task -> Panel { Pill(if (task.done) "已完成" else task.method, if (task.done) mint else blue); Spacer(Modifier.height(8.dp)); Text(task.title, maxLines = 3, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium); Text("预计 ${task.minutes} 分钟", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp) } }
            item { Text("科目重点", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            items(goal.subjects) { s -> Panel { Row { Text(s.name, Modifier.weight(1f), fontWeight = FontWeight.Medium); Text("基础 ${s.level}/5", color = MaterialTheme.colorScheme.onSurfaceVariant) }; Spacer(Modifier.height(8.dp)); Text(if (s.weakness.isBlank()) "尚未填写薄弱点" else "当前薄弱点：${s.weakness}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
    }
}
@Composable private fun Metric(label: String, value: String, modifier: Modifier = Modifier) { Panel(modifier) { Text(value, color = blue, fontSize = 17.sp, fontWeight = FontWeight.Bold); Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable private fun Diagnosis(existing: Goal?, save: (Goal) -> Unit, notify: (String) -> Unit) {
    var step by remember(existing?.id) { mutableIntStateOf(0) }
    var name by remember(existing?.id) { mutableStateOf(existing?.name ?: "") }
    var deadline by remember(existing?.id) { mutableStateOf(existing?.deadline ?: LocalDate.now().plusMonths(6).toString()) }
    var minutes by remember(existing?.id) { mutableStateOf(existing?.minutesPerDay?.toString() ?: "120") }
    var kind by remember(existing?.id) { mutableStateOf(existing?.kind ?: "考研") }
    var subjectText by remember(existing?.id) { mutableStateOf(existing?.subjects?.joinToString("、") { it.name } ?: "") }
    var weakness by remember(existing?.id) { mutableStateOf(existing?.subjects?.firstOrNull()?.weakness ?: "") }
    var level by remember(existing?.id) { mutableIntStateOf(existing?.subjects?.firstOrNull()?.level ?: 2) }
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { PageTitle("学习诊断", "${step + 1}/3 · 目标 → 科目 → 薄弱点") }
        item { LinearProgressIndicator(progress = { (step + 1) / 3f }, modifier = Modifier.fillMaxWidth()) }
        when (step) {
            0 -> { item { OutlinedTextField(name, { name = it }, label = { Text("学习目标") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }; item { OutlinedTextField(kind, { kind = it }, label = { Text("目标类型") }, modifier = Modifier.fillMaxWidth()) }; item { OutlinedTextField(deadline, { deadline = it }, label = { Text("截止日期 YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth()) }; item { OutlinedTextField(minutes, { minutes = it }, label = { Text("每天可学习分钟") }, modifier = Modifier.fillMaxWidth()) } }
            1 -> { item { OutlinedTextField(subjectText, { subjectText = it }, label = { Text("科目，用顿号分隔") }, supportingText = { Text("例如：高等数学、408、英语") }, modifier = Modifier.fillMaxWidth()) }; item { Text("当前基础：$level / 5"); Slider(level.toFloat(), { level = it.toInt().coerceIn(1, 5) }, valueRange = 1f..5f, steps = 3) } }
            else -> { item { Text("当前主要薄弱点", fontWeight = FontWeight.SemiBold) }; items(listOf("概念不理解", "容易遗忘", "独立做题困难", "不会识别题型", "计算错误", "缺少练习", "难以坚持")) { item -> FilterChip(selected = weakness == item, onClick = { weakness = item }, label = { Text(item) }) }; item { Text("推荐匹配度是启发式指标，不代表学习成功概率。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { if (step > 0) OutlinedButton(onClick = { step-- }) { Text("上一步") }; Button(onClick = {
            if (step < 2) { if (step == 0 && (name.isBlank() || minutes.toIntOrNull() !in 20..720 || runCatching { LocalDate.parse(deadline).isAfter(LocalDate.now()) }.getOrDefault(false).not())) notify("请填写目标、有效日期及 20–720 分钟时长") else if (step == 1 && subjectText.isBlank()) notify("请填写至少一门科目") else step++ }
            else { val names = subjectText.split('、', ',', '，').map { it.trim() }.filter { it.isNotBlank() }.distinct(); if (names.isEmpty()) notify("请填写至少一门科目") else save(Goal(id = existing?.id ?: java.util.UUID.randomUUID().toString(), name = name.trim(), deadline = deadline, minutesPerDay = minutes.toInt(), kind = kind, subjects = names.map { n -> existing?.subjects?.find { it.name == n }?.copy(level = level, weakness = weakness) ?: Subject(name = n, level = level, weakness = weakness) })) }
        }) { Text(if (step == 2) "保存诊断" else "下一步") } } }
    }
}

@Composable private fun PlanPage(data: Snapshot, goal: Goal?, showBack: Boolean, onBack: () -> Unit, update: (Snapshot) -> Unit, notify: (String) -> Unit) {
    val today = LocalDate.now().toString()
    val tasks = data.tasks.filter { it.due == today }
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { PageTitle("学习计划", "根据目标差距与时间预算分配今天的任务", showBack, onBack) }
        if (goal == null) item { Empty("先完成诊断", "计划需要目标、科目和每日可用时间。") }
        else {
            item { Panel { Text("今日可用 ${goal.minutesPerDay} 分钟", fontWeight = FontWeight.Bold); Text("任务最多占 85%，其余留给休息和复盘", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(12.dp)); Button(onClick = { runCatching { Planner.generate(goal) }.onSuccess { generated -> update(data.copy(tasks = data.tasks.filterNot { it.due == today && !it.done } + generated)); notify("已生成 ${generated.size} 项今日任务") }.onFailure { notify(it.message ?: "生成失败") } }) { Text(if (tasks.isEmpty()) "生成今日计划" else "重新计算未完成任务") } } }
            if (tasks.isEmpty()) item { Empty("暂无计划", "点击生成后，任务会按重要程度和基础情况排序。") }
            items(tasks) { t -> Panel { Row { Pill(if (t.done) "已完成" else t.method, if (t.done) mint else blue); Spacer(Modifier.weight(1f)); Text("${t.minutes} 分", color = blue) }; Spacer(Modifier.height(10.dp)); Text(t.title, fontWeight = FontWeight.Medium); Spacer(Modifier.height(5.dp)); Text(t.steps, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
    }
}

@Composable private fun TodayPage(data: Snapshot, goal: Goal?, update: (Snapshot) -> Unit, notify: (String) -> Unit) {
    val tasks = data.tasks.filter { it.due == LocalDate.now().toString() }
    var activeId by remember { mutableStateOf<String?>(null) }
    var running by remember { mutableStateOf(false) }
    var seconds by remember { mutableIntStateOf(0) }
    var finishing by remember { mutableStateOf<StudyTask?>(null) }
    var mastery by remember { mutableIntStateOf(3) }
    LaunchedEffect(running, activeId) { while (running && activeId != null) { delay(1000); seconds++ } }
    finishing?.let { task -> AlertDialog(onDismissRequest = { finishing = null }, title = { Text("记录学习结果") }, text = { Column { Text(task.title, maxLines = 2); Spacer(Modifier.height(10.dp)); Text("实际学习 ${maxOf(1, seconds / 60)} 分钟"); Text("掌握程度 $mastery / 5"); Slider(mastery.toFloat(), { mastery = it.toInt().coerceIn(1, 5) }, valueRange = 1f..5f, steps = 3) } }, confirmButton = { TextButton(onClick = { val done = task.copy(done = true, actualMinutes = maxOf(1, seconds / 60), mastery = mastery); val review = Review(taskId = task.id, subjectId = task.subjectId, title = task.title, nextDate = Planner.nextReview(LocalDate.now(), if (mastery <= 2) 0 else 2, 0).toString()); update(data.copy(tasks = data.tasks.map { if (it.id == task.id) done else it }, reviews = data.reviews + review)); finishing = null; activeId = null; running = false; seconds = 0; notify("已记录完成，并安排下一次复习") }) { Text("确认完成") } }, dismissButton = { TextButton(onClick = { finishing = null }) { Text("继续学习") } }) }
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { PageTitle("今日学习", "把注意力放在下一项具体任务上") }
        if (tasks.isEmpty()) item { Empty("没有今日任务", if (goal == null) "先完成诊断。" else "前往计划页生成今日任务。") }
        else items(tasks) { task -> Panel { Row(verticalAlignment = Alignment.CenterVertically) { Pill(if (task.done) "已完成" else task.method, if (task.done) mint else blue); Spacer(Modifier.weight(1f)); Text("${task.minutes} 分", fontSize = 12.sp) }; Spacer(Modifier.height(9.dp)); Text(task.title, fontWeight = FontWeight.SemiBold); Text(task.steps, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); if (!task.done) { Spacer(Modifier.height(10.dp)); if (activeId == task.id) { Text("计时 ${"%02d".format(seconds / 60)}:${"%02d".format(seconds % 60)}", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = blue); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = { running = !running }) { Text(if (running) "暂停" else "继续") }; Button(onClick = { running = false; finishing = task }) { Text("完成并记录") } } } else OutlinedButton(onClick = { activeId = task.id; seconds = 0; running = true }) { Text("开始计时") } } } }
    }
}

@Composable private fun ReviewPage(data: Snapshot, update: (Snapshot) -> Unit, notify: (String) -> Unit) {
    val due = data.reviews.filter { it.nextDate <= LocalDate.now().toString() }
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { PageTitle("复习中心", "先独立回忆，再按真实表现安排下次复测") }
        if (due.isEmpty()) item { Empty("当前没有待复习内容", "完成今日任务后会自动创建复习条目。") }
        else items(due) { review -> Panel { Pill(if (review.nextDate < LocalDate.now().toString()) "已逾期" else "今日待复习"); Spacer(Modifier.height(8.dp)); Text(review.title, fontWeight = FontWeight.SemiBold); Text("已复习 ${review.repetitions} 次", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(10.dp)); Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { listOf("不会", "部分", "独立答对").forEachIndexed { i, label -> OutlinedButton(onClick = { val next = review.copy(nextDate = Planner.nextReview(LocalDate.now(), i, review.repetitions).toString(), repetitions = review.repetitions + 1, lastResult = i); update(data.copy(reviews = data.reviews.map { if (it.id == review.id) next else it })); notify("下次复习：${next.nextDate}") }, contentPadding = PaddingValues(horizontal = 9.dp)) { Text(label, fontSize = 11.sp) } } } } }
    }
}

@Composable private fun MethodsPage(goal: Goal?, listState: LazyListState, showBack: Boolean, onBack: () -> Unit, openDetail: (String) -> Unit, notify: (String) -> Unit) {
    val subject = goal?.subjects?.firstOrNull()
    val ranked = Planner.recommend(if (subject?.name?.contains("数学") == true) "计算" else "概念", subject?.weakness ?: "", subject?.level ?: 2)
    val context = LocalContext.current
    val clipboard = remember(context) { context.getSystemService(ClipboardManager::class.java) }
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), state = listState, contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { PageTitle("学习方法库", "点击查看详情，长按复制方法名称", showBack, onBack) }
        items(ranked, key = { it.method.id }) { recommendation ->
            Panel(Modifier.combinedClickable(
                onClick = { openDetail(recommendation.method.id) },
                onLongClick = {
                    clipboard?.setPrimaryClip(ClipData.newPlainText("学习方法", recommendation.method.name))
                    notify("已复制：${recommendation.method.name}")
                }
            )) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(recommendation.method.name, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Pill("${recommendation.score} 分")
                }
                Spacer(Modifier.height(7.dp))
                Text("${recommendation.method.tag} · ${recommendation.method.solves}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text(recommendation.method.steps, fontSize = 13.sp)
                Spacer(Modifier.height(5.dp))
                Text("建议 ${recommendation.method.minutes} 分钟 · ${recommendation.reasons.joinToString("、")}", fontSize = 12.sp, color = blue)
            }
        }
    }
}

@Composable private fun MethodDetailPage(methodId: String, onBack: () -> Unit) {
    val method = Planner.methods.firstOrNull { it.id == methodId }
    val detail = Planner.detailFor(methodId)
    if (method == null || detail == null) {
        LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(20.dp)) {
            item { PageTitle("方法详情", "当前内容不可用", true, onBack) }
            item { Empty("找不到这个方法", "它可能已被移除，请返回方法列表。", "返回列表", onBack) }
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { PageTitle(method.name, "${method.tag} · 推荐 ${method.minutes} 分钟", true, onBack) }
        item { DetailBlock("方法简介", detail.summary) }
        item { DetailBlock("核心原理", detail.principle) }
        item { Panel { Text("操作步骤", fontWeight = FontWeight.SemiBold, fontSize = 17.sp); Spacer(Modifier.height(8.dp)); detail.steps.forEachIndexed { index, step -> Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) { Pill("${index + 1}", mint); Spacer(Modifier.width(10.dp)); Text(step, Modifier.weight(1f), fontSize = 13.sp) } } } }
        item { DetailBlock("完整示例", detail.example) }
        item { DetailBlock("适用场景", detail.scenarios) }
        item { DetailBlock("常见误区", detail.mistakes) }
        item { DetailBlock("实践建议", detail.practice) }
        item { DetailBlock("适用边界", detail.limitations, accent = Color(0xFF9A5A16)) }
    }
}

@Composable private fun DetailBlock(title: String, body: String, accent: Color = blue) {
    Panel { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = accent); Spacer(Modifier.height(8.dp)); Text(body, fontSize = 14.sp, lineHeight = 22.sp) }
}

@Composable private fun AnalyticsPage(data: Snapshot, goal: Goal?, showBack: Boolean, onBack: () -> Unit) { val done = data.tasks.filter { it.done }; LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { item { PageTitle("学习分析", "所有数据来自真实完成记录", showBack, onBack) }; if (done.isEmpty()) item { Empty("还没有学习记录", "完成第一项任务后，这里会显示实际时长与掌握情况。") } else { item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Metric("完成任务", "${done.size}", Modifier.weight(1f)); Metric("实际时长", "${done.sumOf { it.actualMinutes }} 分", Modifier.weight(1f)) } }; item { Panel { Text("计划与实际", fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(8.dp)); Text("预计 ${done.sumOf { it.minutes }} 分 · 实际 ${done.sumOf { it.actualMinutes }} 分"); LinearProgressIndicator(progress = { (done.sumOf { it.actualMinutes }.toFloat() / done.sumOf { it.minutes }.coerceAtLeast(1)).coerceIn(0f, 1f) }, Modifier.fillMaxWidth().padding(top = 10.dp)) } }; goal?.subjects?.forEach { s -> item { Panel { Text(s.name, fontWeight = FontWeight.SemiBold); Text("完成 ${done.count { it.subjectId == s.id }} 项 · 实际 ${done.filter { it.subjectId == s.id }.sumOf { it.actualMinutes }} 分", fontSize = 12.sp) } } } } } }
@Composable private fun MorePage(navigate: (String) -> Unit, showBack: Boolean, onBack: () -> Unit) { LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { PageTitle("更多", "规划与记录工具", showBack, onBack) }; items(listOf("计划" to "生成并查看今日任务", "方法" to "了解 15 种学习方法", "分析" to "查看真实学习记录", "设置" to "主题与数据管理")) { (name, desc) -> Panel(Modifier.clickable { navigate(name) }) { Text(name, fontWeight = FontWeight.SemiBold); Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }
@Composable private fun SettingsPage(data: Snapshot, update: (Snapshot) -> Unit, clear: () -> Unit, notify: (String) -> Unit, showBack: Boolean, onBack: () -> Unit) { var confirm by remember { mutableStateOf(false) }; if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("清空所有本地数据？") }, text = { Text("目标、任务、复习与学习记录会永久删除。") }, confirmButton = { TextButton(onClick = { clear(); confirm = false }) { Text("确认清空") } }, dismissButton = { TextButton(onClick = { confirm = false }) { Text("取消") } }); LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { item { PageTitle("设置", "只在此设备保存你的学习资料", showBack, onBack) }; item { Panel { Row(verticalAlignment = Alignment.CenterVertically) { Text("深色模式", Modifier.weight(1f)); Switch(data.dark, { update(data.copy(dark = it)) }) } } }; item { Panel { Text("本地资料", fontWeight = FontWeight.SemiBold); Text("${data.goals.size} 个目标 · ${data.tasks.size} 项任务 · ${data.reviews.size} 条复习", fontSize = 12.sp); Spacer(Modifier.height(10.dp)); OutlinedButton(onClick = { notify("数据保存在应用本地空间") }) { Text("查看存储说明") } } }; item { OutlinedButton(onClick = { confirm = true }, modifier = Modifier.fillMaxWidth()) { Text("清空本地数据") } } } }
