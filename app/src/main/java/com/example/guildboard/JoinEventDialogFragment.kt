package com.example.guildboard

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class JoinEventDialogFragment : DialogFragment() {

    private lateinit var tvTitle: TextView
    private lateinit var rgRole: RadioGroup
    private lateinit var rgCommitment: RadioGroup
    private lateinit var btnSave: Button
    private lateinit var tvCancel: TextView

    private lateinit var dbHelper: GuildBoardDbHelper

    private var eventId: Long = -1
    private var eventTitle: String = ""
    private var currentRole: String = ""
    private var currentCommitment: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getLong(ARG_EVENT_ID, -1)
            eventTitle = it.getString(ARG_EVENT_TITLE, "")
            currentRole = it.getString(ARG_ROLE, "")
            currentCommitment = it.getString(ARG_COMMITMENT, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_join_event, container, false)
        dbHelper = GuildBoardDbHelper(requireContext())

        tvTitle = view.findViewById(R.id.tvJoinEventTitle)
        rgRole = view.findViewById(R.id.rgRole)
        rgCommitment = view.findViewById(R.id.rgCommitment)
        btnSave = view.findViewById(R.id.btnSaveJoin)
        tvCancel = view.findViewById(R.id.tvCloseJoin)

        tvTitle.text = "Dołącz do: $eventTitle"

        // Ustawienie poprzedniego stanu, jeśli użytkownik już dołączył
        if (currentRole.contains("Mistrz Gry")) {
            rgRole.check(R.id.rbRoleGm)
        } else if (currentRole.isNotEmpty()) {
            rgRole.check(R.id.rbRolePlayer)
        }

        when (currentCommitment) {
            "Może będę" -> rgCommitment.check(R.id.rbCommMaybe)
            "Tylko obserwuję" -> rgCommitment.check(R.id.rbCommObserver)
            "Na pewno" -> rgCommitment.check(R.id.rbCommSure)
            "Na pewno będę" -> rgCommitment.check(R.id.rbCommSure) // dla pewności
        }

        tvCancel.setOnClickListener { dismiss() }

        btnSave.setOnClickListener {
            val mainActivity = activity as? MainActivity
            val currentUser = mainActivity?.getCurrentUser()

            if (currentUser == null) {
                Toast.makeText(requireContext(), "Zaloguj się, aby dołączyć!", Toast.LENGTH_SHORT).show()
                dismiss()
                mainActivity?.showLoginDialog()
                return@setOnClickListener
            }

            val role = if (rgRole.checkedRadioButtonId == R.id.rbRoleGm) "Mistrz Gry" else "Gracz"
            val commitment = when (rgCommitment.checkedRadioButtonId) {
                R.id.rbCommMaybe -> "Może będę"
                R.id.rbCommObserver -> "Tylko obserwuję"
                else -> "Na pewno"
            }

            dbHelper.joinOrUpdateEventParticipant(eventId, currentUser.id, role, commitment)
            Toast.makeText(requireContext(), "Zapisano Twój status w wydarzeniu!", Toast.LENGTH_SHORT).show()

            (parentFragment as? GamesListFragment)?.refreshEvents()
            dismiss()
        }

        return view
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
        const val TAG = "JoinEventDialogFragment"
        private const val ARG_EVENT_ID = "event_id"
        private const val ARG_EVENT_TITLE = "event_title"
        private const val ARG_ROLE = "event_role"
        private const val ARG_COMMITMENT = "event_commitment"

        fun newInstance(id: Long, title: String, role: String, commitment: String): JoinEventDialogFragment {
            val fragment = JoinEventDialogFragment()
            val args = Bundle().apply {
                putLong(ARG_EVENT_ID, id)
                putString(ARG_EVENT_TITLE, title)
                putString(ARG_ROLE, role)
                putString(ARG_COMMITMENT, commitment)
            }
            fragment.arguments = args
            return fragment
        }
    }
}