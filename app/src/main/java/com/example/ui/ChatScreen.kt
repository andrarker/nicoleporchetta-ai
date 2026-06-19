package com.example.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Message
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<Message>,
    isLoading: Boolean,
    activeModel: String,
    onSendMessage: (String, String?) -> Unit,
    onClearChat: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDrawer: () -> Unit,
    errorMessage: String?,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var attachedUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) attachedUri = uri
    }
    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) attachedUri = uri
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0F))
    ) {
        // Glow viola in alto a sinistra
        Box(
            modifier = Modifier
                .size(500.dp)
                .offset(x = (-100).dp, y = (-80).dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFC084FC).copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
        )
        // Glow rosa in basso a destra
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 60.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF472B6).copy(alpha = 0.13f),
                            Color.Transparent
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = Color(0xFF0A0A0F),
                    modifier = Modifier.fillMaxWidth().height(64.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Logo Box
                            Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.profile),
                                contentDescription = "Logo App e IA",
                                modifier = Modifier
                                    .clickable { onOpenDrawer() }
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(12.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "NicolePorchetta AI",
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(Color(0xFFC084FC), Color(0xFFF1F0FF))
                                    )
                                )
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Model chip
                            Box(
                                modifier = Modifier
                                    .clickable { onOpenSettings() }
                                    .background(Color(0xFF1A1A24), RoundedCornerShape(20.dp))
                                    .border(1.dp, Color(0xFFC084FC).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = activeModel,
                                    color = Color(0xFFD49BFF),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(onClick = onClearChat) {
                                Icon(Icons.Default.Create, contentDescription = "Nuova Chat", tint = Color(0xFFF1F0FF))
                            }
                            IconButton(onClick = onOpenSettings) {
                                Icon(Icons.Default.Settings, contentDescription = "Impostazioni", tint = Color(0xFFF1F0FF))
                            }
                        }
                        
                        androidx.compose.material3.HorizontalDivider(
                            color = Color(0xFFC084FC).copy(alpha = 0.2f),
                            thickness = 1.dp,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0D0D14),
                shadowElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(
                    start = 16.dp, end = 16.dp,
                    top = 12.dp, bottom = 20.dp
                )) {
                    if (attachedUri != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("Allegato selezionato", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { attachedUri = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Rimuovi", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Bottoni icona
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1A1A24))
                                .clickable { getContent.launch("*/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📎", fontSize = 18.sp)
                        }
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1A1A24))
                                .clickable { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🖼", fontSize = 18.sp)
                        }
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1A1A24))
                                .clickable { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎬", fontSize = 18.sp)
                        }

                        // TextField
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF1A1A24))
                                .border(1.5.dp, Color(0xFFC084FC).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            BasicTextField(
                                value = inputText, 
                                onValueChange = { inputText = it },
                                textStyle = TextStyle(color = Color(0xFFF1F0FF), fontSize = 16.sp),
                                decorationBox = { inner ->
                                    if (inputText.isEmpty()) Text("dimmi qualcosa...", color = Color(0xFF8B8A9E), fontSize = 16.sp)
                                    inner()
                                }
                            )
                        }

                        // Bottone invio
                        val canSend = inputText.isNotBlank() || attachedUri != null
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        listOf(Color(0xFFC084FC), Color(0xFFF472B6))
                                    )
                                )
                                .clickable { 
                                    if (canSend) {
                                        onSendMessage(inputText, attachedUri?.toString())
                                        inputText = ""
                                        attachedUri = null
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                androidx.compose.material.icons.Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = {
            if (errorMessage != null) {
                Snackbar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.secondary,
                    action = {
                        TextButton(onClick = onDismissError) {
                            Text("OK", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(errorMessage)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (messages.isEmpty() && !isLoading) {
                WelcomeCard(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(messages) { message ->
                        MessageBubble(message, timeFormatter)
                    }
                    if (isLoading) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun WelcomeCard(modifier: Modifier = Modifier) {
    Box(
      modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .clip(RoundedCornerShape(28.dp))
        .background(
          brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color(0xFF1A1A24), Color(0xFF111118))
          )
        )
        .border(
          width = 1.dp,
          brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color(0xFFC084FC), Color(0xFFF472B6))
          ),
          shape = RoundedCornerShape(28.dp)
        )
        .padding(36.dp)
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Immagine profilo grande
        Image(
          painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.profile),
          contentDescription = "NicolePorchetta AI",
          modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .border(2.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFC084FC), Color(0xFFF472B6))), CircleShape),
          contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        Spacer(Modifier.height(20.dp))
        Text(
          "ciao.",
          fontSize = 42.sp,
          fontWeight = FontWeight.ExtraBold,
          style = TextStyle(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
              colors = listOf(Color(0xFFC084FC), Color(0xFFF472B6))
            )
          )
        )
        Spacer(Modifier.height(8.dp))
        Text("sono Nicole.", fontSize = 20.sp,
          fontWeight = FontWeight.SemiBold, color = Color(0xFFF1F0FF))
        Spacer(Modifier.height(12.dp))
        Text(
          "dimmi cosa vuoi — testi, immagini, video, codice, whatever.\nprima metti le chiavi API nelle ⚙️ impostazioni.",
          fontSize = 14.sp, color = Color(0xFF8B8A9E),
          textAlign = TextAlign.Center, lineHeight = 22.sp
        )
      }
    }
}

@Composable
fun MessageBubble(message: Message, timeFormatter: SimpleDateFormat) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        if (isUser) {
            Surface(
                shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp, topEnd = 4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (message.imageUrl != null) {
                        AsyncImage(
                            model = message.imageUrl,
                            contentDescription = "Immagine allegata",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .padding(bottom = 8.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    if (message.content.isNotBlank()) {
                        Text(message.content, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
                    }
                    Text(
                        text = timeFormatter.format(Date(message.timestamp)),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                    )
                }
            }
        } else {
            Row(modifier = Modifier.widthIn(max = 300.dp)) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.profile),
                    contentDescription = "Avatar IA",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .align(Alignment.Bottom),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Surface(
                    shape = RoundedCornerShape(topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp, topStart = 4.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (message.content.startsWith("http://") || message.content.startsWith("https://")) {
                            AsyncImage(
                                model = message.content,
                                contentDescription = "Immagine Generata",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 250.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(bottom = 8.dp),
                                contentScale = androidx.compose.ui.layout.ContentScale.FillWidth
                            )
                        } else {
                            Text(message.content, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge)
                        }
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            Text(
                                text = timeFormatter.format(Date(message.timestamp)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            if (message.model.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "· ${message.model}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "typing")
        for (i in 0 until 3) {
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = i * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha$i"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha), CircleShape)
            )
        }
    }
}
