package com.example.guildboard

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var btnLogin: ImageButton
    private lateinit var btnMenu: ImageButton
    private lateinit var tvHeaderTitle: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

    private var currentUserNick: String = "Jan_Gracz"
    private var isLoggedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnLogin = findViewById(R.id.btnLogin)
        btnMenu = findViewById(R.id.btnMenu)
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Przycisk w lewym górnym rogu: Panel logowania
        btnLogin.setOnClickListener {
            showLoginDialog()
        }

        // Przycisk w prawym górnym rogu: Menu rozwijane
        btnMenu.setOnClickListener { view ->
            showTopMenu(view)
        }

        // Dolne menu nawigacyjne
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_games -> {
                    loadFragment(GamesListFragment())
                    true
                }
                R.id.nav_chat -> {
                    loadFragment(ChatFragment())
                    true
                }
                R.id.nav_players -> {
                    loadFragment(PlayersFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        // Ekran startowy: Lista gier
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_games
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // Wyświetla rozwijane menu w prawym górnym rogu z odpowiednim motywem ciemnym
    private fun showTopMenu(view: View) {
        val wrapper = ContextThemeWrapper(this, R.style.CustomPopupMenuTheme)
        val popupMenu = PopupMenu(wrapper, view)
        popupMenu.menuInflater.inflate(R.menu.top_overflow_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_logout -> {
                    performLogout()
                    true
                }
                R.id.menu_about -> {
                    showAboutDialog()
                    true
                }
                R.id.menu_exit -> {
                    Toast.makeText(this, "Zamykanie aplikacji...", Toast.LENGTH_SHORT).show()
                    finishAffinity()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    fun showLoginDialog() {
        val dialog = LoginDialogFragment.newInstance()
        dialog.show(supportFragmentManager, LoginDialogFragment.TAG)
    }

    fun showAboutDialog() {
        val dialog = AboutDialogFragment.newInstance()
        dialog.show(supportFragmentManager, AboutDialogFragment.TAG)
    }

    fun performLogin(nickOrEmail: String) {
        isLoggedIn = true
        currentUserNick = if (nickOrEmail.contains("@")) nickOrEmail.substringBefore("@") else nickOrEmail
        Toast.makeText(this, "Zalogowano pomyślnie jako: $currentUserNick", Toast.LENGTH_SHORT).show()

        // Odśwież obecny fragment, jeśli wyświetlany jest Profil
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is ProfileFragment) {
            loadFragment(ProfileFragment())
        }
    }

    fun performLogout() {
        if (!isLoggedIn) {
            Toast.makeText(this, "Nie jesteś zalogowany.", Toast.LENGTH_SHORT).show()
            return
        }
        isLoggedIn = false
        currentUserNick = "Gość"
        Toast.makeText(this, "Wylogowano pomyślnie.", Toast.LENGTH_SHORT).show()

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is ProfileFragment) {
            loadFragment(ProfileFragment())
        }
    }

    fun selectChatTab() {
        if (bottomNavigationView.selectedItemId == R.id.nav_chat) {
            loadFragment(ChatFragment())
        } else {
            bottomNavigationView.selectedItemId = R.id.nav_chat
        }
    }

    fun getCurrentUserNick(): String = currentUserNick

    fun isUserLoggedIn(): Boolean = isLoggedIn
}