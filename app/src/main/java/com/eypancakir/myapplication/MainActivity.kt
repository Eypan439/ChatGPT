package com.eypancakir.myapplication

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eypancakir.myapplication.ui.theme.MyApplicationTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

class MainActivity : ComponentActivity() {

    private lateinit var database: DatabaseReference
    private var messageText by rememberSaveable { mutableStateOf("") }
    private var messages by mutableStateOf(listOf<Message>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase.getInstance().reference

        database.child("messages").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newMessages = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                messages = newMessages
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ChatAppBar()
                }
            }
        }
    }
}

@Composable
fun ChatAppBar() {
    Surface(
        color = MaterialTheme.colors.primary,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                top = 8.dp,
                bottom = 8.dp
            )
        ) {
            Text(
                text = "Chat App",
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onPrimary
            )
        }
    }
}

@Composable
fun ChatMessages(messages: List<String>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(messages) { message ->
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.primaryVariant,
                elevation = 4.dp
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colors.onPrimary
                )
            }
        }
    }
}

@Composable
fun ChatInput(onSendClicked: (String) -> Unit) {
    var message by remember { mutableStateOf("") }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.background,
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.EmojiEmotions,
                    contentDescription = "Emoji Icon",
                    tint = MaterialTheme.colors.primaryVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = "Enter your message") },
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = MaterialTheme.colors.primaryVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.onPrimary)
            )

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(
                onClick = {
                    onSendClicked(message)
                    message = ""
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Send,
                    contentDescription = "Send Icon",
                    tint = MaterialTheme.colors.primaryVariant
                )
            }
        }
    }
}

@Composable
fun ChatScreen(messages: List<String>, onSendClicked: (String) -> Unit) {
    Scaffold(
        topBar = { ChatAppBar() },
        content = { ChatMessages(messages) },
        bottomBar = { ChatInput(onSendClicked) }
    )
}

@Composable
fun MessageCard(message: Message) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_baseline_account_circle_24),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = message.username,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = message.text)
        }
    }
}

fun sendMessage(message: String) {
    val key = database.child("messages").push().key ?: return
    val timestamp = System.currentTimeMillis()
    val messageData = Message(message, timestamp)
    database.child("messages").child(key).setValue(messageData)
}

data class Message(val text: String, val timestamp: Long) {
    constructor() : this("", 0) // Firebase Realtime Database tarafından gereklidir
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        ChatScreen()
    }
}