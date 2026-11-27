package com.example.guildboard

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GuildsFragment : Fragment() {

    // Widok 1: Przeglądanie
    private lateinit var layoutBrowseGuilds: LinearLayout
    private lateinit var btnCreateGuild: Button
    private lateinit var etSearch: EditText
    private lateinit var rvGuilds: RecyclerView
    private lateinit var guildAdapter: GuildAdapter

    // Widok 2: Strona Mojej Gildii
    private lateinit var layoutMyGuild: LinearLayout
    private lateinit var tvMyGuildName: TextView
    private lateinit var tvMyGuildRoleAndMeta: TextView
    private lateinit var btnBrowseAllGuilds: Button
    private lateinit var btnLeaveGuild: Button

    // Chipy sekcji Mojej Gildii
    private lateinit var chipGuildChat: TextView
    private lateinit var chipGuildMembers: TextView
    private lateinit var chipGuildGames: TextView
    private lateinit var chipGuildManage: TextView

    // Sekcje treści Mojej Gildii
    private lateinit var secGuildChat: LinearLayout
    private lateinit var secGuildMembers: LinearLayout
    private lateinit var secGuildGames: LinearLayout
    private lateinit var secGuildManage: LinearLayout

    // Czat Gildii
    private lateinit var rvGuildChatMessages: RecyclerView
    private lateinit var etGuildMessageInput: EditText
    private lateinit var btnSendGuildMessage: ImageButton
    private lateinit var guildChatAdapter: ChatAdapter

    // Członkowie Gildii
    private lateinit var rvMyGuildMembers: RecyclerView
    private lateinit var myGuildMembersAdapter: GuildMemberAdapter

    // Gry Gildii
    private lateinit var etAddGuildGame: EditText
    private lateinit var btnAddGuildGame: Button
    private lateinit var rvGuildGames: RecyclerView
    private lateinit var guildGameAdapter: GuildGameAdapter

    // Zarządzanie & Prośby
    private lateinit var rvPendingMembers: RecyclerView
    private lateinit var rvManageMembers: RecyclerView
    private lateinit var pendingAdapter: GuildMemberAdapter
    private lateinit var manageAdapter: GuildMemberAdapter

    private lateinit var dbHelper: GuildBoardDbHelper
    private var allGuilds: MutableList<Guild> = mutableListOf()
    private var myGuild: Guild? = null
    private var isViewingBrowseMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_guilds, container, false)
        dbHelper = (activity as? MainActivity)?.getDbHelper() ?: GuildBoardDbHelper(requireContext())

        layoutBrowseGuilds = view.findViewById(R.id.layoutBrowseGuilds)
        btnCreateGuild = view.findViewById(R.id.btnCreateGuild)
        etSearch = view.findViewById(R.id.etSearchGuild)
        rvGuilds = view.findViewById(R.id.rvGuilds)

        layoutMyGuild = view.findViewById(R.id.layoutMyGuild)
        tvMyGuildName = view.findViewById(R.id.tvMyGuildName)
        tvMyGuildRoleAndMeta = view.findViewById(R.id.tvMyGuildRoleAndMeta)
        btnBrowseAllGuilds = view.findViewById(R.id.btnBrowseAllGuilds)
        btnLeaveGuild = view.findViewById(R.id.btnLeaveGuild)

        chipGuildChat = view.findViewById(R.id.chipGuildChat)
        chipGuildMembers = view.findViewById(R.id.chipGuildMembers)
        chipGuildGames = view.findViewById(R.id.chipGuildGames)
        chipGuildManage = view.findViewById(R.id.chipGuildManage)

        secGuildChat = view.findViewById(R.id.secGuildChat)
        secGuildMembers = view.findViewById(R.id.secGuildMembers)
        secGuildGames = view.findViewById(R.id.secGuildGames)
        secGuildManage = view.findViewById(R.id.secGuildManage)

        rvGuildChatMessages = view.findViewById(R.id.rvGuildChatMessages)
        etGuildMessageInput = view.findViewById(R.id.etGuildMessageInput)
        btnSendGuildMessage = view.findViewById(R.id.btnSendGuildMessage)

        rvMyGuildMembers = view.findViewById(R.id.rvMyGuildMembers)
        etAddGuildGame = view.findViewById(R.id.etAddGuildGame)
        btnAddGuildGame = view.findViewById(R.id.btnAddGuildGame)
        rvGuildGames = view.findViewById(R.id.rvGuildGames)

        rvPendingMembers = view.findViewById(R.id.rvPendingMembers)
        rvManageMembers = view.findViewById(R.id.rvManageMembers)

        setupListeners()
        setupSearch()

        return view
    }

    override fun onResume() {
        super.onResume()
        checkUserGuildState()
    }

    private fun checkUserGuildState() {
        val mainActivity = activity as? MainActivity ?: return
        val currentUser = mainActivity.getCurrentUser()

        loadGuilds()

        if (currentUser != null) {
            myGuild = dbHelper.getUserGuild(currentUser.id)
            if (myGuild != null && !isViewingBrowseMode) {
                showMyGuildPage(myGuild!!, currentUser)
                return
            }
        } else {
            myGuild = null
        }

        layoutMyGuild.visibility = View.GONE
        layoutBrowseGuilds.visibility = View.VISIBLE
    }

    fun loadGuilds() {
        allGuilds = dbHelper.getAllGuilds().toMutableList()
        if (::guildAdapter.isInitialized) {
            guildAdapter.updateGuilds(allGuilds)
        } else {
            setupBrowseRecyclerView()
        }
    }

    private fun showMyGuildPage(guild: Guild, currentUser: User) {
        layoutBrowseGuilds.visibility = View.GONE
        layoutMyGuild.visibility = View.VISIBLE

        val role = dbHelper.getUserGuildRole(guild.id, currentUser.id) ?: "MEMBER"
        val roleLabel = when (role) {
            "OWNER" -> "Właściciel"
            "MODERATOR" -> "Moderator"
            else -> "Członek"
        }

        tvMyGuildName.text = guild.name
        tvMyGuildRoleAndMeta.text = "Twoja rola: $roleLabel | ${guild.city} | ${guild.memberCount} Członków"

        if (role == "OWNER" || role == "MODERATOR") {
            chipGuildManage.visibility = View.VISIBLE
        } else {
            chipGuildManage.visibility = View.GONE
        }

        if (role == "OWNER") {
            btnLeaveGuild.text = "Rozwiąż gildię"
        } else {
            btnLeaveGuild.text = "Opuść gildię"
        }

        setupMyGuildSubSections(guild, currentUser, role)
    }

    private fun setupMyGuildSubSections(guild: Guild, currentUser: User, userRole: String) {
        val chatMessages = dbHelper.getGuildChatMessages(guild.id).toMutableList()
        guildChatAdapter = ChatAdapter(chatMessages)
        val chatLayoutManager = LinearLayoutManager(requireContext())
        chatLayoutManager.stackFromEnd = true
        rvGuildChatMessages.layoutManager = chatLayoutManager
        rvGuildChatMessages.adapter = guildChatAdapter

        btnSendGuildMessage.setOnClickListener {
            val text = etGuildMessageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val newMsg = ChatMessage(currentUser.nickname, timeNow, text, false)
                dbHelper.addGuildChatMessage(guild.id, currentUser.id, text)
                guildChatAdapter.addMessage(newMsg)
                etGuildMessageInput.setText("")
                rvGuildChatMessages.smoothScrollToPosition(guildChatAdapter.itemCount - 1)
            }
        }

        val members = dbHelper.getGuildMembers(guild.id)
        val acceptedMembers = members.filter { it.status == "ACTIVE" }

        myGuildMembersAdapter = GuildMemberAdapter(acceptedMembers, userRole, currentUser.id) { member, view ->
            showMemberActionMenu(guild, member, view, userRole, currentUser)
        }
        rvMyGuildMembers.layoutManager = LinearLayoutManager(requireContext())
        rvMyGuildMembers.adapter = myGuildMembersAdapter

        val games = dbHelper.getGuildGames(guild.id).toMutableList()
        guildGameAdapter = GuildGameAdapter(games)
        rvGuildGames.layoutManager = LinearLayoutManager(requireContext())
        rvGuildGames.adapter = guildGameAdapter

        btnAddGuildGame.setOnClickListener {
            val gameTitle = etAddGuildGame.text.toString().trim()
            if (gameTitle.isNotEmpty()) {
                val newGameId = dbHelper.addGuildGame(guild.id, gameTitle)
                if (newGameId > 0) {
                    guildGameAdapter.addGame(GuildGame(newGameId, guild.id, gameTitle, "Planszówki"))
                    etAddGuildGame.setText("")
                    Toast.makeText(requireContext(), "Dodano grę: $gameTitle", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val isOwnerOrMod = userRole == "OWNER" || userRole == "MODERATOR"
        if (isOwnerOrMod) {
            val pendingMembers = members.filter { it.status == "PENDING" }
            pendingAdapter = GuildMemberAdapter(pendingMembers, userRole, currentUser.id) { member, view ->
                showMemberActionMenu(guild, member, view, userRole, currentUser)
            }
            rvPendingMembers.layoutManager = LinearLayoutManager(requireContext())
            rvPendingMembers.adapter = pendingAdapter

            manageAdapter = GuildMemberAdapter(acceptedMembers, userRole, currentUser.id) { member, view ->
                showMemberActionMenu(guild, member, view, userRole, currentUser)
            }
            rvManageMembers.layoutManager = LinearLayoutManager(requireContext())
            rvManageMembers.adapter = manageAdapter
        }

        setupSubSectionTabs()
    }

    private fun setupSubSectionTabs() {
        val chips = listOf(chipGuildChat, chipGuildMembers, chipGuildGames, chipGuildManage)
        val sections = listOf(secGuildChat, secGuildMembers, secGuildGames, secGuildManage)

        chips.forEachIndexed { index, chip ->
            chip.setOnClickListener {
                chips.forEachIndexed { i, c ->
                    if (i == index) {
                        c.setBackgroundResource(R.drawable.bg_chip)
                        c.backgroundTintList = android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), R.color.accent_gold)
                        )
                        c.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_dark))
                        sections[i].visibility = View.VISIBLE
                    } else {
                        c.setBackgroundResource(R.drawable.bg_chip)
                        c.backgroundTintList = null
                        c.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_white))
                        sections[i].visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showMemberActionMenu(guild: Guild, member: GuildMember, anchorView: View, currentUserRole: String, currentUser: User) {
        val wrapper = ContextThemeWrapper(requireContext(), R.style.CustomPopupMenuTheme)
        val popup = PopupMenu(wrapper, anchorView)

        if (member.status == "PENDING") {
            popup.menu.add("Zaakceptuj prośbę o dołączenie")
            popup.menu.add("Odrzuć prośbę")
        } else {
            if (currentUserRole == "OWNER") {
                if (member.role == "MEMBER") {
                    popup.menu.add("Nadaj rolę Moderatora")
                    popup.menu.add("Przekaż rolę Właściciela")
                } else if (member.role == "MODERATOR") {
                    popup.menu.add("Odbierz rolę Moderatora")
                    popup.menu.add("Przekaż rolę Właściciela")
                }
                popup.menu.add("Usuń z gildii")
            } else if (currentUserRole == "MODERATOR") {
                if (member.role == "MEMBER") {
                    popup.menu.add("Usuń z gildii")
                }
            }
        }

        for (i in 0 until popup.menu.size()) {
            val item = popup.menu.getItem(i)
            val span = SpannableString(item.title)
            span.setSpan(ForegroundColorSpan(Color.WHITE), 0, span.length, 0)
            item.title = span
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.title.toString()) {
                "Zaakceptuj prośbę o dołączenie" -> {
                    dbHelper.updateGuildMemberStatus(guild.id, member.userId, "active")
                    Toast.makeText(requireContext(), "Zaakceptowano użytkownika ${member.userNick}", Toast.LENGTH_SHORT).show()
                    checkUserGuildState()
                    true
                }
                "Odrzuć prośbę", "Usuń z gildii" -> {
                    dbHelper.removeGuildMember(guild.id, member.userId)
                    Toast.makeText(requireContext(), "Usunięto użytkownika ${member.userNick}", Toast.LENGTH_SHORT).show()
                    checkUserGuildState()
                    true
                }
                "Nadaj rolę Moderatora" -> {
                    dbHelper.updateGuildMemberRole(guild.id, member.userId, "moderator")
                    Toast.makeText(requireContext(), "Nadano rolę Moderatora dla ${member.userNick}", Toast.LENGTH_SHORT).show()
                    checkUserGuildState()
                    true
                }
                "Odbierz rolę Moderatora" -> {
                    dbHelper.updateGuildMemberRole(guild.id, member.userId, "member")
                    Toast.makeText(requireContext(), "Odebrano rolę Moderatora dla ${member.userNick}", Toast.LENGTH_SHORT).show()
                    checkUserGuildState()
                    true
                }
                "Przekaż rolę Właściciela" -> {
                    val success = dbHelper.transferGuildOwnership(
                        guildId = guild.id,
                        newOwnerUserId = member.userId
                    )
                    if (success) {
                        Toast.makeText(requireContext(), "Przekazano rolę Właściciela dla ${member.userNick}. Jesteś teraz Członkiem.", Toast.LENGTH_LONG).show()
                        checkUserGuildState()
                    } else {
                        Toast.makeText(requireContext(), "Błąd przekazywania własności.", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun setupListeners() {
        btnBrowseAllGuilds.setOnClickListener {
            isViewingBrowseMode = true
            layoutMyGuild.visibility = View.GONE
            layoutBrowseGuilds.visibility = View.VISIBLE
        }

        btnLeaveGuild.setOnClickListener {
            val currentUser = (activity as? MainActivity)?.getCurrentUser() ?: return@setOnClickListener
            if (myGuild != null) {
                val success = dbHelper.leaveOrDisbandGuild(myGuild!!.id, currentUser.id)
                if (success) {
                    Toast.makeText(requireContext(), "Gildia została opuszczona/rozwiązana.", Toast.LENGTH_SHORT).show()
                    myGuild = null
                    isViewingBrowseMode = false
                    checkUserGuildState()
                }
            }
        }

        btnCreateGuild.setOnClickListener {
            val mainActivity = activity as? MainActivity
            val currentUser = mainActivity?.getCurrentUser()

            if (currentUser == null) {
                Toast.makeText(requireContext(), "Musisz się zalogować, aby stworzyć własną gildię!", Toast.LENGTH_SHORT).show()
                mainActivity?.showLoginDialog()
                return@setOnClickListener
            }

            if (dbHelper.isUserInAnyGuild(currentUser.id)) {
                val userGuild = dbHelper.getUserGuild(currentUser.id)
                Toast.makeText(requireContext(), "Należysz już do gildii \"${userGuild?.name}\"! Możesz należeć tylko do 1 gildii.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val dialog = CreateGuildDialogFragment.newInstance()
            dialog.show(childFragmentManager, CreateGuildDialogFragment.TAG)
        }
    }

    private fun setupBrowseRecyclerView() {
        guildAdapter = GuildAdapter(
            guilds = allGuilds,
            onDetailsClick = { guild ->
                val mainActivity = activity as? MainActivity
                if (mainActivity?.isUserLoggedIn() == true) {
                    val dialog = GuildDetailsDialogFragment.newInstance(guild)
                    dialog.show(childFragmentManager, GuildDetailsDialogFragment.TAG)
                } else {
                    Toast.makeText(requireContext(), "Zaloguj się lub załóż konto, aby przejrzeć szczegóły tej gildii!", Toast.LENGTH_LONG).show()
                    mainActivity?.showLoginDialog()
                }
            },
            onJoinClick = { guild ->
                val mainActivity = activity as? MainActivity
                val currentUser = mainActivity?.getCurrentUser()

                if (currentUser == null) {
                    Toast.makeText(requireContext(), "Zaloguj się, aby dołączyć do gildii!", Toast.LENGTH_SHORT).show()
                    mainActivity?.showLoginDialog()
                } else {
                    val status = dbHelper.joinGuild(guild.id, currentUser.id, guild.requiresApproval)
                    if (status == "ALREADY_IN_GUILD") {
                        val currentG = dbHelper.getUserGuild(currentUser.id)
                        Toast.makeText(requireContext(), "Należysz już do gildii \"${currentG?.name}\"! Możesz należeć tylko do 1 gildii.", Toast.LENGTH_LONG).show()
                    } else if (status == "ACTIVE") {
                        Toast.makeText(requireContext(), "Dołączono do gildii ${guild.name}!", Toast.LENGTH_SHORT).show()
                        isViewingBrowseMode = false
                        checkUserGuildState()
                    } else if (status == "PENDING") {
                        Toast.makeText(requireContext(), "Wysłano prośbę o dołączenie do ${guild.name}. Czeka na akceptację.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )

        rvGuilds.layoutManager = LinearLayoutManager(requireContext())
        rvGuilds.adapter = guildAdapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterGuilds()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterGuilds() {
        val query = etSearch.text.toString().lowercase()
        val filtered = allGuilds.filter { guild ->
            guild.name.lowercase().contains(query) ||
                    guild.city.lowercase().contains(query) ||
                    guild.description.lowercase().contains(query)
        }
        if (::guildAdapter.isInitialized) {
            guildAdapter.updateGuilds(filtered)
        }
    }
}