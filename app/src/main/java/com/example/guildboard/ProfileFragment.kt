package com.example.guildboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    private lateinit var layoutLoggedIn: LinearLayout
    private lateinit var layoutNotLoggedIn: LinearLayout

    private lateinit var tvNick: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvGamesCount: TextView
    private lateinit var tvTablesCount: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvGamesCollection: TextView
    private lateinit var btnManageRequests: Button
    private lateinit var btnEditProfile: Button
    private lateinit var btnLoginFromProfile: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        layoutLoggedIn = view.findViewById(R.id.layoutLoggedIn)
        layoutNotLoggedIn = view.findViewById(R.id.layoutNotLoggedIn)

        tvNick = view.findViewById(R.id.tvProfileNick)
        tvStatus = view.findViewById(R.id.tvProfileStatus)
        tvGamesCount = view.findViewById(R.id.tvGamesCount)
        tvTablesCount = view.findViewById(R.id.tvTablesCount)
        tvRating = view.findViewById(R.id.tvRating)
        tvGamesCollection = view.findViewById(R.id.tvGamesCollection)

        btnManageRequests = view.findViewById(R.id.btnManageRequests)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnLoginFromProfile = view.findViewById(R.id.btnLoginFromProfile)

        updateProfileUI()

        btnLoginFromProfile.setOnClickListener {
            (activity as? MainActivity)?.showLoginDialog()
        }

        btnManageRequests.setOnClickListener {
            Toast.makeText(requireContext(), "Otwieranie panelu zarządzania zgłoszeniami...", Toast.LENGTH_SHORT).show()
        }

        btnEditProfile.setOnClickListener {
            val dialog = EditProfileDialogFragment.newInstance()
            dialog.show(parentFragmentManager, EditProfileDialogFragment.TAG)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        updateProfileUI()
    }

    private fun updateProfileUI() {
        val mainActivity = activity as? MainActivity ?: return
        val currentUser = mainActivity.getCurrentUser()

        if (currentUser != null) {
            layoutNotLoggedIn.visibility = View.GONE
            layoutLoggedIn.visibility = View.VISIBLE

            tvNick.text = "${currentUser.nickname} (Ty)"
            tvStatus.text = "Miejscowość: ${currentUser.city} • ${currentUser.roleAndLevel}"

            tvGamesCount.text = currentUser.gamesPlayed.toString()
            tvTablesCount.text = currentUser.tablesOrganized.toString()

            if (currentUser.rating > 0) {
                tvRating.text = "${currentUser.rating} ★"
            } else {
                tvRating.text = "Brak ★"
            }

            if (currentUser.ownedGamesText.isNotEmpty()) {
                val formattedGames = currentUser.ownedGamesText
                    .split(",")
                    .joinToString("\n") { "• ${it.trim()}" }
                tvGamesCollection.text = formattedGames
            } else {
                tvGamesCollection.text = "• Brak gier w kolekcji (Uzupełnij w edycji profilu)"
            }
        } else {
            layoutLoggedIn.visibility = View.GONE
            layoutNotLoggedIn.visibility = View.VISIBLE
        }
    }
}