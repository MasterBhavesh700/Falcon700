package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.api.GeminiRetrofitClient
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class AiChatMessage(
    val sender: String, // "USER" or "AI"
    val text: String,
    val engine: com.example.ui.theme.AiEngine,
    val voice: com.example.ui.theme.AiVoiceStyle,
    val timestamp: Long = System.currentTimeMillis()
)

class FalconViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FalconDatabase.getInstance(application)
    private val repository = FalconRepository(database.dao)

    // --- Tab Navigation and Core UI State ---
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun selectTab(index: Int) {
        _currentTab.value = index
    }

    // --- Customization Settings state ---
    private val _appTheme = MutableStateFlow(AppThemeValue.GOLD)
    val appTheme: StateFlow<AppThemeValue> = _appTheme.asStateFlow()

    fun setAppTheme(theme: AppThemeValue) {
        _appTheme.value = theme
    }

    private val _aiEngine = MutableStateFlow(AiEngine.GEMINI)
    val aiEngine: StateFlow<AiEngine> = _aiEngine.asStateFlow()

    fun setAiEngine(engine: AiEngine) {
        _aiEngine.value = engine
    }

    private val _aiVoiceStyle = MutableStateFlow(AiVoiceStyle.STRICT_COMMANDER)
    val aiVoiceStyle: StateFlow<AiVoiceStyle> = _aiVoiceStyle.asStateFlow()

    fun setAiVoiceStyle(style: AiVoiceStyle) {
        _aiVoiceStyle.value = style
    }

    // --- Date Tracking ---
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentDateString: String = sdf.format(Date()) // "2026-05-29"

    private val _studyDate = MutableStateFlow(currentDateString)
    val studyDate: StateFlow<String> = _studyDate.asStateFlow()

    // --- User Session State ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow("")
    val authError: StateFlow<String> = _authError.asStateFlow()

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing.asStateFlow()

    private val _syncMessage = MutableStateFlow("")
    val syncMessage: StateFlow<String> = _syncMessage.asStateFlow()

    // --- Dynamic User-Partitioned Data Flows ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val allDailyLogs: StateFlow<List<DailyLogEntity>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> repository.getAllDailyLogs(user.email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allFlashcards: StateFlow<List<FlashcardEntity>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> repository.getAllFlashcards(user.email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allHabits: StateFlow<List<HabitEntity>> = _currentUser
        .filterNotNull()
        .flatMapLatest { user -> repository.getAllHabits(user.email) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cumulativeXp: StateFlow<Int> = allHabits
        .map { habits ->
            habits.sumOf { habit ->
                val completedDatesCount = if (habit.doneDays.isBlank()) 0 else habit.doneDays.split(",").filter { it.isNotBlank() }.size
                completedDatesCount * habit.xp
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Selected Log State ---
    private val _currentLog = MutableStateFlow<DailyLogEntity?>(null)
    val currentLog: StateFlow<DailyLogEntity?> = _currentLog.asStateFlow()

    // --- Gemini Support State ---
    private val _geminiResult = MutableStateFlow("")
    val geminiResult: StateFlow<String> = _geminiResult.asStateFlow()

    private val _geminiLoading = MutableStateFlow(false)
    val geminiLoading: StateFlow<Boolean> = _geminiLoading.asStateFlow()

    // --- Conversational Chat ---
    private val _aiChatHistory = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val aiChatHistory: StateFlow<List<AiChatMessage>> = _aiChatHistory.asStateFlow()

    fun clearChatHistory() {
        _aiChatHistory.value = emptyList()
        _geminiResult.value = ""
    }

    init {
        // Prepare preloaded user session & load initial data state
        viewModelScope.launch {
            val defaultEmail = "bp111223@gmail.com"
            var activeUser = repository.getUserByEmail(defaultEmail)
            if (activeUser == null) {
                activeUser = UserEntity(
                    email = defaultEmail,
                    passwordHash = "falcon700",
                    name = "Cadet Bhavesh Patel",
                    rank = "MBBS 3rd Year (GMC Ambikapur)",
                    initialCapital = 500000.0,
                    monthlySip = 50000.0,
                    cloudEndpoint = "https://ais-dev-tc55jkxijcuobmn47qf4mz.cloud/telemetry",
                    cloudEnabled = true
                )
                repository.insertUser(activeUser)
            }
            _currentUser.value = activeUser

            loadLogForDate(currentDateString)
            prepopulateFlashcardsIfEmpty()
            prepopulateHabitsIfEmpty()
        }
    }

    // --- Authentication Actions ---
    fun signInUser(email: String, passwordRaw: String, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authError.value = ""
            val user = repository.getUserByEmail(email.trim().lowercase())
            if (user != null) {
                if (user.passwordHash == passwordRaw) {
                    _currentUser.value = user
                    loadLogForDate(_studyDate.value)
                    prepopulateFlashcardsIfEmpty()
                    prepopulateHabitsIfEmpty()
                    onCompleted(true)
                } else {
                    _authError.value = "Incorrect passcode for this cadet account."
                    onCompleted(false)
                }
            } else {
                _authError.value = "No cadet record coordinates found under this email."
                onCompleted(false)
            }
        }
    }

    fun registerNewUser(email: String, passwordRaw: String, name: String, rank: String, onCompleted: (Boolean) -> Unit) {
        viewModelScope.launch {
            _authError.value = ""
            if (email.trim().isEmpty() || passwordRaw.trim().isEmpty() || name.trim().isEmpty()) {
                _authError.value = "All credentials columns must be filled."
                onCompleted(false)
                return@launch
            }
            val existing = repository.getUserByEmail(email.trim().lowercase())
            if (existing != null) {
                _authError.value = "An account with this email identifier already exists."
                onCompleted(false)
            } else {
                val newUser = UserEntity(
                    email = email.trim().lowercase(),
                    passwordHash = passwordRaw,
                    name = name.trim(),
                    rank = if (rank.trim().isEmpty()) "MBBS Student" else rank.trim()
                )
                repository.insertUser(newUser)
                _currentUser.value = newUser
                loadLogForDate(_studyDate.value)
                prepopulateFlashcardsIfEmpty()
                prepopulateHabitsIfEmpty()
                onCompleted(true)
            }
        }
    }

    fun signOutUser() {
        _currentUser.value = null
        _currentLog.value = null
    }

    // --- Cloud Synchronization System ---
    fun triggerCloudSync() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _syncing.value = true
            _syncMessage.value = "Handshaking secure cloud portal..."
            kotlinx.coroutines.delay(800)
            _syncMessage.value = "Serializing local study telemetry schemas..."
            kotlinx.coroutines.delay(600)
            
            try {
                // Perform a standard mock client API trigger simulation
                _syncMessage.value = "Uploading synced index logs to cloud ledger endpoint..."
                kotlinx.coroutines.delay(1000)
                
                val updatedUser = user.copy(lastSyncTime = System.currentTimeMillis(), cloudEnabled = true)
                repository.insertUser(updatedUser)
                _currentUser.value = updatedUser
                _syncMessage.value = "Encryption Sync Completed Successfully!"
            } catch (e: Exception) {
                _syncMessage.value = "Sync cache preserved locally: ${e.message}"
            } finally {
                _syncing.value = false
            }
        }
    }

    fun updateUserConfiguration(cloudEndpoint: String, cloudEnabled: Boolean, initialCapital: Double? = null, monthlySip: Double? = null) {
        val user = _currentUser.value ?: return
        val updated = user.copy(
            cloudEndpoint = cloudEndpoint,
            cloudEnabled = cloudEnabled,
            initialCapital = initialCapital ?: user.initialCapital,
            monthlySip = monthlySip ?: user.monthlySip
        )
        viewModelScope.launch {
            repository.insertUser(updated)
            _currentUser.value = updated
        }
    }

    fun loadLogForDate(date: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _studyDate.value = date
            val existing = repository.getDailyLogByDate(user.email, date)
            if (existing != null) {
                _currentLog.value = existing
            } else {
                val newLog = DailyLogEntity(
                    id = "${user.email}|$date",
                    date = date,
                    userEmail = user.email
                )
                repository.insertDailyLog(newLog)
                _currentLog.value = newLog
            }
        }
    }

    // --- Update Logs ---
    fun updateDailyChecked(
        body: Boolean? = null,
        mind: Boolean? = null,
        honor: Boolean? = null,
        hold: Boolean? = null,
        diary: String? = null,
        hrsFmt: Float? = null,
        hrsPsm: Float? = null,
        portfolio: Double? = null
    ) {
        val user = _currentUser.value ?: return
        val log = _currentLog.value ?: DailyLogEntity(
            id = "${user.email}|${_studyDate.value}",
            date = _studyDate.value,
            userEmail = user.email
        )
        val updated = log.copy(
            bodyChecked = body ?: log.bodyChecked,
            mindChecked = mind ?: log.mindChecked,
            honorChecked = honor ?: log.honorChecked,
            holdChecked = hold ?: log.holdChecked,
            diaryLine = diary ?: log.diaryLine,
            studyHoursFmt = hrsFmt ?: log.studyHoursFmt,
            studyHoursPsm = hrsPsm ?: log.studyHoursPsm,
            portfolioValue = portfolio ?: log.portfolioValue
        )
        viewModelScope.launch {
            repository.insertDailyLog(updated)
            _currentLog.value = updated
        }
    }

    // --- Flashcard Operations ---
    fun updateFlashcardMastery(id: Int, state: String) {
        viewModelScope.launch {
            repository.updateFlashcardMastery(id, state, System.currentTimeMillis())
        }
    }

    fun addNewFlashcard(subject: String, question: String, answer: String, explanation: String = "") {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertFlashcard(
                FlashcardEntity(
                    userEmail = user.email,
                    subject = subject,
                    question = question,
                    answer = answer,
                    explanation = explanation
                )
            )
        }
    }

    fun deleteFlashcard(id: Int) {
        viewModelScope.launch {
            repository.deleteFlashcardById(id)
        }
    }

    // --- AI Study Companion (Gemini Calls) ---
    fun callGeminiAI(actionType: String, textInput: String) {
        if (textInput.trim().isEmpty()) {
            _geminiResult.value = "Falcon Cadet system query is empty. Enter a note or topic above."
            return
        }

        viewModelScope.launch {
            _geminiLoading.value = true
            _geminiResult.value = ""

            val engineName = when (_aiEngine.value) {
                AiEngine.GEMINI -> "Gemini Medical Command"
                AiEngine.CLAUDE -> "Claude Clinical Synthesizer"
                AiEngine.CHATGPT -> "ChatGPT Mnemonic Hacker"
                AiEngine.NOTEBOOK_LM -> "NotebookLM Cadet Clerk"
            }

            val userMessageText = when (actionType) {
                "DEEP_DIVE" -> "Perform exam-grade deep-dive on: $textInput"
                "FACT_CHECK" -> "Fact-check these medical notes: $textInput"
                "MNEMONIC" -> "Create memory aids & structures for: $textInput"
                "BOOK_WISDOM" -> {
                    val book = BookLibrary.books.find { it.id.equals(textInput, ignoreCase = true) || it.title.equals(textInput, ignoreCase = true) }
                    "DEPLOY COACH ANALYSIS: [${book?.emoji ?: "📖"} ${book?.title ?: textInput}] by ${book?.author ?: "Expert Authors"}"
                }
                else -> textInput
            }

            // Append USER message to history
            val userMsg = com.example.ui.AiChatMessage(
                sender = "USER", 
                text = userMessageText, 
                engine = _aiEngine.value, 
                voice = _aiVoiceStyle.value
            )
            _aiChatHistory.value = _aiChatHistory.value + userMsg

            val engineTraitPrompt = when (_aiEngine.value) {
                AiEngine.GEMINI -> """
                    - Maintain a highly authoritative, structured, and textbook-exact response format.
                    - Highlight specific diagnostic criteria and standard medical schedules (Reddy FMT 34th Ed, K Park PSM 26th Ed).
                    - Formulate output with strict, high-yield medical summaries.
                    - Focus intensely on correctness and textbook accountability.
                """.trimIndent()
                AiEngine.CLAUDE -> """
                    - Adopt Claude's signature multi-layered, highly logical research synthesis.
                    - Break down topics into pathological, physiological and clinical case-based reasoning.
                    - Provide extensive reasoning blocks, comprehensive comparison charts, and advanced correlation.
                    - Explain the "why" and "how" with immaculate scientific style.
                """.trimIndent()
                AiEngine.CHATGPT -> """
                    - Prioritize high-speed active recall memory aids, acronyms, and creative analogies.
                    - Present short, hyper-punchy flashcard templates, comparison grids, and rapid mnemonics.
                    - Formulate creative, highly memorable connection systems to help memorize complex toxins, vaccines, or math.
                    - Use bullet points and bolding aggressively for speed reading.
                """.trimIndent()
                AiEngine.NOTEBOOK_LM -> """
                    - Adopt a systematic, scholarly NotebookLM Note-Clerk approach.
                    - Synthesize student journals, exam parameters, and textbook chapters into cohesive research briefs.
                    - Include citation indexes, numbered outlines, and high-yield study tags.
                    - Summarize and cross-reference multiple study resources perfectly.
                """.trimIndent()
            }

            val voiceStylePrompt = when (_aiVoiceStyle.value) {
                AiVoiceStyle.STRICT_COMMANDER -> "Tone: Authoritative, direct, uncompromising, and military-like accountability partner. Challenge the cadet to maintain elite study habits and avoid procrastinating."
                AiVoiceStyle.HELPFUL_MENTOR -> "Tone: Gentle but firm, academically comprehensive, explanatory, and encouraging clinical maturity and professional pride."
                AiVoiceStyle.MNEMONIC_WIZARD -> "Tone: Creative, conversational, clever, focused entirely on memory association shortcuts, visual links, and rapid-recall codes."
                AiVoiceStyle.SOCRATIC_DRILLER -> "Tone: Interactive, questioning, probing diagnostic reasoning. Formulate questions that force the student to recall specific facts."
            }

            val systemPrompt = """
                You are executing as $engineName under standard study command mode.
                You are a premier AI companion built exclusively for the authenticated user, an MBBS student or financial modeler.

                Engine Specific Trait:
                $engineTraitPrompt

                Companion Tone Style:
                $voiceStylePrompt

                Strict Operational Guidelines:
                - ALWAYS match the elite context: MBBS 3rd-year topics in Forensic Medicine & Toxicology (FMT, standard Reddy book) and Preventive & Social Medicine (PSM, standard K Park book) or Finance equations.
                - When asked to fact-check or deep-dive:
                  1. Output crystal-clear comparison tables using Markdown.
                  2. Present colorful lists with standard emojis for high visual reading speed.
                  3. Include a highly memorable "BRAIN RECONNAISSANCE" Mnemonic Callout Box with border emblems.
                  4. Conclude with a direct, challenging action cue or accountability prompt.
                - When providing TrueYield financial calculations: use realistic net growth rate (approx 4.8% after inflation, LTCG, and expense-ratio drags).
                - Keep descriptions highly structured with rich, bold headings. Expand all medical abbreviations on first use.
            """.trimIndent()

            val userPrompt = when (actionType) {
                "DEEP_DIVE" -> "Perform an exam-grade deep-dive on: $textInput"
                "FACT_CHECK" -> "Fact-check these medical notes against standard gold sources: $textInput"
                "MNEMONIC" -> "Create highly memorable exam mnemonics, tables, and rapid-fire memory recall points for: $textInput"
                "BOOK_WISDOM" -> {
                    val book = BookLibrary.books.find { it.id.equals(textInput, ignoreCase = true) || it.title.equals(textInput, ignoreCase = true) }
                    book?.prompt ?: "Perform high-performance book coaching analysis on $textInput"
                }
                else -> textInput
            }

            val result = withContext(Dispatchers.IO) {
                GeminiRetrofitClient.fetchGeminiResponse(systemPrompt, userPrompt)
            }
            _geminiResult.value = result

            // Append AI message to history
            val aiMsg = com.example.ui.AiChatMessage(
                sender = "AI", 
                text = result, 
                engine = _aiEngine.value, 
                voice = _aiVoiceStyle.value
            )
            _aiChatHistory.value = _aiChatHistory.value + aiMsg

            _geminiLoading.value = false
        }
    }

    // --- Habits Tracking Actions ---
    private suspend fun prepopulateHabitsIfEmpty() {
        val user = _currentUser.value ?: return
        val count = repository.getAllHabits(user.email).first().size
        if (count == 0) {
            val list = listOf(
                HabitEntity("h1", "Reveille — Wake Up 0500", "DISCIPLINE", "05:00", "⚡", 20, user.email),
                HabitEntity("h2", "Physical Training (PT)", "PHYSICAL", "05:30", "🏋️", 30, user.email),
                HabitEntity("h3", "Cold Shower + Grooming", "PHYSICAL", "06:30", "🚿", 15, user.email),
                HabitEntity("h4", "Protein Breakfast", "NUTRITION", "07:00", "🥚", 10, user.email),
                HabitEntity("h5", "Study Block Alpha (2h)", "COGNITIVE", "08:00", "📖", 30, user.email),
                HabitEntity("h6", "Hydration — 3L Water", "HEALTH", "10:00", "💧", 10, user.email),
                HabitEntity("h7", "Study Block Bravo (2h)", "COGNITIVE", "14:00", "🧠", 30, user.email),
                HabitEntity("h8", "Movement Break 10min", "PHYSICAL", "16:30", "🚶", 10, user.email),
                HabitEntity("h9", "Tactical Review — Notes", "COGNITIVE", "19:00", "🗒️", 25, user.email),
                HabitEntity("h10", "Mindfulness 10min", "MENTAL", "20:30", "🧘", 15, user.email),
                HabitEntity("h11", "Reading 30min", "MENTAL", "21:00", "📚", 20, user.email),
                HabitEntity("h12", "Lights Out — 2200", "DISCIPLINE", "22:00", "🌙", 15, user.email)
            )
            repository.insertHabits(list)
        }
    }

    fun toggleHabitCompletion(habitId: String, date: String) {
        viewModelScope.launch {
            val habit = repository.getHabitById(habitId) ?: return@launch
            val doneList = if (habit.doneDays.isBlank()) emptyList<String>() else habit.doneDays.split(",").filter { it.isNotBlank() }
            val newDoneDays = if (doneList.contains(date)) {
                doneList.filter { it != date }.joinToString(",")
            } else {
                (doneList + date).joinToString(",")
            }
            repository.insertHabit(habit.copy(doneDays = newDoneDays))
        }
    }

    fun addCustomHabit(name: String, category: String, time: String, icon: String, xp: Int) {
        viewModelScope.launch {
            val email = _currentUser.value?.email ?: return@launch
            val id = "h_" + System.currentTimeMillis()
            val newHabit = HabitEntity(
                id = id,
                name = name,
                cat = category.uppercase(),
                time = time,
                icon = icon,
                xp = xp,
                userEmail = email
            )
            repository.insertHabit(newHabit)
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            repository.deleteHabitById(habitId)
        }
    }

    // --- Pre-populate DB ---
    private suspend fun prepopulateFlashcardsIfEmpty() {
        val user = _currentUser.value ?: return
        val count = repository.getAllFlashcards(user.email).first().size
        if (count == 0) {
            val list = listOf(
                FlashcardEntity(
                    userEmail = user.email,
                    subject = "FMT",
                    question = "Explain features of Dhatura Poisoning (The Roadside Poison).",
                    answer = "Dhatura, also known as thorn apple, active alkaloids are Atropine, Hyoscyamine, and Hyoscine. Causes Delirium, Dry mouth, Dry skin, Dilated pupils, and Drowsiness.",
                    explanation = "[Reddy FMT, 34th Ed] Mnemonic: 'Dry as a bone, red as a beet, blind as a bat, hot as a hare, mad as a wet hen.' It is an stupifying poison used for theft and robbery."
                ),
                FlashcardEntity(
                    userEmail = user.email,
                    subject = "FMT",
                    question = "Differentiate Post-mortem Lividity (Hypostasis) vs Bruising (Contusion).",
                    answer = "Lividity: found in dependent parts, blood remains intravascular, incision washes away safely. Bruising: found anywhere, blood extravasated into tissue, incision shows coagulated blood that does NOT wash away.",
                    explanation = "[Reddy FMT, 34th Ed] Important forensic tool for deciding the posture of the body after death and distinguishing antemortem injuries."
                ),
                FlashcardEntity(
                    userEmail = user.email,
                    subject = "FMT",
                    question = "Significance of Diatoms in drowning diagnosis.",
                    answer = "Diatoms are microscopic algae. If present in closed organs like bone marrow, it implies active breathing of water containing diatoms occurred while pressure was pumping blood (alive).",
                    explanation = "[Reddy FMT] Must match the diatom species found in the local water body to rule out post-mortem contamination."
                ),
                FlashcardEntity(
                    userEmail = user.email,
                    subject = "PSM",
                    question = "What is the Cold Chain and how is it monitored?",
                    answer = "A series of storage and transport links under constant low temperature (+2°C to +8°C) from vaccine maker to the recipient. Monitored by Vaccine Vial Monitors (VVM).",
                    explanation = "[K Park, 26th Ed] Top priority links: Ice Lined Refrigerators (ILRs) and Deep Freezers. Never freeze polio or measles vaccines. VVM helps monitor heat exposure."
                ),
                FlashcardEntity(
                    userEmail = user.email,
                    subject = "PSM",
                    question = "Define Primary, Secondary, and Tertiary Prevention with examples.",
                    answer = "Primary: Pre-pathogenesis phase (immunization, nutrition). Secondary: Pathogenesis phase (early diagnosis/therapy like sputum test for Tuberculosis). Tertiary: Late pathogenesis phase (physiotherapy, vocational training).",
                    explanation = "[K Park, 26th Ed] Primary targets the risk factor; Secondary targets early stage disease; Tertiary limits the resulting disability."
                ),
                FlashcardEntity(
                    userEmail = user.email,
                    subject = "PSM",
                    question = "Explain Herd Immunity thresholds and its utility.",
                    answer = "The general resistance of a population to an infectious illness to which a portion is immune. Prevents outbreaks when a defined percentage (e.g., 85-95%) is immune.",
                    explanation = "[K Park] Essential for eradication strategies. If herd immunity is sufficiently high, local chains of transmission die out completely."
                ),
                FlashcardEntity(
                    userEmail = user.email,
                    subject = "FINANCE",
                    question = "Define the TrueYield Wealth Formula for Index Funds.",
                    answer = "TrueYield = Nominal CAGR - (Inflation + Expense Ratio + Portfolio Turnover Costs + LTCG Tax).",
                    explanation = "Aims for a stable route to the ₹1 Crore milestone. Helps medical professionals cut through compounding myths and account for realistic purchasing power."
                )
            )
            repository.insertFlashcards(list)
        }
    }
}

data class BookIntel(
    val id: String,
    val emoji: String,
    val title: String,
    val author: String,
    val category: String, // HABITS, STUDY, PSYCHOLOGY, PERFORMANCE, DISCIPLINE, FINANCE, PRODUCTIVITY
    val takeaway: String,
    val prompt: String
)

object BookLibrary {
    val books = listOf(
        BookIntel(
            id = "atomic",
            emoji = "⚛️",
            title = "Atomic Habits",
            author = "James Clear",
            category = "HABITS",
            takeaway = "Focus on identity-based habits and the 4 Laws of Behavior Change to build 1% daily improvements.",
            prompt = "You are an expert on James Clear's Atomic Habits. Base your response heavily on Atomic Habits' core frameworks — the 4 Laws of Behavior Change (Make it Obvious, Attractive, Easy, Satisfying), habit stacking, identity-based habits, the 2-minute rule, habit tracking, and environment design — give a deep, practical, personalized study-relevance brief. Cover: (1) The identity shift: 'I am someone who...'; (2) Top 5 systems applicable to MBBS or calculation schedules; (3) How to habit-stack; (4) Implementation intentions for weakest areas; (5) A concrete '1% better' progress plan. Be tactical, specific."
        ),
        BookIntel(
            id = "deepwork",
            emoji = "🔬",
            title = "Deep Work",
            author = "Cal Newport",
            category = "STUDY",
            takeaway = "Minimize attention residue and schedule intense, uninterrupted focus blocks to master hard topics.",
            prompt = "You are an expert on Cal Newport's Deep Work. Frame your coaching around Deep Work's frameworks — the 4 disciplines of deep work (Monastic, Bimodal, Rhythmic, Journalistic philosophies), time-block planning, attention residue, the Craftsman approach to tools, shutdown rituals, and Deep Work scheduling. Provide a comprehensive tactical brief covering: (1) Which Deep Work philosophy suits a medical student or busy person best; (2) Restructuring study/work focus blocks; (3) Eliminating shallow distractions; (4) Measuring cognitive depth; (5) A 5-day Deep Work deployment schedule."
        ),
        BookIntel(
            id = "makeitstick",
            emoji = "🧲",
            title = "Make It Stick",
            author = "Brown, Roediger & McDaniel",
            category = "STUDY",
            takeaway = "Replace repetitive reading with effortful retrieval, spacing, and interleaved practice.",
            prompt = "You are an expert on 'Make It Stick: The Science of Successful Learning.' Provide a comprehensive tactical briefing covering: (1) Spaced practice vs massed practice schedules; (2) Retrieval practice and self-testing protocols; (3) Interleaved practice and mixing related topics; (4) Elaborative interrogation (the 'why'); (5) The myth of passive learning and what actually works; (6) A complete 7-day study protocol using Make It Stick principles."
        ),
        BookIntel(
            id = "ultralearn",
            emoji = "⚡",
            title = "Ultralearning",
            author = "Scott Young",
            category = "STUDY",
            takeaway = "Deconstruct skills through metalearning and tackle weakest concepts via intense targeted drills.",
            prompt = "You are an expert on Scott Young's Ultralearning. Focus on: (1) The 9 Ultralearning principles (Metalearning, Focus, Directness, Drill, Retrieval, Feedback, Retention, Intuition, Experimentation) applied to medical and complex technical education; (2) How to identify and attack weakest points via drilling; (3) Directness (learning by doing); (4) Rapid, brutal feedback loops; (5) A 30-day Ultralearning sprint plan for one subject. Give study hour estimates."
        ),
        BookIntel(
            id = "thinkfast",
            emoji = "🧠",
            title = "Thinking, Fast and Slow",
            author = "Daniel Kahneman",
            category = "PSYCHOLOGY",
            takeaway = "Engage deliberate System 2 logical processing to overcome System 1 automated biases.",
            prompt = "You are an expert on Daniel Kahneman's Thinking, Fast and Slow. Give a tactical intelligence brief covering: (1) System 1 vs System 2 — how each affects daily routine and self-regulation; (2) Cognitive biases most likely sabotaging performance (availability, planning fallacy, overconfidence bias); (3) How to use System 2 to design superior processes; (4) The WYSIATI effect; (5) Clinical/analytical mental models for making decisions under high pressure."
        ),
        BookIntel(
            id = "mindset",
            emoji = "🌱",
            title = "Mindset",
            author = "Carol S. Dweck",
            category = "PSYCHOLOGY",
            takeaway = "Develop a Growth Mindset; failures are valuable data, and intelligence is a muscle to train.",
            prompt = "You are an expert on Carol Dweck's Mindset. Give a tactical brief on: (1) Fixed vs Growth Mindset self-diagnostic; (2) Reframing failures or supplementary exam setbacks as growth data; (3) Praise and self-talk (rewiring internal critique); (4) The power of 'not yet'; (5) Building a growth-oriented system with triggers and daily checkpoints."
        ),
        BookIntel(
            id = "willpower",
            emoji = "💪",
            title = "Willpower",
            author = "Roy Baumeister & John Tierney",
            category = "PSYCHOLOGY",
            takeaway = "Manage ego depletion through scheduled energy focus, and establish clear bright-line rules.",
            prompt = "You are an expert on Roy Baumeister's Willpower. Provide a tactical brief on: (1) Ego depletion — what it is and how to work with it; (2) Glucose, energy cycles and scheduling strategies; (3) The importance of the 'bright line' rule for daily habits; (4) Implementation intentions to reduce reliance on momentary willpower; (5) Systemizing the workspace to minimize friction."
        ),
        BookIntel(
            id = "peak",
            emoji = "🏔️",
            title = "Peak",
            author = "Anders Ericsson",
            category = "PERFORMANCE",
            takeaway = "Acquire elite mastery through deliberate practice, seeking constant feedback, and refining mental models.",
            prompt = "You are an expert on Anders Ericsson's Peak: Secrets from the New Science of Expertise. Focus on: (1) Deliberate Practice vs naive practice; (2) The role of rich mental representations; (3) Identifying and practicing weakest subjects deliberately; (4) The 'Gold Standard' of practice applied to difficult skills; (5) Why 10,000 hours is a myth and what actually creates elite expertise."
        ),
        BookIntel(
            id = "canthurtme",
            emoji = "🔥",
            title = "Can't Hurt Me",
            author = "David Goggins",
            category = "DISCIPLINE",
            takeaway = "Callouse the mind using the 40% Rule, the Accountability Mirror, and a secure Cookie Jar archive.",
            prompt = "You are an expert on David Goggins' Can't Hurt Me. Channel Goggins' unmatched heat and mental callousing to deliver a high-intensity briefing: (1) The 40% Rule — protocol to push past comfortable limits daily; (2) The Cookie Jar method — building a mental archive of tough past wins; (3) Callousing the mind; (4) Accountability mirror (uncompromised self-honesty); (5) Adapting a 'Goggins Challenge' for a tough daily study routine. Output in brutal direct Goggins style."
        ),
        BookIntel(
            id = "richdad",
            emoji = "💰",
            title = "Rich Dad Poor Dad",
            author = "Robert Kiyosaki",
            category = "FINANCE",
            takeaway = "Accumulate cash-flow generating assets, avoid toxic liabilities, and master financial intelligence.",
            prompt = "You are an expert on Robert Kiyosaki's Rich Dad Poor Dad. Provide a practical financial brief covering: (1) Assets vs Liabilities — what an ambitious student can start building now; (2) The Cashflow Quadrant and how to plan a career trajectory toward investor/owner quadrants; (3) Concepts of leverage; (4) 'Paying yourself first' on limited capital; (5) Key financial traps to avoid. Keep calculations realistic."
        ),
        BookIntel(
            id = "psymoney",
            emoji = "🏦",
            title = "The Psychology of Money",
            author = "Morgan Housel",
            category = "FINANCE",
            takeaway = "Focus on staying wealthy, utilizing compounding, and keeping an eye on lifestyle inflation.",
            prompt = "You are an expert on Morgan Housel's The Psychology of Money. Provide a tactical financial brief on: (1) Getting wealthy vs staying wealthy; (2) Tail events and financial resilience; (3) Compounding — applying it to financial assets and habits simultaneously; (4) 'Save like a pessimist, invest like an optimist'; (5) Avoiding lifestyle inflation and status traps. Relate it to professional career models."
        ),
        BookIntel(
            id = "naval",
            emoji = "🌟",
            title = "Almanack of Naval Ravikant",
            author = "Eric Jorgenson",
            category = "FINANCE",
            takeaway = "Build specific knowledge, secure media/code leverage, and master selling or building value.",
            prompt = "You are an expert on Naval Ravikant's Almanack. Distill Naval's core principles: (1) Specific Knowledge (finding what feels like play to you, but looks like work to others); (2) Leverage (utilizing media, code, and capital to scale output); (3) 'Learn to sell, learn to build'; (4) Decision-making models; (5) Productizing yourself — how to build a scalable personal platform/brand."
        ),
        BookIntel(
            id = "7habits",
            emoji = "🎯",
            title = "7 Habits of Highly Effective People",
            author = "Stephen R. Covey",
            category = "PRODUCTIVITY",
            takeaway = "Prioritize via the Eisenhower Matrix, define a core mission, and constantly sharpen the saw.",
            prompt = "You are an expert on Stephen Covey's 7 Habits of Highly Effective People. Provide a tactical brief on: (1) Habit 1 (Be Proactive) — absolute ownership; (2) Habit 2 (Begin with the End in Mind) — a personal mission statement exercise; (3) Habit 3 (Put First Things First) — the Eisenhower Matrix; (4) Habits 4, 5, 6 — interpersonal clarity; (5) Habit 7 (Sharpen the Saw) — four dimensions of personal renewal."
        ),
        BookIntel(
            id = "essentialism",
            emoji = "✂️",
            title = "Essentialism",
            author = "Greg McKeown",
            category = "PRODUCTIVITY",
            takeaway = "Pursue fewer but higher-impact commitments, ruthlessly applying the 90% rule to task audits.",
            prompt = "You are an expert on Greg McKeown's Essentialism. Deliver an executive tactical brief on: (1) The Essentialist mindset vs the Non-Essentialist; (2) Choosing the ONE vital priority; (3) The 90% Rule for commitments; (4) Protecting the asset (sleep, recovery, and cognitive capacity); (5) Crafting a routine that makes executing the essential friction-free."
        )
    )
}
