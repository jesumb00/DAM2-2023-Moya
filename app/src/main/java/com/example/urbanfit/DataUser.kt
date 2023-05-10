package com.example.urbanfit



import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.widget.*
import androidx.core.view.isVisible
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


    var email:String=""
    var password:String=""
    var create:Boolean=false
    lateinit var dateBirthdate : Date

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

   lateinit var binding: ActivityDataUserBinding
   lateinit var imageUri: Uri
    var items = listOf("Opción 1", "Opción 2", "Opción 3")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_user)

        binding = ActivityDataUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getDataUser()
        if (!create) {
            binding.dataCancelledButton.isVisible = false
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
            uploadImage()
            register(email,password)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        //register(email,password)
        /*binding.image.setOnClickListener {
            selectImage()
        }*/
    }
    private fun deleteUser(){
        //elimina un documento de Firestore en la colección "user" correspondiente al email dado como parámetro.
        db.collection("user").document(email).delete()

    }
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



    fun getSpinnerIndexByText(text: String?): Int {

        for (i in 0 until binding.dataGYMAssociated.count) {
            if (binding.dataGYMAssociated.getItemAtPosition(i).toString() == text) {
                return i
            }
        }
        return -1
    }
    fun selectSpinnerItemByText(text: String?) {
        val index = getSpinnerIndexByText(text)
        if (index != -1) {
            binding.dataGYMAssociated.setSelection(index)
        }
    }


    private fun dataRecovery() {
        db = FirebaseFirestore.getInstance()
        db.collection("user").document(email).get().addOnSuccessListener {
            binding.dataName.setText(it.get("name") as String?)
            binding.dataLastName.setText(it.get("las_name") as String?)
            binding.dataAddress.setText(it.get("address") as String?)
            binding.dataBirthdate.setText(toDateBirthdate(it.get("birthdate") as Timestamp?))
            selectRadioButtonByText(it.get("gender") as String?)
            selectSpinnerItemByText(it.get("associatedGym") as String?)
        }
    }

    fun getSelectedRadioButtonText(): String? {
        val checkedButtonId = binding.radioGroup.checkedRadioButtonId
        return if (checkedButtonId != -1) {
            val checkedButton = binding.radioGroup.findViewById<RadioButton>(checkedButtonId)
            checkedButton.text.toString()
        } else {
            ""
        }
    }
    fun getSelectedSpinnerItemText(): String? {
        return if (binding.dataGYMAssociated.selectedItem != null) {
            binding.dataGYMAssociated.selectedItem.toString()
        } else {
            ""
        }
    }


    private fun createUser() {

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val date = calendar.time
        db = FirebaseFirestore.getInstance()
        db.collection("user").document(email).set(
            hashMapOf(
            "name" to binding.dataName.text.toString(),
            "las_name" to binding.dataLastName.text.toString(),
            "address" to binding.dataAddress.text.toString(),
            "lastRenovation" to date,
            "asset" to false,
            "birthdate" to dateBirthdate,
            "gender" to getSelectedRadioButtonText(),
            "associatedGym" to getSelectedSpinnerItemText()
            )
        )
    }

    private fun getDataUser() {
        var bundle=intent.extras
        email = bundle?.getString("email").toString()
        password = bundle?.getString("email").toString()
        //create = bundle?.getString("email").toBoolean()
    }

    private fun uploadImage() {


        val storageReference = FirebaseStorage.getInstance().getReference("user/$email")

        storageReference.putFile(imageUri).
                addOnSuccessListener {
                    binding.image.setImageURI(null)

                }.addOnFailureListener{

        }

    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK){
            Log.d("TAG", "llegue dentro");
            imageUri = data?.data!!
            binding.image.setImageURI(imageUri)
        }
    }

    private fun OptionsGYMAssociated() {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.dataGYMAssociated.adapter=adapter
    }

    private fun showDatePickerDialog() {
        val datePicker = DatePickerFragment{day , month, year -> onDateSelected(day, month, year)}
        /*
        Como la clase que inicializamos no sabe que valores es cada dato que
        le pasamos lo ponemos entre parentesis indicando cada valor exacto
        que le pasamos
         */
        datePicker.show(supportFragmentManager, "datePicket")

    }

    fun onDateSelected(day:Int, month:Int, year:Int){
        binding.dataBirthdate.setText("$day/$month/$year")
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)

        dateBirthdate=calendar.time
    }

    //funcion para registar el usuario
    private fun register(email: String, password: String) {
        auth = Firebase.auth
        auth.createUserWithEmailAndPassword(email,password)
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