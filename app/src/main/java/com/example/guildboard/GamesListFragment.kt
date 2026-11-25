package com.example.guildboard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GamesListFragment : Fragment() {

    private lateinit var rvEvents: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var adapter: EventAdapter
    private lateinit var dbHelper: GuildBoardDbHelper

    private var allEvents: MutableList<GameEvent> = mutableListOf()
    private var currentFilter = "Wszystkie"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_games_list, container, false)

        dbHelper = (activity as? MainActivity)?.getDbHelper() ?: GuildBoardDbHelper(requireContext())
        rvEvents = view.findViewById(R.id.rvGameEvents)
        etSearch = view.findViewById(R.id.etSearchGame)

        loadDataFromDatabase()
        setupRecyclerView()
        setupFilters(view)
        setupSearch()

        return view
    }

    private fun loadDataFromDatabase() {
        allEvents = dbHelper.getAllGameEvents().toMutableList()
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter(
            events = allEvents,
            onJoinClick = { event ->
                event.isJoined = true
                dbHelper.updateEventJoinedStatus(event.id, true)
                adapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Wysłano prośbę dołączenia do: ${event.title}", Toast.LENGTH_SHORT).show()
            },
            onOpenChatClick = { event ->
                Toast.makeText(requireContext(), "Otwieranie czatu dla: ${event.title}", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.selectChatTab()
            }
        )

        rvEvents.layoutManager = LinearLayoutManager(requireContext())
        rvEvents.adapter = adapter
    }

    private fun setupFilters(view: View) {
        val chipAll = view.findViewById<TextView>(R.id.chipAll)
        val chipBoardgames = view.findViewById<TextView>(R.id.chipBoardgames)
        val chipRpg = view.findViewById<TextView>(R.id.chipRpg)
        val chipTournaments = view.findViewById<TextView>(R.id.chipTournaments)

        val chips = listOf(chipAll, chipBoardgames, chipRpg, chipTournaments)

        chips.forEach { chip ->
            chip.setOnClickListener {
                currentFilter = chip.text.toString()

                chips.forEach { c ->
                    if (c == chip) {
                        c.setBackgroundResource(R.drawable.bg_chip)
                        c.backgroundTintList = android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), R.color.accent_gold)
                        )
                        c.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_dark))
                    } else {
                        c.setBackgroundResource(R.drawable.bg_chip)
                        c.backgroundTintList = null
                        c.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_white))
                    }
                }

                filterEvents()
            }
        }
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterEvents()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterEvents() {
        val query = etSearch.text.toString().lowercase()
        val filtered = allEvents.filter { event ->
            val matchesCategory = when (currentFilter) {
                "Wszystkie" -> true
                "Planszówki" -> event.category == "Planszówki"
                "Sesje RPG" -> event.category == "Sesje RPG"
                "Turnieje" -> event.category == "Turnieje"
                else -> true
            }

            val matchesQuery = event.title.lowercase().contains(query) ||
                    event.description.lowercase().contains(query)

            matchesCategory && matchesQuery
        }

        adapter.updateEvents(filtered)
    }
}