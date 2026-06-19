package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.ChatScreen
import com.example.ui.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dataStoreManager = DataStoreManager(applicationContext)

        // ViewModel Factory for ChatViewModel
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ChatViewModel(dataStoreManager) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
        val chatViewModel: ChatViewModel by viewModels { factory }

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()

                val messages by chatViewModel.messages.collectAsState()
                
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = MaterialTheme.colorScheme.surface,
                            drawerContentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            HistoryScreen(
                                messages = messages,
                                onClose = { coroutineScope.launch { drawerState.close() } }
                            )
                        }
                    }
                ) {
                    NavHost(navController = navController, startDestination = "chat") {
                        composable("chat") {
                            val isLoading by chatViewModel.isLoading.collectAsState()
                            val activeModel by chatViewModel.activeModel.collectAsState()
                            val error by chatViewModel.errorMessage.collectAsState()

                            ChatScreen(
                                messages = messages,
                                isLoading = isLoading,
                                activeModel = activeModel,
                                onSendMessage = { text, image -> chatViewModel.sendMessage(text, image) },
                                onClearChat = { chatViewModel.clearMessages() },
                                onOpenSettings = { navController.navigate("settings") },
                                onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                                errorMessage = error,
                                onDismissError = { chatViewModel.dismissError() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        composable("settings") {
                            val activeProvider by chatViewModel.activeProvider.collectAsState()
                            val activeModel by chatViewModel.activeModel.collectAsState()

                            SettingsScreen(
                                dataStoreManager = dataStoreManager,
                                activeProvider = activeProvider,
                                activeModel = activeModel,
                                onBack = { navController.popBackStack() },
                                onProviderModelSelected = { p, m -> chatViewModel.setProviderAndModel(p, m) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(messages: List<Message>, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Cronologia", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        
        val userMessages = messages.filter { it.role == "user" }
        
        if (userMessages.isEmpty()) {
            Text("Nessuna conversazione recente.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn {
                items(userMessages.reversed()) { msg ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onClose)
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = msg.content,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}
