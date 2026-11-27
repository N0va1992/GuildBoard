package com.example.guildboard

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class CreateEventDialogFragment : DialogFragment() {

    private lateinit var etTitle: EditText
    private lateinit var etCity: EditText
    private lateinit var etAddress: EditText
    private lateinit var etDateTime: EditText
    private lateinit var etDesc: EditText
    private lateinit var btnCreate: Button
    private lateinit var tvCancel: TextView

    private lateinit var dbHelper: GuildBoardDbHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_create_event, container, false)
        dbHelper = GuildBoardDbHelper(requireContext())

        etTitle = view.findViewById(R.id.etEventTitle)
        etCity = view.findViewById(R.id.etEventCity)
        etAddress = view.findViewById(R.id.etEventAddress)
        etDateTime = view.findViewById(R.id.etEventDateTime)
        etDesc = view.findViewById(R.id.etEventDesc)
        btnCreate = view.findViewById(R.id.btnDoCreateEvent)
        tvCancel = view.findViewById(R.id.tvCloseCreateEvent)

        tvCancel.setOnClickListener { dismiss() }

        btnCreate.setOnClickListener {
            val mainActivity = activity as? MainActivity
            val currentUser = mainActivity?.getCurrentUser()

            if (currentUser == null) {
                Toast.makeText(requireContext(), "Zaloguj się, aby zorganizować spotkanie!", Toast.LENGTH_SHORT).show()
                dismiss()
                mainActivity?.showLoginDialog()
                return@setOnClickListener
            }

            val title = etTitle.text.toString().trim()
            val city = etCity.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val dt = etDateTime.text.toString().trim()
            val desc = etDesc.text.toString().trim()

            if (title.isEmpty() || city.isEmpty() || address.isEmpty() || dt.isEmpty()) {
                Toast.makeText(requireContext(), "Uzupełnij wymagane pola (Nazwa, Miasto, Adres, Data)!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Domyślnie na potrzeby prostoty ustalamy limit miejsc na 4
            val eventId = dbHelper.createGameEvent(title, desc, 4, currentUser.id, city, address, dt)

            if (eventId > 0) {
                Toast.makeText(requireContext(), "Utworzono spotkanie: $title", Toast.LENGTH_SHORT).show()
                (parentFragmentManager.fragments.find { it is GamesListFragment } as? GamesListFragment)?.refreshEvents()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Błąd podczas tworzenia wydarzenia.", Toast.LENGTH_SHORT).show()
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
        const val TAG = "CreateEventDialogFragment"
        fun newInstance() = CreateEventDialogFragment()
    }
}