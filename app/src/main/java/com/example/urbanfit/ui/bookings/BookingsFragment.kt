package com.example.urbanfit.ui.bookings

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.urbanfit.*
import com.example.urbanfit.databinding.FragmentBookingsBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*





class BookingsFragment : Fragment(), AdapterCallbackClassGym {

    private var _binding: FragmentBookingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var email: String
    private lateinit var db: FirebaseFirestore
    lateinit var associatedGym: String
    lateinit var adapter: ArrayAdapter<String>
    var isFirstSelection= true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bookingsViewModel = ViewModelProvider(this).get(BookingsViewModel::class.java)
        _binding = FragmentBookingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        fixArray()
        binding.chooseGymAssociated.doOnItemSelected { position ->
            // Acciones a realizar cuando se selecciona una opción del Spinner
            val selectedItem = binding.chooseGymAssociated.getItemAtPosition(position).toString()
            // Realiza las acciones deseadas con la opción seleccionada
            getClasses(selectedItem)
        }

        return root
    }

    /**
     *  crear un ArrayAdapter para poder mostrar los elementos del spinner.
     *  */
    private fun OptionsGYMAssociated() {
        // Obtiene una instancia de FirebaseFirestore
        var db = FirebaseFirestore.getInstance()

        // Accede a la colección deseada
        db.collection("gym")
            .get()
            .addOnSuccessListener { documents ->
                // Crea una lista vacía para almacenar los nombres de los documentos
                var documentNames = listOf<String>()

                // Recorre los documentos de la colección y agrega sus nombres a la lista
                for (document in documents) {
                    documentNames = documentNames.plusElement(document.id)
                }

                // Creamos un ArrayAdapter y le pasamos el contexto de la actividad, el layout de la vista de los elementos y la lista de elementos a mostrar
                adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, documentNames)
                // Especificamos el layout a utilizar cuando se despliegan las opciones del spinner
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Seteamos el adapter en el spinner
                binding.chooseGymAssociated.adapter=adapter
                // Buscar el índice del texto en el ArrayAdapter
                val index = adapter.getPosition(associatedGym)

                // Seleccionar el índice en el Spinner
                binding.chooseGymAssociated.setSelection(index)
            }
            .addOnFailureListener { exception ->
                // Maneja el caso en que se produzca un error al acceder a la colección
                Log.e("TAG", "Error al obtener los documentos de la colección", exception)
            }

    }

    /**
     * Inicia la obtencion de datos de la lista a partir del associatedGym del usuario
     * */
    private fun fixArray() {
        email = (activity as MainActivity).email // Obtiene el valor de la variable "email" desde la actividad contenedora (MainActivity)
        db = FirebaseFirestore.getInstance() // Obtiene una instancia de FirebaseFirestore

        // Realiza una consulta a la colección "user" y obtiene el documento correspondiente al email
        db.collection("user").document(email).get().addOnSuccessListener { documentSnapshot ->
            associatedGym = documentSnapshot.getString("associatedGym") ?: "" // Obtiene el valor del campo "associatedGym" del documento

            // Verifica si el campo "associatedGym" no es nulo
            if (associatedGym != null) {
                OptionsGYMAssociated()
                getClasses(associatedGym) // Llama a la función "getClasses" pasando el valor de "associatedGym" como argumento
            }
        }
    }

    /**
     * Obtiene los datos de las clases del gimnasio asociado al usuario o al elegido,
     * los carga en el adaptador para cargar la lista, pero a su vez comprueba si
     * la hora limite para iniciar la clase hoy para mostrarlas o no
     * */
    private fun getClasses(associatedGym: String) {
        // Realiza una consulta a la colección "class" donde el campo "associatedGym" sea igual al valor pasado como argumento
        db.collection("class").whereEqualTo("associatedGym", associatedGym).get().addOnSuccessListener { documents ->

            val tempList = mutableListOf<ClassGym>()
            val currentTime = Calendar.getInstance()

            // Itera sobre los documentos obtenidos en la consulta
            for (document in documents) {
                val timestamp = document.getTimestamp("hour") // Obtiene el valor del campo "hour" como Timestamp

                // Verificar si el campo 'hour' es un valor válido (no nulo)
                if (timestamp != null) {
                    val classTime = Calendar.getInstance()
                    classTime.time = timestamp.toDate() // Convierte el Timestamp a Date y establece la fecha en classTime

                    // Establecer la fecha actual solo para la comparación de horas y minutos
                    classTime.set(Calendar.YEAR, currentTime.get(Calendar.YEAR))
                    classTime.set(Calendar.MONTH, currentTime.get(Calendar.MONTH))
                    classTime.set(Calendar.DAY_OF_MONTH, currentTime.get(Calendar.DAY_OF_MONTH))

                    // Comparar hora y minutos de la clase con la hora y minutos actuales
                    val classHour = classTime.get(Calendar.HOUR_OF_DAY)
                    val classMinute = classTime.get(Calendar.MINUTE)
                    val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
                    val currentMinute = currentTime.get(Calendar.MINUTE)

                    // Verificar si la clase está programada para una hora posterior a la hora actual
                    if (classHour > currentHour || (classHour == currentHour && classMinute > currentMinute)) {
                        val id = document.id
                        val name = document.getString("name") ?: ""
                        val description = document.getString("description") ?: ""
                        val schedule = document.getString("schedule") ?: ""
                        val maximumCapacity = document.getLong("maximunCapacity")?.toInt() ?: 0
                        val capacity = document.getLong("capacity")?.toInt() ?: 0
                        val difficulty = document.getString("difficulty") ?: ""
                        val monitor = document.getString("monitor") ?: ""
                        if (capacity < maximumCapacity) {//comprueba que la capacidad no es la misma que la maxima

                            // Agregar la clase a la lista tempList
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
            }

            // Crear un adaptador personalizado y establecerlo en el RecyclerView
            val adapterClass = AdapterClassClassGym(requireContext(), R.layout.class_item, tempList)
            adapterClass.setAdapterCallback(this)
            binding.listClass.adapter = adapterClass
        }
    }

    /**
     * Metodo sobrecargado a partir de la interfaz proporcionada para pasar la clase
     * seleccionada en la clase donde decidimos que hacer con el, en este caso una ventana
     * de seleccion que permite agregar reservas al usuario y guardarlas en la base de datos
     * */
    override fun onItemClicked(data: ClassGym) {
        // Capitalizar el nombre de la clase
        var name = capitalizeFirstLetter(data.name)

        // Crear un AlertDialog para mostrar la confirmación de reserva
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmación")
        builder.setMessage("¿Deseas reservar la $name?")
        builder.setPositiveButton("Reservar") { dialog, which ->
            // Acciones a realizar cuando se presiona el botón Aceptar

            // Obtener una instancia de FirebaseFirestore
            db = FirebaseFirestore.getInstance()

            // Obtener la fecha actual sin incluir la hora
            val currentDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0) // Establecer la hora en 00:00
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            // Obtener la referencia a la subcolección "booking" del usuario actual
            val userRef = db.collection("user").document(email).collection("booking")

            // Crear un mapa con los datos de la reserva
            val subcollectionData = hashMapOf(
                "hour" to data.schedule,
                "date" to currentDate,
                "class" to data.name,
                "associatedGym" to data.associatedGym
            )

            // Crear una consulta para verificar si ya existe una reserva con los mismos datos
            val query = userRef.whereEqualTo("date", currentDate)
                .whereEqualTo("class", data.name)
                .whereEqualTo("associatedGym", data.associatedGym)

            // Ejecutar la consulta
            query.get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Ya existe una reserva para la fecha de hoy y coinciden todos los datos
                        seeMessageRepeatReservationShow("Ya tienes una reserva para esta clase")
                    } else {
                        // No hay reserva existente, agregar los datos a la subcolección "booking"
                        userRef.add(subcollectionData)
                            .addOnSuccessListener { documentReference ->
                                seeMessageRepeatReservationShow("Se añadió la reserva")
                                incrementCapacity(data.id) // Incrementar la capacidad de la clase
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

        // Configurar el botón de Cancelar
        builder.setNegativeButton("Cancelar", null)

        // Mostrar el diálogo
        val dialog = builder.create()
        dialog.show()

    }

    /**
     * Permite mostrar una alerta con un mensaje informativo especificado por parametro
     */
    private fun seeMessageRepeatReservationShow(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Aviso")
        builder.setMessage(message)

        // Configurar el botón de aceptar
        builder.setNegativeButton("Aceptar", null)

        // Crear y mostrar la ventana emergente
        val dialog = builder.create()
        dialog.show()
    }
    /**
     * Permite incrementar las personas de la clase reservada
     * */
    fun incrementCapacity(classId: String) {
        Toast.makeText(requireContext(), "$classId", Toast.LENGTH_SHORT).show()
        val classRef = db.collection("class").document(classId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(classRef)
            val currentCapacity = snapshot.getLong("capacity") ?: 0 // Obtiene el valor actual del campo 'capacity' o usa 0 si es nulo
            val newCapacity = currentCapacity + 1 // Incrementa el valor de 'capacity' en 1
            transaction.update(classRef, "capacity", newCapacity) // Actualiza el campo 'capacity' en la transacción
            null // Indica que no se necesita devolver ningún resultado
        }.addOnSuccessListener {
            Toast.makeText(requireContext(), "Campo 'capacity' incrementado correctamente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Error al incrementar el campo 'capacity'", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * Permite convertir la primera letra de una cadena en mayucula
     */
    fun capitalizeFirstLetter(input: String): String {
        if (input.isEmpty()) {
            return input // Si la cadena está vacía, no se hace ningún cambio y se devuelve tal cual
        }

        val firstChar = input.substring(0, 1) // Obtiene el primer carácter de la cadena
        val remainingChars = input.substring(1) // Obtiene el resto de la cadena

        // Concatena el primer carácter en mayúscula con el resto de la cadena y lo devuelve
        return "${firstChar.uppercase(Locale.getDefault())}$remainingChars"
    }

    fun Spinner.doOnItemSelected(action: (position: Int) -> Unit) {
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isFirstSelection) {
                    isFirstSelection = false
                } else {
                    action.invoke(position)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada aquí si no se necesita
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}