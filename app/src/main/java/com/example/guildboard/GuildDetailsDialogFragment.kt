package com.example.guildboard

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GuildDetailsDialogFragment : DialogFragment() {

    private lateinit var tvName: TextView
    private lateinit var tvMeta: TextView
    private lateinit var tvDesc: TextView
    private lateinit var rvMembers: RecyclerView
    private lateinit var btnClose: Button

    private lateinit var adapter: GuildMemberAdapter
    private lateinit var dbHelper: GuildBoardDbHelper

    private var guildId: Long = 0
    private var guildName: String = ""
    private var guildDesc: String = ""
    private var guildCity: String = ""
    private var guildOwnerNick: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        guildId = arguments?.getLong(ARG_GUILD_ID) ?: 0
        guildName = arguments?.getString(ARG_GUILD_NAME) ?: ""
        guildDesc = arguments?.getString(ARG_GUILD_DESC) ?: ""
        guildCity = arguments?.getString(ARG_GUILD_CITY) ?: ""
        guildOwnerNick = arguments?.getString(ARG_GUILD_OWNER_NICK) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_guild_details, container, false)
        dbHelper = GuildBoardDbHelper(requireContext())

        tvName = view.findViewById(R.id.tvDetailGuildName)
        tvMeta = view.findViewById(R.id.tvDetailGuildMeta)
        tvDesc = view.findViewById(R.id.tvDetailGuildDesc)
        rvMembers = view.findViewById(R.id.rvGuildMembers)
        btnClose = view.findViewById(R.id.btnCloseGuildDetails)

        tvName.text = guildName
        tvMeta.text = "Miejscowość: $guildCity | Właściciel: $guildOwnerNick"
        tvDesc.text = guildDesc

        btnClose.setOnClickListener { dismiss() }

        setupMembersList()

        return view
    }

    private fun setupMembersList() {
        val currentUser = (activity as? MainActivity)?.getCurrentUser()
        val members = dbHelper.getGuildMembers(guildId)

        val userRole = dbHelper.getUserGuildRole(guildId, currentUser?.id ?: 0) ?: "MEMBER"

        adapter = GuildMemberAdapter(
            members = members,
            currentUserRole = userRole,
            currentUserId = currentUser?.id ?: 0,
            onActionClick = { member, anchorView ->
                if (currentUser != null) {
                    showMemberActionMenu(member, anchorView, userRole, currentUser)
                }
            }
        )

        rvMembers.layoutManager = LinearLayoutManager(requireContext())
        rvMembers.adapter = adapter
    }

    private fun showMemberActionMenu(member: GuildMember, anchorView: View, currentUserRole: String, currentUser: User) {
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
                    dbHelper.updateGuildMemberStatus(guildId, member.id, "ACCEPTED")
                    Toast.makeText(requireContext(), "Zaakceptowano użytkownika ${member.userNick}", Toast.LENGTH_SHORT).show()
                    setupMembersList()
                    true
                }
                "Odrzuć prośbę", "Usuń z gildii" -> {
                    dbHelper.removeGuildMember(guildId, member.id)
                    Toast.makeText(requireContext(), "Usunięto użytkownika ${member.userNick}", Toast.LENGTH_SHORT).show()
                    setupMembersList()
                    true
                }
                "Nadaj rolę Moderatora" -> {
                    dbHelper.updateGuildMemberRole(member.id, "MODERATOR")
                    Toast.makeText(requireContext(), "Nadano rolę Moderatora dla ${member.userNick}", Toast.LENGTH_SHORT).show()
                    setupMembersList()
                    true
                }
                "Odbierz rolę Moderatora" -> {
                    dbHelper.updateGuildMemberRole(member.id, "MEMBER")
                    Toast.makeText(requireContext(), "Odebrano rolę Moderatora dla ${member.userNick}", Toast.LENGTH_SHORT).show()
                    setupMembersList()
                    true
                }
                "Przekaż rolę Właściciela" -> {
                    val success = dbHelper.transferGuildOwnership(
                        guildId = guildId,
                        currentOwnerUserId = currentUser.id,
                        newOwnerMemberId = member.id,
                        newOwnerUserId = member.userId,
                        newOwnerNick = member.userNick
                    )
                    if (success) {
                        Toast.makeText(requireContext(), "Przekazano rolę Właściciela dla ${member.userNick}.", Toast.LENGTH_LONG).show()
                        setupMembersList()
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        const val TAG = "GuildDetailsDialogFragment"
        private const val ARG_GUILD_ID = "guild_id"
        private const val ARG_GUILD_NAME = "guild_name"
        private const val ARG_GUILD_DESC = "guild_desc"
        private const val ARG_GUILD_CITY = "guild_city"
        private const val ARG_GUILD_OWNER_NICK = "guild_owner_nick"

        fun newInstance(guild: Guild): GuildDetailsDialogFragment {
            val fragment = GuildDetailsDialogFragment()
            val args = Bundle().apply {
                putLong(ARG_GUILD_ID, guild.id)
                putString(ARG_GUILD_NAME, guild.name)
                putString(ARG_GUILD_DESC, guild.description)
                putString(ARG_GUILD_CITY, guild.city)
                putString(ARG_GUILD_OWNER_NICK, guild.ownerNick)
            }
            fragment.arguments = args
            return fragment
        }
    }
}