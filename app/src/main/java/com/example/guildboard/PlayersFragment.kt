package com.example.guildboard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PlayersFragment : Fragment() {

    private lateinit var rvPlayers: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var adapter: PlayerAdapter
    private lateinit var dbHelper: GuildBoardDbHelper

    private var allPlayers: MutableList<PlayerProfile> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_players, container, false)

        dbHelper = (activity as? MainActivity)?.getDbHelper() ?: GuildBoardDbHelper(requireContext())
        rvPlayers = view.findViewById(R.id.rvPlayers)
        etSearch = view.findViewById(R.id.etSearchPlayer)

        loadPlayersFromDb()
        setupRecyclerView()
        setupSearch()

        return view
    }

    private fun loadPlayersFromDb() {
        allPlayers = dbHelper.getAllPlayers().toMutableList()
    }

    private fun setupRecyclerView() {
        adapter = PlayerAdapter(
            players = allPlayers,
            onInviteClick = { player ->
                Toast.makeText(requireContext(), "Wysłano zaproszenie do gracza: ${player.nickname}", Toast.LENGTH_SHORT).show()
            }
        )

        rvPlayers.layoutManager = LinearLayoutManager(requireContext())
        rvPlayers.adapter = adapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPlayers()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterPlayers() {
        val query = etSearch.text.toString().lowercase()
        val filtered = allPlayers.filter { player ->
            player.nickname.lowercase().contains(query) ||
                    player.roleAndLevel.lowercase().contains(query) ||
                    player.ownedGamesText.lowercase().contains(query)
        }

        adapter.updatePlayers(filtered)
    }
}