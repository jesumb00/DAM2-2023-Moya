package com.example.urbanfit.ui.bookings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.urbanfit.*
import com.example.urbanfit.databinding.FragmentBookingsBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class BookingsFragment : Fragment(), AdapterCallback {

    private var _binding: FragmentBookingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var email: String
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bookingsViewModel = ViewModelProvider(this).get(BookingsViewModel::class.java)
        _binding = FragmentBookingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        fixArray()




        return root
    }

    private fun fixArray() {
        email = (activity as MainActivity).email
        db = FirebaseFirestore.getInstance()
        db.collection("user").document(email).get().addOnSuccessListener { documentSnapshot ->
            val associatedGym = documentSnapshot.getString("associatedGym")
            if (associatedGym != null) {
                getClasses(associatedGym)
            }
        }
    }

    private fun getClasses(associatedGym: String) {
        db.collection("class").whereEqualTo("associatedGym", "Urban Gran Via").get().addOnSuccessListener { documents ->

            val tempList = mutableListOf<ClassGym>()
            val currentTime = Calendar.getInstance()

            for (document in documents) {
                val timestamp = document.getTimestamp("hour")

                // Verificar si el campo 'hours' es un valor válido
                if (timestamp != null) {
                    val classTime = Calendar.getInstance()
                    classTime.time = timestamp.toDate()

                    // Establecer la fecha actual solo para la comparación de horas y minutos
                    classTime.set(Calendar.YEAR, currentTime.get(Calendar.YEAR))
                    classTime.set(Calendar.MONTH, currentTime.get(Calendar.MONTH))
                    classTime.set(Calendar.DAY_OF_MONTH, currentTime.get(Calendar.DAY_OF_MONTH))

                    // Comparar hora y minutos de la clase
                    val classHour = classTime.get(Calendar.HOUR_OF_DAY)
                    val classMinute = classTime.get(Calendar.MINUTE)
                    val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
                    val currentMinute = currentTime.get(Calendar.MINUTE)

                    //if (classHour > currentHour || (classHour == currentHour && classMinute > currentMinute)) {
                        val name = document.getString("name") ?: ""
                        val description = document.getString("description") ?: ""
                        val schedule = document.getString("schedule") ?: ""
                        val maximumCapacity = document.getLong("maximumCapacity")?.toInt() ?: 0
                        val capacity = document.getLong("capacity")?.toInt() ?: 0
                        val difficulty = document.getString("difficulty") ?: ""
                        val monitor = document.getString("monitor") ?: ""

                        tempList += ClassGym(
                            name = name,
                            description = description,
                            schedule = schedule,
                            maximumCapacity = maximumCapacity,
                            capacity = capacity,
                            difficulty = difficulty,
                            associatedGym = associatedGym,
                            monitor = monitor
                        )
                    //}
                }
            }

            val adapterClass = AdapterClass(requireContext(), R.layout.bookings_item, tempList)
            adapterClass.setAdapterCallback(this)
            binding.listClass.adapter = adapterClass

        }
    }






    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClicked(data: ClassGym) {
        var name=capitalizeFirstLetter(data.name)
        Toast.makeText(requireContext(),"$name",Toast.LENGTH_SHORT).show()
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmación")
        builder.setMessage("¿Deseas reservar la $name?")
        builder.setPositiveButton("Reservar") { dialog, which ->
            // Acciones a realizar cuando se presiona el botón Aceptar
            Toast.makeText(requireContext(), "Aceptar", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
            // Acciones a realizar cuando se presiona el botón Cancelar
            Toast.makeText(requireContext(), "Cancelar", Toast.LENGTH_SHORT).show()
        }

        val dialog = builder.create()
        dialog.show()

    }
    fun capitalizeFirstLetter(input: String): String {
        if (input.isEmpty()) {
            return input
        }

        val firstChar = input.substring(0, 1)
        val remainingChars = input.substring(1)

        return "${firstChar.lowercase(Locale.getDefault())}$remainingChars"
    }


}