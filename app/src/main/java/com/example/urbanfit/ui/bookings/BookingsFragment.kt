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

                    if (classHour > currentHour || (classHour == currentHour && classMinute > currentMinute)) {
                        val id = document.id
                        val name = document.getString("name") ?: ""
                        val description = document.getString("description") ?: ""
                        val schedule = document.getString("schedule") ?: ""
                        val maximumCapacity = document.getLong("maximumCapacity")?.toInt() ?: 0
                        val capacity = document.getLong("capacity")?.toInt() ?: 0
                        val difficulty = document.getString("difficulty") ?: ""
                        val monitor = document.getString("monitor") ?: ""

                        tempList += ClassGym(
                            id = id,
                            name = name,
                            description = description,
                            schedule = schedule,
                            maximumCapacity = maximumCapacity,
                            capacity = capacity,
                            difficulty = difficulty,
                            associatedGym = associatedGym,
                            monitor = monitor
                        )
                    }
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
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmación")
        builder.setMessage("¿Deseas reservar la $name?")
        builder.setPositiveButton("Reservar") { dialog, which ->
            // Acciones a realizar cuando se presiona el botón Aceptar
            db = FirebaseFirestore.getInstance()

            val currentDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0) // Establecer la hora en 00:00
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            val userRef = db.collection("user").document(email).collection("booking")
            val subcollectionData = hashMapOf(
                "hour" to data.schedule,
                "date" to currentDate,
                "class" to data.name,
                "associatedGym" to data.associatedGym
            )
            val query = userRef.whereEqualTo("date", currentDate)
                .whereEqualTo("class", data.name)
                .whereEqualTo("associatedGym", data.associatedGym)

            query.get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Ya existe una reserva para la fecha de hoy y coinciden todos los datos
                        seeMessageRepeatReservationShow("Ya tienes una reserva para esta clase")
                    } else {
                        // No hay reserva existente, agregar los datos a la subcolección
                        userRef.add(subcollectionData)
                            .addOnSuccessListener { documentReference ->
                                seeMessageRepeatReservationShow("Se añadió la reserva")
                                incrementCapacity(data.id)
                            }
                            .addOnFailureListener { exception ->
                                seeMessageRepeatReservationShow("No se pudo añadir la reserva")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    seeMessageRepeatReservationShow("Error al consultar las reservas")
                }
            }

        builder.setNegativeButton("Cancelar", null)

        val dialog = builder.create()
        dialog.show()

    }
    private fun seeMessageRepeatReservationShow(message : String){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Aviso")
        builder.setMessage(message)

// Configurar el botón de aceptar
        builder.setNegativeButton("Aceptar", null)

// Crear y mostrar la ventana emergente
        val dialog = builder.create()
        dialog.show()

    }
    fun incrementCapacity(classId: String) {
        Toast.makeText(requireContext(), "$classId", Toast.LENGTH_SHORT).show()
        val classRef = db.collection("class").document(classId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(classRef)
            val currentCapacity = snapshot.getLong("capacity") ?: 0
            val newCapacity = currentCapacity + 1
            transaction.update(classRef, "capacity", newCapacity)
            null
        }.addOnSuccessListener {
            Toast.makeText(requireContext(), "Campo 'capacity' incrementado correctamente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Error al incrementar el campo 'capacity'", Toast.LENGTH_SHORT).show()
        }


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