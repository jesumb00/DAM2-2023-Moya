package com.example.urbanfit



import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.widget.*
import com.bumptech.glide.Glide



import com.example.urbanfit.databinding.ActivityDataUserBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar

import java.util.Date

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

import android.view.View


class DataUser : AppCompatActivity() {


    lateinit var email: String
    lateinit var password: String
    var create: Boolean = false
    lateinit var dateBirthdate : Date

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

   lateinit var binding: ActivityDataUserBinding
   var imageUri: Uri? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_user)

        binding = ActivityDataUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getDataUser()

        if (!create) {
            dataRecovery()
            getFirebaseStorageImageReference()
        }

        OptionsGYMAssociated()
        binding.dataBirthdate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.imageEditButton.setOnClickListener {
            selectImage()

        }

            binding.dataSaveButton.setOnClickListener{
                createUser()
                if (imageUri != null) uploadImage()
                if(create) register(email,password)
                if(create) {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }else{
                    startActivity(Intent(this, MainActivity::class.java)
                        .putExtra("yes", true)
                        .putExtra("email", email)
                        .putExtra("password",password))
                    finish()
                }

            }
        binding.dataCancelledButton.setOnClickListener{
            if(create) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }else{
                startActivity(Intent(this, MainActivity::class.java)
                    .putExtra("yes", true)
                    .putExtra("email", email)
                    .putExtra("password",password))
                finish()
            }

        }


    }

    /**
     * se encarga de obtener la referencia al objeto de Firebase Storage donde se encuentra
     * la imagen del usuario que se quiere cargar. Luego, utiliza la librería Glide para
     * cargar la imagen y establece un listener para manejar eventos de carga.
     * */
    fun getFirebaseStorageImageReference() {
        // Obtiene la referencia al objeto de Firebase Storage donde se encuentra la imagen a cargar
        val storageReference = FirebaseStorage.getInstance().getReference("user/$email")
        // Utiliza Glide para cargar la imagen y establece un listener para manejar eventos de carga
        Glide
            .with(this)
            .load(storageReference)
            .listener(object : RequestListener<Drawable> {
                // Este método se llama si la carga de la imagen falla
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Oculta la barra de progreso si la carga falla
                    binding.progressBar.visibility = View.GONE
                    return false
                }

                // Este método se llama cuando la imagen se ha cargado correctamente
                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Oculta la barra de progreso cuando la carga se completa
                    binding.progressBar.visibility = View.GONE
                    return false
                }
            })
            // Establece la vista de destino para la imagen cargada
            .into(binding.image);
    }
    /**
     * se encarga de seleccionar el RadioButton correspondiente dentro de un RadioGroup,
     * de acuerdo al texto que se le pasó.
     * */
    fun toDateBirthdate(timestamp: Timestamp?): String {
        // Obtiene una instancia del objeto Calendar para trabajar con fechas
        val calendar = Calendar.getInstance()
        //let es una función que se utiliza para realizar una acción en un objeto no nulo.
        //Si el objeto es nulo, el bloque de código dentro de la función let no se ejecuta.
        timestamp?.let {
            // Convierte el objeto Timestamp a milisegundos
            val timeInMillis = it.seconds * 1000 + it.nanoseconds / 1000000
            // Configura el calendario con la fecha en milisegundos
            calendar.timeInMillis = timeInMillis
            // Obtiene el día, el mes y el año del calendario
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)

            dateBirthdate=calendar.time

            // Retorna la fecha en formato de cadena de texto
            return "$day/$month/$year"
        }
        // Retorna una cadena vacía si el argumento 'timestamp' es nulo
        return ""
    }

    fun selectRadioButtonByText(text: String?) {
        // Recorremos todos los elementos hijos del RadioGroup
        for (i in 0 until binding.radioGroup.childCount) {
            // Obtenemos el RadioButton en la posición i
            val radioButton = binding.radioGroup.getChildAt(i) as RadioButton
            // Comparamos el texto del RadioButton con el texto que buscamos
            if (radioButton.text.toString() == text) {
                // Si el texto coincide, marcamos el RadioButton
                radioButton.isChecked = true
                // Salimos del bucle
                break
            }
        }
    }


    /**
     * devuelve el índice del elemento correspondiente en un Spinner.
     * */
    fun getSpinnerIndexByText(text: String?): Int {
        // Recorre los elementos del Spinner
        for (i in 0 until binding.dataGYMAssociated.count) {
            // Verifica si el texto del elemento en la posición actual coincide con el texto deseado
            if (binding.dataGYMAssociated.getItemAtPosition(i).toString() == text) {
                // Devuelve el índice de la posición actual
                return i
            }
        }
        // Si no se encontró ningún elemento que coincida, devuelve -1
        return -1
    }
    /**
     * se encarga de seleccionar en un Spinner el elemento que tiene ese texto.
     * */
    fun selectSpinnerItemByText(text: String?) {
        // Obtiene el índice del elemento del Spinner que tiene el texto buscado
        val index = getSpinnerIndexByText(text)

        // Si se encontró el elemento, lo selecciona en el Spinner
        if (index != -1) {
            binding.dataGYMAssociated.setSelection(index)
        }
    }
    /**
     * recuperar los datos de un usuario específico almacenados en la base de datos
     * de Firebase Firestore y mostrarlos en los campos correspondientes del formulario
     * en la aplicación.
     * */
    private fun dataRecovery() {
        // Obtiene una instancia de FirebaseFirestore
        db = FirebaseFirestore.getInstance()
        // Accede al documento correspondiente al label del usuario y obtiene los datos del mismo
        db.collection("user").document(email).get().addOnSuccessListener { documentSnapshot ->
            // Rellena los campos del formulario con los datos obtenidos del documento
            binding.dataName.setText(documentSnapshot.get("name") as String?)
            binding.dataLastName.setText(documentSnapshot.get("las_name") as String?)
            binding.dataAddress.setText(documentSnapshot.get("address") as String?)
            // Convierte el campo "birthdate" de tipo Timestamp a tipo String y lo muestra en el campo correspondiente del formulario
            binding.dataBirthdate.setText(toDateBirthdate(documentSnapshot.get("birthdate") as Timestamp?))
            // Selecciona el RadioButton correspondiente al género obtenido del documento
            selectRadioButtonByText(documentSnapshot.get("gender") as String?)
            // Selecciona el SpinnerItem correspondiente al gimnasio asociado obtenido del documento
            selectSpinnerItemByText(documentSnapshot.get("associatedGym") as String?)
        }
    }

    /**
     * Retorna el texto de la opción seleccionada en el grupo de RadioButton.
     * Si no hay una opción seleccionada, retorna una cadena vacía.
     */
    fun getSelectedRadioButtonText(): String? {
        // Obtiene el ID del RadioButton seleccionado
        val checkedButtonId = binding.radioGroup.checkedRadioButtonId
        return if (checkedButtonId != -1) { // Si hay algún RadioButton seleccionado
            // Busca el RadioButton seleccionado por su ID y retorna su texto
            val checkedButton = binding.radioGroup.findViewById<RadioButton>(checkedButtonId)
            checkedButton.text.toString()
        } else { // Si no hay RadioButton seleccionado
            ""
        }
    }

    /**
     * Función que devuelve el texto del item seleccionado en un Spinner.
     * @return el texto del item seleccionado o un string vacío si no hay item seleccionado
     */
    fun getSelectedSpinnerItemText(): String? {
        // Verifica que el spinner tenga algún item seleccionado
        return if (binding.dataGYMAssociated.selectedItem != null) {
            // Si hay un item seleccionado, devuelve su texto como String
            binding.dataGYMAssociated.selectedItem.toString()
        } else {
            // Si no hay item seleccionado, devuelve un string vacío
            ""
        }
    }


    /**
     *  se encarga de crear un nuevo documento en la colección "user" de Firestore con los datos
     *  de usuario que el usuario ha proporcionado a través de la interfaz de usuario.
     *  */
    private fun createUser() {
        // Obtener una instancia del calendario y establecer sus campos a las 00:00:00 del día actual
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Obtener la fecha actual a partir del calendario
        val date = calendar.time

        // Obtener una instancia de la base de datos Firestore
        db = FirebaseFirestore.getInstance()

        // Crear un nuevo documento en la colección "user" con el label como ID de documento
        db.collection("user").document(email).set(
            hashMapOf(
                "name" to binding.dataName.text.toString(),
                "las_name" to binding.dataLastName.text.toString(),
                "address" to binding.dataAddress.text.toString(),
                "lastRenovation" to date,
                "isActive" to false,
                "gender" to getSelectedRadioButtonText(),
                "associatedGym" to getSelectedSpinnerItemText(),
                // verificar si dateBirthdate es nulo
                "birthdate" to dateBirthdate
            ).mapNotNull { (key, value) ->
                if (value != null) key to value else null
            }.toMap()
        //filtrar los valores nulos de un mapa y devolver un nuevo mapa con solo los pares clave-valor que no son nulos.
        )
    }


    /**
     * obtener los datos del usuario que se reciben como extras en el Intent que lanzó esta
     * actividad. */
    private fun getDataUser() {
        // Se obtienen los extras del Intent que lanzó esta actividad
        var bundle=intent.extras
        // Se obtiene el correo electrónico de usuario
        email = bundle?.getString("email").toString()
        // Se obtiene la contraseña de usuario
        password = bundle?.getString("password").toString()
        // Se obtiene el valor booleano para indicar si el usuario está creando una cuenta nueva
        create = bundle?.getString("create").toBoolean()
    }

    /**
     *  subir una imagen al storage de Firebase
     *  */
    private fun uploadImage() {
        // Obtenemos la referencia al storage de Firebase
        val storageReference = FirebaseStorage.getInstance().getReference("user/$email")

        // Subimos la imagen al storage y añadimos listeners para manejar el éxito o fallo de la operación
        storageReference.putFile(imageUri!!)
            .addOnSuccessListener {
                // Si la imagen se subió con éxito, limpiamos la vista de la imagen (para que se borre la imagen anterior)
                binding.image.setImageURI(null)
            }
            .addOnFailureListener{
                // Si hubo un error al subir la imagen, no hacemos nada
            }
    }

    /**
     *  función inicia una actividad para permitir al usuario seleccionar una imagen de su dispositivo.
     *  */
    private fun selectImage() {
        // Creamos un nuevo intent
        val intent = Intent()
        // Definimos el tipo de archivo a seleccionar
        intent.type = "image/*"
        // Definimos la acción a realizar, en este caso, obtener contenido
        intent.action = Intent.ACTION_GET_CONTENT
        // Iniciamos la actividad para seleccionar un archivo
        startActivityForResult(intent, 100)
    }

    /**
     * función llamada onActivityResult que se encarga de manejar la respuesta a una
     * actividad iniciada por startActivityForResult*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK){
            Log.d("TAG", "llegue dentro");
            imageUri = data?.data!!
            binding.image.setImageURI(imageUri)
        }
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
                val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, documentNames)
                // Especificamos el layout a utilizar cuando se despliegan las opciones del spinner
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Seteamos el adapter en el spinner
                binding.dataGYMAssociated.adapter=adapter
            }
            .addOnFailureListener { exception ->
                // Maneja el caso en que se produzca un error al acceder a la colección
                Log.e("TAG", "Error al obtener los documentos de la colección", exception)
            }

    }

    /**
     * maneja la selección de la fecha mediante un diálogo de selección de fecha,
     * que se muestra al usuario al hacer clic en un botón correspondiente
     * */
    private fun showDatePickerDialog() {
        val datePicker = DatePickerFragment{day , month, year -> onDateSelected(day, month, year)}
        /*
        Como la clase que inicializamos no sabe que valores es cada dato que
        le pasamos lo ponemos entre parentesis indicando cada valor exacto
        que le pasamos
         */
        datePicker.show(supportFragmentManager, "datePicket")

    }

    /**
     * Mostrar el texto del  DatePicker*/
    fun onDateSelected(day:Int, month:Int, year:Int){
        binding.dataBirthdate.setText("$day/$month/$year")
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)

        dateBirthdate=calendar.time
    }

    /**
     *     funcion para registar el usuario
     */
    private fun register(email: String, password: String) {
        auth = Firebase.auth
        //instancia de auth
        auth.createUserWithEmailAndPassword(email,password)
                //funcion para crear el usuario
            .addOnCompleteListener(this){task ->
                if(task.isSuccessful){
                    //pasar de este activity a otro
                    startActivity(Intent(this, MainActivity::class.java))
                    //finish para no ocupar memoria
                    finish()
                }else{
                    //mensaje por pantalla
                    Toast.makeText(applicationContext, "Registro fallido!", Toast.LENGTH_LONG).show()
                }

            }
    }



}