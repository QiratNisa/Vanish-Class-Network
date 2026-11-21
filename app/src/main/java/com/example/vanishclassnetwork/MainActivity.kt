
package com.example.vanishclassnetwork

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// THEME COLORS

private val Background = Color(0xFF151B22)
private val CardColor = Color(0xFF263440)
private val DustyBlue = Color(0xFF7897B0)
private val SoftMint = Color(0xFFA9CEB9)
private val MainText = Color(0xFFF1F4F6)
private val MutedText = Color(0xFFC2CDD5)
private val ChatBubble = Color(0xFF30483F)
private val DarkBlue = Color(0xFF202C37)

data class ChatMessage(
    val id: String,
    val sender: String,
    val text: String,
    val expiresAt: Long
)

// SAVE CHAT MESSAGES DURING ROTATION

private val ChatMessagesSaver = listSaver<List<ChatMessage>, String>(
    save = { messages ->
        messages.flatMap { message ->
            listOf(
                message.id,
                message.sender,
                message.text,
                message.expiresAt.toString()
            )
        }
    },
    restore = { saved ->
        saved.chunked(4).mapNotNull { item ->
            if (item.size == 4) {
                ChatMessage(
                    id = item[0],
                    sender = item[1],
                    text = item[2],
                    expiresAt = item[3].toLongOrNull() ?: 0L
                )
            } else {
                null
            }
        }
    }
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = DustyBlue,
                    secondary = SoftMint,
                    background = Background,
                    surface = CardColor,
                    onBackground = MainText,
                    onSurface = MainText
                )
            ) {

                // SAVE SCREEN STATE

                var currentScreen by rememberSaveable {
                    mutableStateOf("welcome")
                }

                var studentName by rememberSaveable {
                    mutableStateOf("")
                }

                var deviceId by rememberSaveable {
                    mutableStateOf("")
                }

                when (currentScreen) {

                    "welcome" -> WelcomeScreen {
                        currentScreen = "join"
                    }

                    "join" -> JoinScreen { name, id ->
                        studentName = name
                        deviceId = id
                        currentScreen = "chat"
                    }

                    "chat" -> ChatScreen(
                        studentName = studentName,
                        deviceId = deviceId
                    )
                }
            }
        }
    }
}

// WELCOME SCREEN

@Composable
fun WelcomeScreen(onStartClick: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Background,
                        DarkBlue,
                        Color(0xFF344B5D)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(115.dp)
                    .background(
                        CardColor,
                        RoundedCornerShape(32.dp)
                    )
                    .border(
                        1.dp,
                        DustyBlue.copy(alpha = 0.5f),
                        RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("💬", fontSize = 58.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "VANISH",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MainText,
                letterSpacing = 6.sp
            )

            Text(
                "CLASS NETWORK",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SoftMint,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                "Your messages.\nYour moment. Gone in 30 seconds.",
                fontSize = 16.sp,
                lineHeight = 26.sp,
                textAlign = TextAlign.Center,
                color = MutedText
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftMint,
                    contentColor = Background
                )
            ) {
                Text(
                    "GET STARTED  →",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                "PRIVATE  •  SIMPLE  •  TEMPORARY",
                color = MutedText,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

// JOIN CLASS SCREEN

@Composable
fun JoinScreen(
    onJoinClick: (String, String) -> Unit
) {

    // SAVE JOIN DETAILS DURING ROTATION

    var studentName by rememberSaveable {
        mutableStateOf("")
    }

    var deviceId by rememberSaveable {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("💬", fontSize = 48.sp)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Join Your Class",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = MainText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Connect with your classmates\nin one shared space.",
            fontSize = 14.sp,
            color = MutedText,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(23.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardColor
            ),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    "Your Details",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftMint
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "STUDENT NAME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DustyBlue
                )

                Spacer(modifier = Modifier.height(7.dp))

                OutlinedTextField(
                    value = studentName,
                    onValueChange = {
                        studentName = it
                    },
                    placeholder = {
                        Text("Enter your name")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftMint,
                        unfocusedBorderColor = DustyBlue,
                        focusedTextColor = MainText,
                        unfocusedTextColor = MainText,
                        focusedPlaceholderColor = MutedText,
                        unfocusedPlaceholderColor = MutedText,
                        cursorColor = SoftMint
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    "DEVICE ID",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DustyBlue
                )

                Spacer(modifier = Modifier.height(7.dp))

                OutlinedTextField(
                    value = deviceId,
                    onValueChange = {
                        deviceId = it
                    },
                    placeholder = {
                        Text("Enter your device ID")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(13.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftMint,
                        unfocusedBorderColor = DustyBlue,
                        focusedTextColor = MainText,
                        unfocusedTextColor = MainText,
                        focusedPlaceholderColor = MutedText,
                        unfocusedPlaceholderColor = MutedText,
                        cursorColor = SoftMint
                    )
                )

                Spacer(modifier = Modifier.height(25.dp))

                Button(
                    onClick = {
                        if (
                            studentName.isNotBlank() &&
                            deviceId.isNotBlank()
                        ) {
                            onJoinClick(
                                studentName.trim(),
                                deviceId.trim()
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftMint,
                        contentColor = Background
                    )
                ) {
                    Text(
                        "JOIN CLASS  →",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            "Messages disappear after 30 seconds",
            color = SoftMint,
            fontSize = 12.sp
        )
    }
}

// CHAT SCREEN

@Composable
fun ChatScreen(
    studentName: String,
    deviceId: String
) {

    var messageText by rememberSaveable {
        mutableStateOf("")
    }

    // RESTORE MESSAGES AFTER ROTATION

    var messages by rememberSaveable(
        stateSaver = ChatMessagesSaver
    ) {
        mutableStateOf(listOf<ChatMessage>())
    }

    var now by remember {
        mutableLongStateOf(System.currentTimeMillis())
    }

    var connectionStatus by remember {
        mutableStateOf("Connecting...")
    }

    val repository = remember {
        SyncRepository(
            onMessageReceived = { message ->

                // AVOID ADDING SAME MESSAGE TWICE

                if (messages.none { it.id == message.id }) {
                    messages = messages + message
                }
            },

            onMessageExpired = { id ->
                messages = messages.filterNot {
                    it.id == id
                }
            },

            onStatusChanged = { status ->
                connectionStatus = status
            }
        )
    }

    // CONNECT TO RELAY

    LaunchedEffect(repository, studentName, deviceId) {
        repository.connect(studentName, deviceId)
    }

    // DISCONNECT WHEN CHAT SCREEN IS REMOVED

    DisposableEffect(repository) {
        onDispose {
            repository.disconnect()
        }
    }

    // UPDATE COUNTDOWN AND REMOVE EXPIRED MESSAGES

    LaunchedEffect(Unit) {
        while (true) {

            now = System.currentTimeMillis()

            messages = messages.filter {
                it.expiresAt > now
            }

            delay(250)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {

        // CHAT HEADER

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(DarkBlue, CardColor)
                    )
                )
                .padding(horizontal = 17.dp, vertical = 18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            DustyBlue.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💬", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(11.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        "Vanish Class",
                        color = MainText,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Text(
                        "Class Group Chat",
                        color = MutedText,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                val statusLabel = when {
                    connectionStatus.equals(
                        "Connected",
                        ignoreCase = true
                    ) -> "● Connected"

                    connectionStatus.contains(
                        "Connecting",
                        ignoreCase = true
                    ) -> "● Connecting"

                    else -> "● Reconnecting"
                }

                val statusColor = when {
                    connectionStatus.equals(
                        "Connected",
                        ignoreCase = true
                    ) -> SoftMint

                    connectionStatus.contains(
                        "Connecting",
                        ignoreCase = true
                    ) -> DustyBlue

                    else -> Color(0xFFFFD28A)
                }

                Box(
                    modifier = Modifier
                        .widthIn(max = 125.dp)
                        .background(
                            CardColor,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(
                            horizontal = 9.dp,
                            vertical = 7.dp
                        )
                ) {

                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(17.dp))

            Text(
                "Welcome, $studentName!",
                color = MainText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                "Device ID: $deviceId",
                color = MutedText,
                fontSize = 11.sp
            )
        }

        // EXPIRY NOTICE

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 14.dp,
                    vertical = 11.dp
                )
                .background(
                    CardColor,
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text("⏳", fontSize = 20.sp)

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                "Every message disappears after 30 seconds.",
                modifier = Modifier.weight(1f),
                color = SoftMint,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // MESSAGES

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {

            if (messages.isEmpty()) {

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 65.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text("✉️", fontSize = 50.sp)

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            "No messages yet",
                            fontSize = 18.sp,
                            color = MainText,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            "Start the conversation!",
                            color = MutedText,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { message ->

                val remaining = (
                        message.expiresAt - now
                        ).coerceAtLeast(0L) / 1000

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {

                    Column(
                        modifier = Modifier
                            .widthIn(max = 290.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        ChatBubble,
                                        CardColor
                                    )
                                ),
                                RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = 18.dp,
                                    bottomEnd = 5.dp
                                )
                            )
                            .padding(15.dp)
                    ) {

                        Text(
                            message.sender,
                            color = SoftMint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(7.dp))

                        Text(
                            message.text,
                            color = MainText,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.15f)
                        )

                        Spacer(modifier = Modifier.height(7.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text("⏳", fontSize = 12.sp)

                            Spacer(modifier = Modifier.width(5.dp))

                            Text(
                                "$remaining seconds remaining",
                                color = MutedText,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // MESSAGE INPUT

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardColor)
                .padding(11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = messageText,
                onValueChange = {
                    if (it.length <= 500) {
                        messageText = it
                    }
                },
                placeholder = {
                    Text("Write a message...")
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SoftMint,
                    unfocusedBorderColor = DustyBlue,
                    focusedTextColor = MainText,
                    unfocusedTextColor = MainText,
                    focusedPlaceholderColor = MutedText,
                    unfocusedPlaceholderColor = MutedText,
                    cursorColor = SoftMint
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // SEND BUTTON - ONLINE AND OFFLINE

            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        repository.sendMessage(messageText)
                        messageText = ""
                    }
                },
                enabled = messageText.isNotBlank(),
                modifier = Modifier.height(53.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftMint,
                    contentColor = Background,
                    disabledContainerColor = DustyBlue.copy(alpha = 0.3f),
                    disabledContentColor = MutedText
                )
            ) {
                Text(
                    "SEND",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
