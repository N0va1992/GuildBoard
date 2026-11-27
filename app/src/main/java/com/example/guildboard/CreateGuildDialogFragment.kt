package com.example.guildboard

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class CreateGuildDialogFragment : DialogFragment() {

    private lateinit var etName: EditText
    private lateinit var etCity: EditText
    private lateinit var etDesc: EditText
    private lateinit var cbApproval: CheckBox
    private lateinit var btnCreate: Button
    private lateinit var tvCancel: TextView

    private lateinit var dbHelper: GuildBoardDbHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_create_guild, container, false)
        dbHelper = GuildBoardDbHelper(requireContext())

        etName = view.findViewById(R.id.etGuildName)
        etCity = view.findViewById(R.id.etGuildCity)
        etDesc = view.findViewById(R.id.etGuildDesc)
        cbApproval = view.findViewById(R.id.cbRequiresApproval)
        btnCreate = view.findViewById(R.id.btnDoCreateGuild)
        tvCancel = view.findViewById(R.id.tvCloseCreateGuild)

        tvCancel.setOnClickListener { dismiss() }

        btnCreate.setOnClickListener {
            val mainActivity = activity as? MainActivity
            val currentUser = mainActivity?.getCurrentUser()

            if (currentUser == null) {
                Toast.makeText(requireContext(), "Musisz być zalogowany, aby stworzyć gildię!", Toast.LENGTH_SHORT).show()
                dismiss()
                mainActivity?.showLoginDialog()
                return@setOnClickListener
            }

            val name = etName.text.toString().trim()
            val city = etCity.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val reqApp = cbApproval.isChecked

            if (name.isEmpty() || city.isEmpty()) {
                Toast.makeText(requireContext(), "Podaj nazwę gildii oraz miejscowość!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newGuild = Guild(
                name = name,
                description = desc,
                ownerId = currentUser.id,
                ownerNick = currentUser.nickname,
                city = city,
                requiresApproval = reqApp,
                memberCount = 1
            )

            val guildId = dbHelper.createGuild(newGuild)
            if (guildId > 0) {
                Toast.makeText(requireContext(), "Stworzono gildię: $name!", Toast.LENGTH_SHORT).show()
                dismiss()
                // Refresh list
                val guildsFragment = parentFragment as? GuildsFragment
                if (guildsFragment != null) {
                    guildsFragment.loadGuilds()
                    // Reload resume state to jump to "My Guild" page
                    guildsFragment.onResume()
                }
            } else {
                Toast.makeText(requireContext(), "Błąd tworzenia gildii.", Toast.LENGTH_SHORT).show()
            }
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
        const val TAG = "CreateGuildDialogFragment"
        fun newInstance() = CreateGuildDialogFragment()
    }
}