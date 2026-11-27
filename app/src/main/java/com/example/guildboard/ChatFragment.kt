package com.example.guildboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatFragment : Fragment() {

    private lateinit var layoutChatLocked: LinearLayout
    private lateinit var layoutChatContent: LinearLayout
    private lateinit var btnLoginForChat: Button

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessageInput: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var tvStatus: TextView
    private lateinit var adapter: ChatAdapter
    private lateinit var dbHelper: GuildBoardDbHelper

    private var messagesList: MutableList<ChatMessage> = mutableListOf()
    private val CURRENT_EVENT_ID = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        dbHelper = (activity as? MainActivity)?.getDbHelper() ?: GuildBoardDbHelper(requireContext())

        layoutChatLocked = view.findViewById(R.id.layoutChatLocked)
        layoutChatContent = view.findViewById(R.id.layoutChatContent)
        btnLoginForChat = view.findViewById(R.id.btnLoginForChat)

        rvMessages = view.findViewById(R.id.rvChatMessages)
        etMessageInput = view.findViewById(R.id.etMessageInput)
        btnSend = view.findViewById(R.id.btnSendMessage)
        tvStatus = view.findViewById(R.id.tvWsStatus)

        btnLoginForChat.setOnClickListener {
            (activity as? MainActivity)?.showLoginDialog()
        }

        checkLoginState()
        loadMessagesFromDb()
        setupRecyclerView()
        setupSendButton()

        return view
    }

    override fun onResume() {
        super.onResume()
        checkLoginState()
    }

    private fun checkLoginState() {
        val mainActivity = activity as? MainActivity ?: return
        if (mainActivity.isUserLoggedIn()) {
            layoutChatLocked.visibility = View.GONE
            layoutChatContent.visibility = View.VISIBLE
        } else {
            layoutChatContent.visibility = View.GONE
            layoutChatLocked.visibility = View.VISIBLE
        }
    }

    private fun loadMessagesFromDb() {
        messagesList = dbHelper.getChatMessages(eventId = CURRENT_EVENT_ID).toMutableList()
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messagesList)
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        rvMessages.layoutManager = layoutManager
        rvMessages.adapter = adapter
    }

    private fun setupSendButton() {
        btnSend.setOnClickListener {
            val text = etMessageInput.text.toString().trim()
            val currentUser = (activity as? MainActivity)?.getCurrentUser()
            
            if (currentUser != null && text.isNotEmpty()) {
                val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val newMessage = ChatMessage(currentUser.nickname, timeNow, text, false)

                dbHelper.addChatMessage(CURRENT_EVENT_ID, currentUser.id, text)
                adapter.addMessage(newMessage)

                etMessageInput.setText("")
                rvMessages.smoothScrollToPosition(adapter.itemCount - 1)
            } else if (text.isEmpty()) {
                Toast.makeText(requireContext(), "Wpisz wiadomość...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Zaloguj się aby wysłać", Toast.LENGTH_SHORT).show()
            }
        }
    }
}