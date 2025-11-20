package com.yustianto.aplikasiedubahisya

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Data Model untuk data.json ---

@Serializable
data class LearningData(
    val categories: List<LearningCategory>
)

@Serializable
data class LearningCategory(
    val id: String,
    val name: String,
    val description: String,
    val videos: List<VideoInfo>
)

@Serializable
data class VideoInfo(
    val id: String,
    val title: String,
    val fileName: String
)

// --- Data Model untuk kuis.json ---

@Serializable
data class QuizData(
    val quizzes: List<Quiz>
)

@Serializable
data class Quiz(
    val quizId: String,
    val categoryId: String,
    val title: String,
    val questions: List<Question>
)

@Serializable
data class Question(
    @SerialName("q_id") // Memberi tahu Kotlin untuk mencari "q_id" di JSON
    val qId: String,      // Menggunakan nama yang sesuai standar Kotlin
    val questionText: String,
    val questionImage: String? = null, // Optional, bisa jadi null
    val options: List<Option>,
    val correctAnswerId: String
)

@Serializable
data class Option(
    val optionId: String,
    val text: String? = null, // Optional, bisa jadi null
    val image: String? = null // Optional, bisa jadi null
)
