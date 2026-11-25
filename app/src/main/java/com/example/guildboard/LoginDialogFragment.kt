package com.example.guildboard

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class LoginDialogFragment : DialogFragment() {

    private lateinit var layoutLogin: LinearLayout
    private lateinit var layoutRegister: LinearLayout

    // Formularz Logowania
    private lateinit var etLoginEmail: EditText
    private lateinit var etLoginPass: EditText
    private lateinit var btnDoLogin: Button
    private lateinit var btnSwitchToRegister: Button
    private lateinit var tvCloseLogin: TextView

    // Formularz Rejestracji
    private lateinit var etRegisterNick: EditText
    private lateinit var etRegisterEmail: EditText
    private lateinit var etRegisterPassword: EditText
    private lateinit var etRegisterGames: EditText
    private lateinit var rgRole: RadioGroup
    private lateinit var btnDoRegister: Button
    private lateinit var btnSwitchToLogin: Button
    private lateinit var tvCloseRegister: TextView

    private lateinit var dbHelper: GuildBoardDbHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_login, container, false)
        dbHelper = GuildBoardDbHelper(requireContext())

        // Sekcje
        layoutLogin = view.findViewById(R.id.layoutLogin)
        layoutRegister = view.findViewById(R.id.layoutRegister)

        // Elementy Logowania
        etLoginEmail = view.findViewById(R.id.etLoginEmail)
        etLoginPass = view.findViewById(R.id.etLoginPassword)
        btnDoLogin = view.findViewById(R.id.btnDoLogin)
        btnSwitchToRegister = view.findViewById(R.id.btnSwitchToRegister)
        tvCloseLogin = view.findViewById(R.id.tvCloseLogin)

        // Elementy Rejestracji
        etRegisterNick = view.findViewById(R.id.etRegisterNick)
        etRegisterEmail = view.findViewById(R.id.etRegisterEmail)
        etRegisterPassword = view.findViewById(R.id.etRegisterPassword)
        etRegisterGames = view.findViewById(R.id.etRegisterGames)
        rgRole = view.findViewById(R.id.rgRole)
        btnDoRegister = view.findViewById(R.id.btnDoRegister)
        btnSwitchToLogin = view.findViewById(R.id.btnSwitchToLogin)
        tvCloseRegister = view.findViewById(R.id.tvCloseRegister)

        setupListeners()

        return view
    }

    private fun setupListeners() {
        btnSwitchToRegister.setOnClickListener {
            layoutLogin.visibility = View.GONE
            layoutRegister.visibility = View.VISIBLE
        }

        btnSwitchToLogin.setOnClickListener {
            layoutRegister.visibility = View.GONE
            layoutLogin.visibility = View.VISIBLE
        }

        tvCloseLogin.setOnClickListener { dismiss() }
        tvCloseRegister.setOnClickListener { dismiss() }

        // Obsługa Logowania
        btnDoLogin.setOnClickListener {
            val emailOrNick = etLoginEmail.text.toString().trim()
            val pass = etLoginPass.text.toString().trim()

            if (emailOrNick.isEmpty() || pass.isEmpty()) {
                Toast.makeText(requireContext(), "Uzupełnij e-mail/nick oraz hasło!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = dbHelper.authenticateUser(emailOrNick, pass)
            if (user != null) {
                Toast.makeText(requireContext(), "Witaj ponownie, ${user.nickname}!", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.onUserAuthenticated(user)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Błędne dane logowania!", Toast.LENGTH_SHORT).show()
            }
        }

        // Obsługa Rejestracji z walidacją hasła
        btnDoRegister.setOnClickListener {
            val nick = etRegisterNick.text.toString().trim()
            val email = etRegisterEmail.text.toString().trim()
            val pass = etRegisterPassword.text.toString().trim()
            val games = etRegisterGames.text.toString().trim()

            if (nick.isEmpty()) {
                Toast.makeText(requireContext(), "Podaj nazwę użytkownika!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty() || !email.contains("@")) {
                Toast.makeText(requireContext(), "Podaj poprawny adres e-mail!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Wymogi dotyczące hasła: Min. 8 znaków, co najmniej 1 wielka litera, 1 mała litera i 1 cyfra
            val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")
            if (!passwordPattern.matches(pass)) {
                Toast.makeText(
                    requireContext(),
                    "Hasło musi zawierać min. 8 znaków, co najmniej 1 wielką literę, 1 małą literę i 1 cyfrę!",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val selectedRoleId = rgRole.checkedRadioButtonId
            val roleText = when (selectedRoleId) {
                R.id.rbGm -> "Mistrz Gry • Średniozaawansowany"
                R.id.rbOrganizer -> "Organizator • Zaawansowany"
                else -> "Gracz • Początkujący"
            }

            // Tworzenie czystego konta użytkownika (brak gier, 0 ocen)
            val newUser = User(
                nickname = nick,
                email = email,
                password = pass,
                roleAndLevel = roleText,
                city = "Kraków",
                distanceText = "Kraków (0.5 km)",
                ownedGamesText = games, // Puste jeśli nie podano
                gamesPlayed = 0,
                tablesOrganized = 0,
                rating = 0.0
            )

            val resultId = dbHelper.registerUser(newUser)
            if (resultId > 0) {
                val createdUser = newUser.copy(id = resultId)
                Toast.makeText(requireContext(), "Konto stworzone pomyślnie!", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.onUserAuthenticated(createdUser)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Użytkownik o takim nicku lub e-mailu już istnieje!", Toast.LENGTH_SHORT).show()
            }
        }
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
        const val TAG = "LoginDialogFragment"
        fun newInstance() = LoginDialogFragment()
    }
}