package com.atlaes.learnpilot.core

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class PlannerTest {
    @Test fun methodsRespondToTaskTypeAndWeakness() {
        val memory = Planner.recommend("记忆", "容易遗忘", 2).take(3).map { it.method.name }
        val coding = Planner.recommend("编码", "缺少练习", 3).take(3).map { it.method.name }
        assertTrue(memory.contains("间隔重复") || memory.contains("主动回忆"))
        assertTrue(coding.contains("刻意练习") || coding.contains("项目式学习"))
        assertNotEquals(memory, coding)
    }
    @Test fun planRespectsAvailableMinutes() {
        val today = LocalDate.of(2026, 10, 14)
        val goal = Goal(name = "考研", deadline = "2027-01-01", minutesPerDay = 120, subjects = listOf(Subject(name = "高等数学"), Subject(name = "408"), Subject(name = "英语")))
        assertTrue(Planner.generate(goal, today).sumOf { it.minutes } <= 120)
    }
    @Test fun reviewRespondsToRecall() {
        val today = LocalDate.of(2026, 10, 14)
        assertEquals(today.plusDays(1), Planner.nextReview(today, 0, 4))
        assertTrue(Planner.nextReview(today, 2, 4).isAfter(Planner.nextReview(today, 1, 4)))
    }
    @Test fun rejectsImpossibleInput() {
        assertThrows(IllegalArgumentException::class.java) { Planner.generate(Goal(name = "x", deadline = "2026-01-01", minutesPerDay = 10, subjects = emptyList()), LocalDate.of(2026, 10, 14)) }
    }
    @Test fun everyMethodHasStableDetailContent() {
        assertEquals(Planner.methods.size, Planner.methods.map { it.id }.toSet().size)
        Planner.methods.forEach { method ->
            val detail = Planner.detailFor(method.id)
            assertNotNull(detail)
            assertTrue(detail!!.summary.isNotBlank())
            assertTrue(detail.steps.size >= 3)
            assertTrue(detail.example.isNotBlank())
        }
    }
}
