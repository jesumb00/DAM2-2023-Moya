package com.example.urbanfit.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.urbanfit.BookingGYM
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.urbanfit.MainActivity
import com.example.urbanfit.databinding.FragmentHomeBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

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
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        fixArray()
        return root
    }
    private fun fixArray() {
        email = (activity as MainActivity).email
        db = FirebaseFirestore.getInstance()
        getBookings()
    }
    private fun getBookings() {

        val userRef = db.collection("user").document(email).collection("booking")

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0) // Establecer la hora en 00:00
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val query = userRef.whereEqualTo("date", today)

        val bookingList = mutableListOf<BookingGYM>() // Lista para almacenar las reservas

        query.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    // Acceder a los datos de cada documento
                    val hour = document.getString("hour") ?: ""
                    val date = convertTimestampToCalendar(document.getTimestamp("date") ?: Timestamp.now())
                    val className = document.getString("class") ?: ""
                    val associatedGym = document.getString("associatedGym") ?: ""

                    // Crear objeto Booking y agregarlo a la lista
                    val booking = BookingGYM(hour, date, className, associatedGym)
                    bookingList.add(booking)

                    // Realizar las acciones deseadas con los datos

                    Log.d("Firestore", "Hora: $hour, Fecha: $date, Clase: $className, Gimnasio: $associatedGym")
                }

                // Aquí puedes realizar cualquier operación adicional con la lista de reservas
                // por ejemplo, mostrarlas en un RecyclerView o procesarlas de alguna otra manera

            }
            .addOnFailureListener { exception ->
                // Manejar el error en caso de que ocurra
                Toast.makeText(requireContext(), "Error al obtener los documentos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    fun convertTimestampToCalendar(timestamp: Timestamp): Calendar {
        val date = timestamp.toDate()
        return Calendar.getInstance().apply { time = date }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}