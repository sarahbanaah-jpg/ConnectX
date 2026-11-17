package com.connectx.social.util

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AiSecurityAssistant(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    data class AnalysisResult(
        val shouldFlag: Boolean = false,
        val shouldBlock: Boolean = false,
        val reason: String = "",
        val severity: String = "low"
    )

    private val blockedWords = setOf(
        "kill yourself", "kys", "bomb threat", "shoot up", "terrorist attack",
        "child porn", "cp ", "nude minors", "underage sex"
    )

    private val suspiciousPatterns = listOf(
        Regex("(?i)\\b(free\\s+money|crypto\\s+giveaway|send\\s+btc)\\b"),
        Regex("(?i)\\b(click\\s+here\\s+now|limited\\s+offer|act\\s+now)\\b"),
        Regex("(?i)https?://[\\w.-]+\\.(xyz|top|click|loan|gq)\\b"),
        Regex("(?i)\\b(nazi|white\\s+power)\\b")
    )

    fun analyzePost(content: String): AnalysisResult {
        val lower = content.lowercase()
        for (word in blockedWords) {
            if (lower.contains(word)) {
                return AnalysisResult(true, true, "Prohibited content detected", "high")
            }
        }
        for (pattern in suspiciousPatterns) {
            if (pattern.containsMatchIn(content)) {
                return AnalysisResult(true, false, "Suspicious pattern or potential scam/hate content", "medium")
            }
        }
        if (content.length > 2000) {
            return AnalysisResult(true, false, "Unusually long content", "low")
        }
        if (Regex("(.)\\1{8,}").containsMatchIn(content)) {
            return AnalysisResult(true, false, "Spam-like repeated characters", "low")
        }
        return AnalysisResult()
    }

    suspend fun createFlag(type: String, targetId: String, reason: String, severity: String = "medium") {
        try {
            val data = mapOf(
                "type" to type, "targetId" to targetId, "reason" to reason,
                "severity" to severity, "reportedBy" to "ai_assistant",
                "createdAt" to Timestamp.now(), "resolved" to false, "resolvedBy" to ""
            )
            db.collection("security_flags").add(data).await()
        } catch (_: Exception) { }
    }
}
