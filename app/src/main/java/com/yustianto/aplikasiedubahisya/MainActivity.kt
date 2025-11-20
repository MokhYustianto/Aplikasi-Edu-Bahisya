package com.yustianto.aplikasiedubahisya

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.yustianto.aplikasiedubahisya.ui.theme.AplikasiEduBahisyaTheme
import kotlinx.serialization.json.Json
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val learningData = loadLearningData(this)
        val quizData = loadQuizData(this)

        setContent {
            EduBahisyaApp(learningData = learningData, quizData = quizData)
        }
    }
}

@Composable
fun EduBahisyaApp(learningData: LearningData?, quizData: QuizData?) {
    AplikasiEduBahisyaTheme {
        AppNavHost(learningData = learningData, quizData = quizData)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
// Membuat Fungsi untuk Navigasi setiap halaman Edu Bahisya
fun AppNavHost(learningData: LearningData?, quizData: QuizData?) {
    val navController = rememberNavController()
    NavHost(
        navController = navController, 
        startDestination = "home",
        enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(300)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(300)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(300)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300)) }
    ) {
        composable("home") {
            HomeScreen(
                learningData = learningData,
                onCategoryClick = { categoryId -> navController.navigate("learning/$categoryId") },
                onQuizClick = { navController.navigate("quiz_selection") }
            )
        }
        composable(
            route = "learning/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")
            LearningScreen(
                learningData = learningData,
                categoryId = categoryId,
                navController = navController,
                onVideoClick = { videoFileName -> navController.navigate("video/$videoFileName") }
            )
        }
        composable(
            route = "video/{fileName}",
            arguments = listOf(navArgument("fileName") { type = NavType.StringType }),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName")
            VideoPlayerScreen(fileName = fileName, navController = navController)
        }
        composable("quiz_selection") {
            QuizSelectionScreen(quizData = quizData, navController = navController)
        }
        composable(
            route = "quiz/{quizId}",
            arguments = listOf(navArgument("quizId") { type = NavType.StringType })
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId")
            QuizScreen(quizData = quizData, quizId = quizId, navController = navController)
        }
    }
}

// --- COMPOSABLE UNTUK SETIAP LAYAR ---

@Composable
// Membuat Layout Halaman Utama atau Beranda
fun HomeScreen(learningData: LearningData?, onCategoryClick: (String) -> Unit, onQuizClick: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(topBar = { MainAppBar() }, modifier = modifier) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(learningData?.categories ?: emptyList()) { category ->
                val nameParts = category.name.split(" ", limit = 2)
                HomeMenuButton(
                    title = nameParts.getOrNull(0) ?: "",
                    subtitle = nameParts.getOrNull(1) ?: "",
                    onClick = { onCategoryClick(category.id) }
                )
            }
            item { HomeMenuButton(title = "Kuis", subtitle = "Bahisya", onClick = onQuizClick) }
        }
    }
}

@Composable
// Membuat Layout Halaman Pembelajaran
fun LearningScreen(learningData: LearningData?, categoryId: String?, navController: NavController, onVideoClick: (String) -> Unit) {
    val category = remember(learningData, categoryId) {
        learningData?.categories?.find { it.id == categoryId }
    }

    Scaffold(topBar = { DetailAppBar(title = category?.name ?: "Belajar", onBackClick = { navController.popBackStack() }) }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            category?.description?.let {
                Text(text = it, modifier = Modifier.padding(16.dp), fontSize = 16.sp)
            }
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(category?.videos ?: emptyList()) { video ->
                    VideoListItem(videoInfo = video, onClick = { onVideoClick(video.fileName) })
                }
            }
        }
    }
}

@Composable
// Membuat Layout Kuis
fun QuizSelectionScreen(quizData: QuizData?, navController: NavController) {
    Scaffold(topBar = { DetailAppBar(title = "Pilih Kuis", onBackClick = { navController.popBackStack() }) }) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(quizData?.quizzes ?: emptyList()) { quiz ->
                val titleParts = quiz.title.replace("Kuis: ", "").split(" ", limit = 2)
                HomeMenuButton(
                    title = titleParts.getOrNull(0) ?: "",
                    subtitle = titleParts.getOrNull(1) ?: "",
                    onClick = { navController.navigate("quiz/${quiz.quizId}") }
                )
            }
        }
    }
}

@Composable
// Untuk Pemutar Video Pembelajaran
fun VideoPlayerScreen(fileName: String?, navController: NavController) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    LaunchedEffect(fileName) {
        fileName?.let {
            val videoUri = Uri.parse("asset:///$it")
            val mediaItem = MediaItem.fromUri(videoUri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Scaffold(topBar = { DetailAppBar(title = fileName ?: "Pemutar Video", onBackClick = { navController.popBackStack() }) }) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(Color.Black)) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply { player = exoPlayer }
                },
                modifier = Modifier.fillMaxSize().align(Alignment.Center)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuizScreen(quizData: QuizData?, quizId: String?, navController: NavController) {
    val quiz = remember(quizData, quizId) {
        quizData?.quizzes?.find { it.quizId == quizId }
    }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionId by remember { mutableStateOf<String?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var showResultDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { DetailAppBar(title = quiz?.title ?: "Kuis", onBackClick = { navController.popBackStack() }) },
        bottomBar = {
            val buttonText = if (showResult) "Lanjut" else "Periksa Jawaban"
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (showResult) { // Tombol "Soal Berikutnya"
                            if (currentQuestionIndex < (quiz?.questions?.size ?: 0) - 1) {
                                currentQuestionIndex++
                                selectedOptionId = null
                                showResult = false
                            } else {
                                showResultDialog = true // Tampilkan dialog hasil
                            }
                        } else { // Tombol "Periksa Jawaban"
                            if (selectedOptionId != null) {
                                if (selectedOptionId == quiz?.questions?.get(currentQuestionIndex)?.correctAnswerId) {
                                    score++
                                }
                                showResult = true
                            }
                        }
                    }
                ) { 
                    Text(buttonText, fontSize = 18.sp)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = buttonText)
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentQuestionIndex,
            transitionSpec = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) togetherWith
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
            },
            label = "QuizQuestionAnimation"
        ) {
            val question = quiz?.questions?.getOrNull(it)
            if (question != null) {
                LazyColumn(
                    modifier = Modifier.padding(innerPadding).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {

                        QuizQuestion(
                            question = question,
                            selectedOptionId = selectedOptionId,
                            showResult = showResult,
                            onOptionSelected = { optionId ->
                                if (!showResult) {
                                    selectedOptionId = optionId
                                }
                            }
                        )
                    }
                }
            } else {
                // Menampilkan Pop-Up Hasil Kuis
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Kuis Selesai!")
                }
            }
        }
    }

    if (showResultDialog) {
        QuizResultDialog(
            score = score,
            totalQuestions = quiz?.questions?.size ?: 0,
            onDismiss = { 
                showResultDialog = false
                navController.popBackStack()
            }
        )
    }
}


// --- KOMPONEN-KOMPONEN UI KECIL EDU BAHISYA---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar() {
    TopAppBar(
        title = { Text("Edu Bahisya", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF007BFF),
            titleContentColor = Color.White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailAppBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF007BFF),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Membuat Tombol Halaman Utama dan juga Halaman Kuis
fun HomeMenuButton(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1.2f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.CenterStart) {
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                if (subtitle.isNotEmpty()) {
                    Text(text = subtitle, fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
// Membuat Tombol Halaman Pembelajaran
fun VideoListItem(videoInfo: VideoInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play Video", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = videoInfo.title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
// Membuat Komponen Soal Kuis
fun QuizQuestion(
    question: Question,
    selectedOptionId: String?,
    showResult: Boolean,
    onOptionSelected: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        question.questionImage?.let {
            AsyncImage(
                model = "file:///android_asset/${it}",
                contentDescription = "Gambar Pertanyaan",
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.None
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = question.questionText, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            question.options.forEach { option ->
                AnswerOptionItem(
                    option = option,
                    isSelected = option.optionId == selectedOptionId,
                    showResult = showResult,
                    isCorrect = option.optionId == question.correctAnswerId,
                    onOptionSelected = { onOptionSelected(option.optionId) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
// Membuat Komponen Pilihan Jawaban
fun AnswerOptionItem(option: Option, isSelected: Boolean, showResult: Boolean, isCorrect: Boolean, onOptionSelected: () -> Unit) {
    val borderColor = when {
        showResult && isCorrect -> Color(0xFF4CAF50) // Hijau
        showResult && isSelected && !isCorrect -> Color(0xFFF44336) // Merah
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Gray
    }
    val borderWidth = if (isSelected || (showResult && isCorrect)) 2.dp else 1.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(12.dp))
            .clickable(enabled = !showResult, onClick = onOptionSelected)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (option.image != null) {
            AsyncImage(
                model = "file:///android_asset/${option.image}",
                contentDescription = "Gambar Pilihan Jawaban",
                modifier = Modifier
                    .size(185.dp)
                    .clip(RoundedCornerShape(80.dp))
                    .aspectRatio(1f),
                contentScale = ContentScale.Fit


            )
        }
        option.text?.let {
            val label = "${option.optionId.lowercase()}. "
            Text(text = "$label$it", fontSize = 16.sp)
        }
    }
}

@Composable
// Membuat Komponen Hasil Kuis
fun QuizResultDialog(score: Int, totalQuestions: Int, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Hasil Kuis", fontWeight = FontWeight.Bold) },
        text = { Text(text = "Anda berhasil menjawab dengan benar $score dari $totalQuestions soal!", fontSize = 16.sp) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Selesai")
            }
        }
    )
}

// --- FUNGSI PEMUATAN DATA ---
private fun loadLearningData(context: Context): LearningData? {
    return try {
        val jsonString = context.assets.open("data.json").bufferedReader().use { it.readText() }
        Json.decodeFromString(jsonString)
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        null
    }
}

private fun loadQuizData(context: Context): QuizData? {
    return try {
        val jsonString = context.assets.open("kuis.json").bufferedReader().use { it.readText() }
        Json.decodeFromString(jsonString)
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        null
    }
}

// --- PRATINJAU (PREVIEW) ---

@Preview(showBackground = true)
@Composable
fun QuizResultDialogPreview() {
    AplikasiEduBahisyaTheme {
        QuizResultDialog(score = 2, totalQuestions = 3, onDismiss = {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun HomeScreenPreview() {
    val fakeCategories = listOf(
        LearningCategory("cat01", "Cakapan Sehari", "", emptyList()),
        LearningCategory("cat02", "Cakapan Kantor", "", emptyList())
    )
    val fakeData = LearningData(fakeCategories)

    AplikasiEduBahisyaTheme {
        HomeScreen(
            learningData = fakeData,
            onCategoryClick = {},
            onQuizClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LearningScreenPreview() {
    val fakeCategory = LearningCategory(
        id = "cat04",
        name = "Bahisya Dasar",
        description = "Ini adalah deskripsi singkat untuk kategori Bahisya Dasar.",
        videos = listOf(VideoInfo("v01", "Alfabet A-Z", ""))

    )
    val fakeData = LearningData(listOf(fakeCategory))

    AplikasiEduBahisyaTheme {
        LearningScreen(categoryId = "cat04", learningData = fakeData, navController = rememberNavController(), onVideoClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun QuizSelectionScreenPreview() {
    val fakeQuizzes = listOf(
        Quiz("q01", "cat01", "Kuis: Cakapan Sehari-hari", emptyList()),
        Quiz("q02", "cat02", "Kuis: Cakapan Kantor", emptyList())
    )
    val fakeQuizData = QuizData(fakeQuizzes)
    AplikasiEduBahisyaTheme {
        QuizSelectionScreen(quizData = fakeQuizData, navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun VideoPlayerScreenPreview() {
    AplikasiEduBahisyaTheme {
        VideoPlayerScreen(fileName = "preview.mp4", navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun QuizScreenPreview() {
    val fakeQuestion = Question(
        qId = "q01",
        questionText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        questionImage = "Halo.jpg",
        options = listOf(
            Option("a", text = "Pilihan Jawaban A"),
            Option("b", image = "Maaf.jpg"),
            Option("c", text = "Pilihan Jawaban C")
        ),
        correctAnswerId = "a"
    )
    val fakeQuiz = Quiz("q01", "cat01", "Kuis Bahisya Dasar", listOf(fakeQuestion))
    val fakeQuizData = QuizData(listOf(fakeQuiz))

    AplikasiEduBahisyaTheme {
        QuizScreen(quizData = fakeQuizData, quizId = "q01", navController = rememberNavController())
    }
}
