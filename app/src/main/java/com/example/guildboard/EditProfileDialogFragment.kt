package com.example.guildboard

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class EditProfileDialogFragment : DialogFragment() {

    private lateinit var etNick: EditText
    private lateinit var etCity: EditText
    private lateinit var etGames: EditText
    private lateinit var rgRole: RadioGroup
    private lateinit var btnSave: Button
    private lateinit var tvCancel: TextView

    private lateinit var dbHelper: GuildBoardDbHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_edit_profile, container, false)
        dbHelper = GuildBoardDbHelper(requireContext())

        etNick = view.findViewById(R.id.etEditNick)
        etCity = view.findViewById(R.id.etEditCity)
        etGames = view.findViewById(R.id.etEditGames)
        rgRole = view.findViewById(R.id.rgEditRole)
        btnSave = view.findViewById(R.id.btnSaveProfile)
        tvCancel = view.findViewById(R.id.tvCloseEditProfile)

        val currentUser = (activity as? MainActivity)?.getCurrentUser()
        if (currentUser != null) {
            etNick.setText(currentUser.nickname)
            etCity.setText(currentUser.city)
            etGames.setText(currentUser.ownedGamesText)
        }

        tvCancel.setOnClickListener { dismiss() }

        btnSave.setOnClickListener {
            val nick = etNick.text.toString().trim()
            val city = etCity.text.toString().trim()
            val games = etGames.text.toString().trim()

            if (nick.isEmpty()) {
                Toast.makeText(requireContext(), "Podaj nazwę użytkownika!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (city.isEmpty()) {
                Toast.makeText(requireContext(), "Podaj miejscowość!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val roleText = when (rgRole.checkedRadioButtonId) {
                R.id.rbEditGm -> "Mistrz Gry • Średniozaawansowany"
                R.id.rbEditOrganizer -> "Organizator • Zaawansowany"
                else -> "Gracz • Początkujący"
            }

            if (currentUser != null) {
                val success = dbHelper.updateUserProfile(
                    userId = currentUser.id,
                    newNick = nick,
                    newCity = city,
                    newGames = games,
                    newRole = roleText
                )

                if (success) {
                    val updatedUser = currentUser.copy(
                        nickname = nick,
                        city = city,
                        distanceText = "$city (0.5 km)",
                        ownedGamesText = games,
                        roleAndLevel = roleText
                    )
                    (activity as? MainActivity)?.onUserAuthenticated(updatedUser)
                    Toast.makeText(requireContext(), "Zapisano zmiany w profilu!", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Nie udało się zapisać zmian.", Toast.LENGTH_SHORT).show()
                }
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
        const val TAG = "EditProfileDialogFragment"
        fun newInstance() = EditProfileDialogFragment()
    }
}