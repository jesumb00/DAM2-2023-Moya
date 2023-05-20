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
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import java.util.Date


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var email: String
    private lateinit var db: FirebaseFirestore
    private lateinit var chart: BarChart
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
        chart = binding.chart
        countReservationsCurrentWeek(email)





        return root
    }
    /**
     * Inicia la obtencion de datos para la lista
     * */
    private fun fixArray() {
        email = (activity as MainActivity).email // Obtiene el valor de la variable "email" desde la actividad contenedora (MainActivity)
        db = FirebaseFirestore.getInstance() // Obtiene una instancia de FirebaseFirestore

        getBookings() // Llama a la función "getBookings" para obtener las reservas
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



            }
            .addOnFailureListener { exception ->
                // Manejar el error en caso de que ocurra
                Toast.makeText(requireContext(), "Error al obtener los documentos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    /**
     * Convierte un objeto Timestamp a un objeto Calendar
     * */
    fun convertTimestampToCalendar(timestamp: Timestamp): Calendar {
        val date = timestamp.toDate() // Convierte el objeto Timestamp a un objeto Date
        return Calendar.getInstance().apply { time = date } // Asigna la fecha al objeto Calendar y lo devuelve
    }
    /**
     *  Establece configuracion del estilo y las propiedades del char.
     * */
    private fun setupChart() {
        // Configura el estilo y las propiedades del gráfico
        chart.description.isEnabled = false
        chart.setDrawValueAboveBar(true)

        val xAxis = chart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)

        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.setDrawGridLines(false)

        chart.animateY(800)
    }
    /**
     * Establece configuracion de lineas y margenes de char.
     * */
    private fun setChartMargins() {
        chart.setExtraOffsets(0f, 0f, 0f, 0f)  // Establece los márgenes superior, izquierdo, inferior y derecho a 0

        val xAxis = chart.xAxis
        xAxis.setDrawAxisLine(false)  // No dibujar la línea del eje X
        xAxis.setDrawGridLines(false)  // No dibujar las líneas de la cuadrícula del eje X
        xAxis.setDrawLabels(true)  // Mostrar los labels del eje X

        val leftAxis = chart.axisLeft
        leftAxis.setDrawAxisLine(false)  // No dibujar la línea del eje Y izquierdo
        leftAxis.setDrawGridLines(false)  // No dibujar las líneas de la cuadrícula del eje Y izquierdo
        leftAxis.setDrawLabels(false)  // No mostrar los labels del eje Y izquierdo

        val rightAxis = chart.axisRight
        rightAxis.setDrawAxisLine(false)  // No dibujar la línea del eje Y derecho
        rightAxis.setDrawGridLines(false)  // No dibujar las líneas de la cuadrícula del eje Y derecho
        rightAxis.setDrawLabels(false)  // No mostrar los labels del eje Y derecho

        chart.legend.isEnabled = false  // Ocultar la leyenda

        chart.invalidate()  // Actualizar el gráfico
    }

    /**
     * Establece los datos y algunas configuraciones del char
     */
    private fun setData(daysOfWeek: List<Float>) {
        val bookingsPerDay = listOf("L", "M", "MX", "J", "V", "S", "D") // Lista de los nombres de los días de la semana

        val entries = mutableListOf<BarEntry>()
        for (i in daysOfWeek.indices) {
            val bookingCount = daysOfWeek[i]
            entries.add(BarEntry(i.toFloat(), bookingCount)) // Agregar las entradas de datos para cada día de la semana
        }

        val colors = listOf(
            Color.parseColor("#FF0000"), // Rojo
            Color.parseColor("#00FF00")  // Verde
        )

        val dataSet = BarDataSet(entries, "Reservas") // Crear un conjunto de datos de barras con las entradas
        dataSet.setColors(colors) // Asignar colores a las barras
        dataSet.setDrawValues(false) // No mostrar los valores en las barras

        val barData = BarData(dataSet) // Crear los datos del gráfico de barras
        barData.barWidth = 0.9f // Ajustar el ancho de la barra

        chart.data = barData // Asignar los datos al gráfico
        chart.invalidate() // Invalidar el gráfico para que se actualice y muestre los cambios

        // Configurar los labels en el eje X
        chart.setExtraOffsets(0f, 0f, 0f, 20f) // Ajustar el margen inferior del gráfico
        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(bookingsPerDay) // Asignar los nombres de los días como etiquetas en el eje X
        xAxis.position = XAxis.XAxisPosition.BOTTOM // Posicionar el eje X en la parte inferior
        xAxis.granularity = 1f // Configurar la separación entre las etiquetas en el eje X
        xAxis.setDrawAxisLine(true) // Mostrar la línea del eje X
        xAxis.setDrawGridLines(false) // No mostrar las líneas de la cuadrícula en el eje X
        xAxis.setDrawLabels(true) // Mostrar las etiquetas en el eje X
        xAxis.textSize = 26f // Tamaño de la letra en el eje X
        // Ajustar el tamaño de la letra en el eje Y

        // Aquí puedes agregar cualquier configuración adicional que necesites para el gráfico
    }

    /**
     * Permite recibir la lista producida con las reservas de la semana
     * y adaptarlas para meterlas en el char
     * */
    fun compareAndAssign(values: MutableLiveData<List<Int>>): List<Float> {
        val currentValue = values.value ?: emptyList() // Obtener el valor actual de MutableLiveData. Si es nulo, se utiliza una lista vacía.

        return currentValue.map { if (it > 0) 2.0f else 0.0f } // Mapear cada valor de la lista actual. Si es mayor que 0, se asigna 2.0f, de lo contrario, se asigna 0.0f.
    }


    fun countReservationsCurrentWeek(userId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val startOfWeek = getStartOfWeek() // Obtener la fecha de inicio de la semana actual
        val endOfWeek = getEndOfWeek() // Obtener la fecha de fin de la semana actual

        val dayNames = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

        val bookingCounts = MutableList(dayNames.size) { 0 } // Lista mutable para almacenar los conteos de reserva por día

        val bookingCountsLiveData = MutableLiveData<List<Int>>() // LiveData para almacenar los conteos de reserva

        firestore.collection("user")
            .document(userId)
            .collection("booking")
            .whereGreaterThanOrEqualTo("date", startOfWeek)
            .whereLessThanOrEqualTo("date", endOfWeek)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val bookingDate = document.getDate("date")
                    val dayOfWeek = getDayOfWeek(bookingDate)
                    if (dayOfWeek != null) {
                        bookingCounts[dayOfWeek - 1]++ // Incrementar el conteo de reserva para el día correspondiente
                    }
                }
                bookingCountsLiveData.value = bookingCounts // Asignar los conteos de reserva al LiveData

                // Imprimir el valor actual de bookingCountsLiveData
                println("bookingCountsLiveData: ${bookingCountsLiveData.value}")

                // Obtener y procesar los valores comparados y asignados
                val data = compareAndAssign(bookingCountsLiveData)
                println("Resultado: $data")

                setChartMargins() // Configurar los márgenes del gráfico
                setupChart() // Configurar el gráfico
                setData(data) // Establecer los datos en el gráfico
            }
            .addOnFailureListener { exception ->
                // Manejar cualquier error que ocurra al obtener las reservas
                println("Error al obtener las reservas: ${exception.message}")
            }
    }

    /**
     * Toma una fecha (Date) como parámetro y devuelve el día de la
     * semana correspondiente como un valor entero estableciendo
     * el dia en el que comienza la semana.
     * */
    fun getDayOfWeek(date: Date?): Int? {
        if (date != null) {
            val calendar = Calendar.getInstance() // Obtener una instancia del objeto Calendar
            calendar.time = date // Establecer la fecha en el calendario
            var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // Obtener el día de la semana como un valor entero

            if (dayOfWeek == Calendar.SUNDAY) {
                dayOfWeek = 7 // Cambiar el índice del domingo a 7 (último día de la semana)
            } else {
                dayOfWeek -= 1 // Restar 1 para ajustar el índice a partir de 0
            }

            return dayOfWeek // Devolver el día de la semana
        }
        return null // Si la fecha es nula, se devuelve nulo
    }

    /**
     * Obtiene la fecha de incio de la semana
     * */
    fun getStartOfWeek(): Date {
        val calendar = Calendar.getInstance() // Obtener una instancia del objeto Calendar

        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek) // Establecer el primer día de la semana
        calendar.add(Calendar.DAY_OF_WEEK, -6) // Restar 6 días para obtener el inicio de la semana actual

        // Establecer la hora, minutos, segundos y milisegundos a cero para obtener la fecha de inicio exacta
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        println("Inicio de la semana: ${calendar.time}")

        return calendar.time // Devolver la fecha de inicio de la semana como un objeto Date
    }

    /**
     * Obtiene la fecha de finalizacion de la semana
     * */
    fun getEndOfWeek(): Date {
        val calendar = Calendar.getInstance() // Obtener una instancia del objeto Calendar

        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek) // Establecer el primer día de la semana
        calendar.set(Calendar.HOUR_OF_DAY, 23) // Establecer la última hora del día
        calendar.set(Calendar.MINUTE, 59) // Establecer el último minuto de la hora
        calendar.set(Calendar.SECOND, 59) // Establecer el último segundo del minuto
        calendar.set(Calendar.MILLISECOND, 999) // Establecer el último milisegundo del segundo

        println("Fin de la semana: ${calendar.time}")

        return calendar.time // Devolver la fecha de fin de la semana como un objeto Date
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}