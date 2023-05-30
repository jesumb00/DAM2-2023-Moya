package com.example.urbanfit

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth


class RegisterActivity : AppCompatActivity() {

    private lateinit var registerEmail: EditText
    private lateinit var registerPassword: EditText
    private lateinit var registerRepeatPassword: EditText
    private lateinit var registerButton: Button
    private lateinit var registerGoLoginButton: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)



        registerEmail = findViewById(R.id.registerEmail)
        registerPassword = findViewById(R.id.registerPassword)
        registerRepeatPassword = findViewById(R.id.registerRepeatPassword)
        registerButton = findViewById(R.id.registerButton)
        registerGoLoginButton = findViewById(R.id.registerGoLoginButton)

        registerButton.setOnClickListener {
            val email = registerEmail.text.toString()
            val password = registerPassword.text.toString()
            val repeatPassword = registerRepeatPassword.text.toString()

            if (checkEmpty(email,password,repeatPassword)){
                startActivity(Intent(this, DataUser::class.java)
                    .putExtra("email", email)
                    .putExtra("password",password)
                    .putExtra("create", "true"))
                finish()
            }
        }

        registerGoLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }


    //Comprueba si los campos estan rellenos
    private fun checkEmpty(email: String, password: String, repeatPassword: String): Boolean {
        val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+\$") // Patrón para validar el formato del email
        val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}\$") // Patrón para validar la contraseña

        if (email.isEmpty()) {
            seeMessageRepeatReservationShow("El campo de correo electrónico está vacío")
            return false
        }

        if (!email.matches(emailPattern)) {
            seeMessageRepeatReservationShow("El correo electrónico no tiene un formato válido")
            return false
        }

        if (password.isEmpty()) {
            seeMessageRepeatReservationShow("El campo de contraseña está vacío")
            return false
        }
        if (!password.matches(passwordPattern)) {
            seeMessageRepeatReservationShow("La contraseña debe contener al menos 6 caracteres una letra minúscula, una letra mayúscula y un número")
            return false
        }
        if(password != repeatPassword){
            seeMessageRepeatReservationShow("Las contraseñas introducidas deben ser iguales")
            return false
        }

        return true
    }


    private fun seeMessageRepeatReservationShow(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Aviso")
        builder.setMessage(message)

        // Configurar el botón de aceptar
        builder.setNegativeButton("Aceptar", null)

        // Crear y mostrar la ventana emergente
        val dialog = builder.create()
        dialog.show()
    }
}