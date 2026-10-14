package com.atlaes.learnpilot.core

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

data class Subject(val id: String = UUID.randomUUID().toString(), val name: String, val level: Int = 2, val importance: Int = 3, val weakness: String = "")
data class Goal(val id: String = UUID.randomUUID().toString(), val name: String, val deadline: String, val minutesPerDay: Int, val subjects: List<Subject>, val workDays: Int = 6, val kind: String = "自定义")
data class StudyTask(val id: String = UUID.randomUUID().toString(), val subjectId: String, val title: String, val steps: String, val method: String, val minutes: Int, val due: String, val done: Boolean = false, val actualMinutes: Int = 0, val mastery: Int = 0)
data class Review(val id: String = UUID.randomUUID().toString(), val taskId: String, val subjectId: String, val title: String, val nextDate: String, val repetitions: Int = 0, val lastResult: Int = 0)
data class Method(val name: String, val tag: String, val solves: String, val steps: String, val kinds: Set<String>, val weak: Set<String>, val minutes: Int, val id: String = name)
data class Recommendation(val method: Method, val score: Int, val reasons: List<String>)
data class MethodDetail(
    val methodId: String,
    val summary: String,
    val principle: String,
    val steps: List<String>,
    val example: String,
    val scenarios: String,
    val mistakes: String,
    val practice: String,
    val limitations: String
)

object Planner {
    val methods = listOf(
        Method("费曼学习法", "理解", "似懂非懂", "用自己的话解释 → 找出卡住处 → 补齐后重讲", setOf("概念", "应用"), setOf("概念不理解"), 20),
        Method("间隔重复", "长期保持", "容易遗忘", "遮住答案回忆 → 记录表现 → 安排复测", setOf("记忆", "公式"), setOf("容易遗忘"), 15),
        Method("主动回忆", "记忆", "看懂却说不出", "合上资料列问题 → 独立回答 → 核对遗漏", setOf("记忆", "公式", "概念"), setOf("容易遗忘", "不会独立回答"), 20),
        Method("刻意练习", "实战", "独立做题困难", "只练一个弱点 → 独立完成 → 核对反馈", setOf("计算", "编码", "应用"), setOf("独立做题困难"), 35),
        Method("交错练习", "辨析", "不会识别题型", "混合两类题 → 判断题型 → 说明依据", setOf("题型", "计算"), setOf("不会识别题型"), 30),
        Method("问题驱动学习", "理解", "学习缺少目标", "提出具体问题 → 找所需知识 → 验证答案", setOf("应用", "编码"), setOf("缺少练习"), 30),
        Method("生成效应", "理解", "被动看答案", "先预测 → 解释推理 → 对照修正", setOf("概念", "计算"), setOf("不会独立回答"), 15),
        Method("对比学习", "辨析", "易混知识", "并列写相同点 → 找关键差异 → 做辨析题", setOf("题型", "概念"), setOf("不会识别题型"), 20),
        Method("双向提问", "理解", "知识关联薄弱", "正向提问 → 反问边界 → 独立回答", setOf("概念", "记忆"), setOf("不会独立回答"), 20),
        Method("项目式学习", "实战", "缺少实际练习", "定义小成果 → 实现 → 运行验证", setOf("编码", "应用"), setOf("缺少练习"), 50),
        Method("最小行动法", "启动习惯", "难以开始", "选 5 分钟动作 → 立即开始 → 完成后决定是否继续", setOf("习惯"), setOf("难以坚持"), 5),
        Method("间歇休息与睡眠巩固", "恢复", "疲劳错误", "专注一段时间 → 短休 → 睡前简短回顾", setOf("习惯"), setOf("学习速度慢"), 10),
        Method("错题归因", "纠错", "重复犯错", "定位错误步骤 → 归类 → 隔天换题重做", setOf("计算", "题型"), setOf("计算错误", "不会识别题型"), 20),
        Method("限时训练", "考试", "时间分配失衡", "设定时限 → 独立完成 → 分析耗时", setOf("考试", "计算"), setOf("时间分配不合理"), 35),
        Method("精读与结构分析", "理解", "阅读只看表面", "划分结构 → 标注逻辑 → 复述主旨", setOf("阅读"), setOf("概念不理解"), 30)
    )
    val methodDetails = methods.map { method ->
        when (method.name) {
            "费曼学习法" -> MethodDetail(method.id, "用自己的话把知识讲给初学者听，定位真正不懂的地方。", "解释会迫使你提取概念之间的因果关系，而不是停留在熟悉感。", listOf("写下要解释的知识点", "不看资料，用简单语言讲一遍", "标记卡住或只能照抄术语的地方", "回到资料补齐，再重新讲一遍"), "学习 Java 集合时，先解释 HashMap 的键值存储，再用一个简单例子说明哈希冲突如何处理，直到不看资料也能讲清。", "适合概念理解、面试表达和章节复盘，可与主动回忆、间隔重复组合。", "把抄定义当成解释，或只追求讲得流畅而不检查因果关系。", "今天选一个正在学习的概念，录一段 2 分钟的口头解释，并写下一个卡点。", "不适合代替大量题目或编码练习；完全陌生的内容应先获得最小背景知识。")
            "间隔重复" -> MethodDetail(method.id, "根据真实回忆表现逐步拉长复习间隔，让记忆在快要遗忘时被重新提取。", "间隔与主动提取共同增加检索难度，复习结果决定下一次日期。", listOf("把知识点改写成一个可回答的问题", "合上资料独立回答并记录结果", "不会就缩短间隔，答对就逐步延长", "在下一次复习前不要反复阅读答案"), "英语单词先在当天回忆，完全不会安排明天复测，部分掌握安排 2 至 3 天后，连续答对再延长到 7 天。", "适合词汇、公式、事实和易混概念，最好和主动回忆一起使用。", "把固定日期表当成规则，不看回忆结果；复习卡片写得过于宽泛。", "建立 10 个可独立回答的卡片，今天完成第一次回忆并为每张卡标记表现。", "不能替代理解和应用；复杂技能需要加入做题、编码或输出。")
            "主动回忆" -> MethodDetail(method.id, "合上资料主动提取答案，再用资料检查遗漏。", "真正的学习信号来自能否在没有提示时提取，而不是再次阅读时的熟悉感。", listOf("把章节标题改成问题", "离开资料写出答案或步骤", "对照资料圈出遗漏", "把遗漏改成下一次复习的问题"), "复习 Java 面试时不看笔记回答“HashMap 在 JDK 8 中如何处理冲突”，再对照答案补充链表转红黑树的条件。", "适合记忆、公式、面试问答和章节复盘，可与间隔重复、错题归因组合。", "边看答案边假装回忆，或问题太大导致只写出零散关键词。", "用 5 个问题做一次 10 分钟闭卷回忆，统计独立回答数量。", "对于还没有理解的知识，先补概念，不要把不会当作单纯记忆问题。")
            "刻意练习" -> MethodDetail(method.id, "把复杂能力拆成一个可观察的薄弱技能，进行有反馈的重复练习。", "针对性练习和即时反馈能减少重复犯同一种错误。", listOf("只选择一个当前薄弱点", "完成略高于当前水平的小练习", "立即核对过程而不只看答案", "修改后换一道相似题验证"), "练习 Java 线程安全时，只关注共享计数器的竞态条件，先独立写出错误版本，再用 synchronized 或原子类修正并测试。", "适合计算、编码、题型识别和实际操作，适合在理解基础之后使用。", "一次练太多技能，重复抄答案，或者没有留下错误原因。", "选择 3 道同类题，给每道题记录“我在哪一步失去控制”。", "需要题目或作品反馈；没有基础时应先做示范和低难度练习。")
            "交错练习" -> MethodDetail(method.id, "把相近但不同的题型交错排列，训练识别和选择方法的能力。", "交错会降低短期流畅感，却能强化从题目特征到解法的判断。", listOf("选择两个已学会的相近题型", "混合排列题目而不标注类型", "做题前先写出识别依据", "复盘误判的关键特征"), "高数复习中交错导数极值与单调性题，先判断题型和条件，再决定使用哪条定理。", "适合题型辨析、计算和考试训练，可与错题归因组合。", "基础还没建立就大量混题，导致只是在猜；题型差异没有被总结。", "用 6 道题做一组交错训练，记录每题识别是否正确。", "新概念首次学习或基础准确率很低时，先用分块练习建立模式。")
            "问题驱动学习" -> MethodDetail(method.id, "从一个具体问题出发，只学习解决它所需要的知识。", "明确问题给学习提供选择标准，减少漫无目的的资料堆积。", listOf("写出最终要解决的问题", "列出可能缺少的知识", "边学习边验证假设", "完成后复盘问题是否真的解决"), "为 Java 面试准备一个“如何设计过期缓存”的问题，先列出数据结构、并发和淘汰策略，再实现一个小版本。", "适合项目、综合应用和编程学习，可与项目式学习、刻意练习组合。", "问题太大没有可交付结果，或者为了找答案跳过验证。", "把一个大问题缩成 30 分钟内能验证的最小问题。", "不适合只需要快速记忆一组事实的短任务；问题必须可观察、可验证。")
            "生成效应" -> MethodDetail(method.id, "先尝试生成答案或推理，再查看资料修正。", "先产出会暴露已有知识并建立提取线索，记忆通常比被动阅读更稳固。", listOf("在看例题前预测结论", "写下自己的推理路径", "对照答案标出差异", "把差异转成一个复习问题"), "学习二叉树遍历前，先手写一棵树的前序结果，再运行代码对照并解释差异。", "适合概念、计算、代码和写作输出，可与主动回忆组合。", "完全没有线索时强行猜，或只对比最后答案不比较推理过程。", "给一道基础题留 3 分钟先写出预测，再看解析。", "预测不等于盲猜；当背景知识不足时应先提供少量提示。")
            "对比学习" -> MethodDetail(method.id, "把容易混淆的概念并列比较，突出决定性差异。", "差异边界比单独重复定义更能帮助辨析和迁移。", listOf("选出两个相近概念", "分别写用途和限制", "列出一个关键差异", "用新例子验证差异"), "比较 Java 中 HashMap 与 ConcurrentHashMap 的线程安全、性能和使用边界，再选择一个场景说明理由。", "适合 408 概念、英语选项和 API 选择，可与交错练习组合。", "只写相同点，或者把差异写成无关紧要的表面属性。", "为一组易混概念制作一张两列表，并各写一个反例。", "概念之间没有真实重叠时不必强行对比。")
            "双向提问" -> MethodDetail(method.id, "既从知识点提出问题，也从答案反推适用条件和限制。", "双向检索会把孤立事实连接成可解释的知识网络。", listOf("为知识点写一个正向问题", "回答后追问为什么和何时不适用", "从答案反推可能的问题", "合上资料复述完整关系"), "复习 Java 线程池时，既回答“核心参数是什么”，也从一个拒绝策略反推“什么场景会触发它”。", "适合概念、记忆和面试复盘，可与费曼学习法、主动回忆组合。", "问题只停留在名称，或连续追问但不记录可验证答案。", "为今天的一个知识点写 3 个正向问题和 1 个反向问题。", "不适合时间极短且只需查阅一个明确事实的任务。")
            "项目式学习" -> MethodDetail(method.id, "通过一个可交付的小项目，把多个知识点放进真实约束中。", "可运行的成果提供了比背诵更真实的反馈，也暴露知识缺口。", listOf("定义小而明确的成果", "拆成可运行的步骤", "边实现边记录问题", "运行验证并写一次复盘"), "用 Kotlin 做一个本地学习计时器，先实现计时，再保存任务，最后补充复习日期和异常处理。", "适合编码、综合应用和职业技能，可与问题驱动学习、刻意练习组合。", "项目范围过大只看教程，或者只追求完成不复盘设计取舍。", "用 45 分钟做一个能运行的最小功能，并保留一条可验证结果。", "临近考试或时间极少时应缩小范围，不适合替代基础知识学习。")
            "最小行动法" -> MethodDetail(method.id, "把开始动作缩小到几分钟，先建立启动而不是追求一次完成很多。", "降低启动成本可以绕过拖延带来的第一道阻力。", listOf("选择一个 5 分钟内能完成的动作", "立刻开始并关闭无关干扰", "完成后再决定是否延长", "记录启动而不是只记录时长"), "把“复习英语”改成“打开错词卡并回忆 5 个词”，完成后再决定是否继续 15 分钟。", "适合启动困难、工作后学习和计划恢复，可与间歇休息组合。", "把最小动作当成最终目标，连续很多天不增加难度。", "今天只设一个 5 分钟启动任务，并在结束时写下下一步。", "它是启动策略，不能代替长期的理解、练习和复习。")
            "间歇休息与睡眠巩固" -> MethodDetail(method.id, "在专注段之间安排恢复，并利用睡眠后的回忆巩固。", "疲劳会放大粗心和注意力错误，适当间隔能保护后续练习质量。", listOf("安排一个可完成的专注段", "离开屏幕短暂休息", "结束前写下关键线索", "第二天用短回忆检查保持情况"), "高数连续做题 40 分钟后休息 10 分钟，睡前只回忆今天的两个错误原因，第二天先重做。", "适合长时间学习、记忆巩固和习惯建立，可与间隔重复组合。", "把休息刷手机变成更长的中断，或者用休息替代练习。", "安排两个 25 分钟专注段，中间休息 5 分钟，次日回忆 3 个要点。", "休息不是万能补救；如果持续疲劳，应减少计划或调整睡眠。")
            "错题归因" -> MethodDetail(method.id, "把错题按概念、识别、计算、表达或粗心等原因分类。", "找到错误机制才能改变下一次的判断，而不是重复抄一遍答案。", listOf("保留自己的错误过程", "指出第一次出错的步骤", "给错误选择具体类别", "写出下次识别信号并换题复测"), "一道二叉树题做错后，判断是遍历概念混淆还是递归边界错误，再分别补概念或做边界练习。", "适合计算、题型、编码和考试复盘，可与刻意练习、交错练习组合。", "只抄标准答案，把所有错误都归为粗心，或只改原题不换题。", "分析最近 3 道错题，统计同类错误是否重复出现。", "样本太少时不要过度下结论；复杂错误可能需要教师或同伴反馈。")
            "限时训练" -> MethodDetail(method.id, "在明确时间约束下完成已经具备基础的任务。", "时间压力会暴露速度、取舍和检查策略，而不是单纯测试知识量。", listOf("先确认基础准确率", "设定略有压力的时限", "独立完成并记录每题用时", "复盘时间花在哪里以及是否值得"), "英语阅读先保证无时限正确率，再用 18 分钟完成一篇真题，分析定位信息和检查时间。", "适合考试模拟、计算和阅读冲刺，可与错题归因组合。", "基础薄弱就盲目计时，只看总分不分析时间分配。", "选择一组熟悉题型，设置比平时少 10% 的时间并记录分段用时。", "不适合首次接触知识或需要深度理解的任务。")
            else -> MethodDetail(method.id, "把复杂文本拆成结构、逻辑和主旨，再用自己的话重建。", "结构化阅读让注意力从逐句翻译转向理解关系和论证。", listOf("划分句子或段落结构", "标记转折、因果和指代", "用一句话写主旨", "检查每个细节是否服务于主旨"), "精读 Java 官方文档的一段并发说明，先标出前提、限制和结论，再用三句话复述。", "适合英语长难句、专业资料和阅读理解，可与双向提问组合。", "逐词翻译却不看结构，把所有句子都做同样深度的分析。", "选一段 150 字材料，画出结构并写 50 字摘要。", "简单文本不必过度拆解；它不能代替词汇和背景知识积累。")
        }
    }
    fun detailFor(id: String): MethodDetail? = methodDetails.firstOrNull { it.methodId == id }
    fun recommend(kind: String, weakness: String, level: Int): List<Recommendation> = methods.map { method ->
        val reasons = mutableListOf<String>()
        var score = 25
        if (kind in method.kinds) { score += 40; reasons += "匹配当前任务类型" }
        if (weakness in method.weak) { score += 25; reasons += "针对已填写的薄弱点" }
        if (level <= 2 && method.name in setOf("费曼学习法", "主动回忆", "最小行动法")) { score += 10; reasons += "适合当前基础" }
        Recommendation(method, score.coerceAtMost(100), reasons.ifEmpty { listOf("可作为辅助方法") })
    }.sortedByDescending { it.score }

    fun generate(goal: Goal, today: LocalDate = LocalDate.now()): List<StudyTask> {
        require(goal.name.isNotBlank()) { "请填写学习目标" }
        require(goal.subjects.isNotEmpty()) { "请添加至少一门科目" }
        require(goal.minutesPerDay in 20..720) { "每日学习时间需在 20 到 720 分钟之间" }
        require(LocalDate.parse(goal.deadline).isAfter(today)) { "截止日期必须晚于今天" }
        val budget = (goal.minutesPerDay * 0.85).toInt()
        val ordered = goal.subjects.sortedByDescending { it.importance * (6 - it.level.coerceIn(1, 5)) }
        var remaining = budget
        return ordered.mapNotNull { subject ->
            val duration = minOf(45, remaining)
            if (duration < 20) return@mapNotNull null
            remaining -= duration
            val kind = when {
                subject.name.contains("数学") -> "计算"
                subject.name.contains("英语") -> "阅读"
                subject.name.contains("408") -> "概念"
                else -> "记忆"
            }
            val recommended = recommend(kind, subject.weakness, subject.level).first()
            val action = when (kind) {
                "计算" -> "理解一个薄弱概念，独立完成 5 道基础题并记录错误原因"
                "阅读" -> "精读一段材料，划分句子结构并复述主旨"
                "概念" -> "整理一个知识框架，合上资料回答 5 个自拟问题"
                else -> "整理一个核心知识点，合上资料回忆并做 5 道辨析题"
            }
            StudyTask(subjectId = subject.id, title = "${subject.name} · $action", steps = recommended.method.steps, method = recommended.method.name, minutes = duration, due = today.toString())
        }
    }
    fun nextReview(today: LocalDate, result: Int, repetitions: Int): LocalDate {
        val days = when (result) { 0 -> 1; 1 -> 2; else -> listOf(1L, 3L, 7L, 14L, 30L, 45L)[repetitions.coerceIn(0, 5)] }
        return today.plusDays(days.toLong())
    }
    fun daysLeft(goal: Goal, today: LocalDate = LocalDate.now()) = ChronoUnit.DAYS.between(today, LocalDate.parse(goal.deadline)).coerceAtLeast(0)
}
