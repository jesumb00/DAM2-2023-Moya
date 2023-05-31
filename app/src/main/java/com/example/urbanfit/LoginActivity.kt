package com.example.urbanfit

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    //lateinit significa que vamos a inicializarla posteriormente no en la declaracion
    //var significa que es variable, val seria para constante
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var loginGoRegisterButton: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        loginEmail=findViewById(R.id.loginEmail)
        loginPassword=findViewById(R.id.loginPassword)
        loginButton=findViewById(R.id.loginButtton)
        loginGoRegisterButton=findViewById(R.id.loginGoRegisterButton)

        loginButton.setOnClickListener {
            val email = loginEmail.text.toString()
            val password = loginPassword.text.toString()
            if( checkEmpty(email,password)){
                login(email,password)
            }
        }
        loginGoRegisterButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){task ->
                if(task.isSuccessful){
                    startActivity(Intent(this, MainActivity::class.java)
                        .putExtra("email", loginEmail.text.toString())
                        .putExtra("password", loginPassword.text.toString()))
                    finish()
                }else{
                    seeMessageRepeatReservationShow("Asegurate que te registraste previamente")
                }
            }
    }

    private fun checkEmpty(email: String, password: String): Boolean {
        val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+\$") // Patrón para validar el formato del email
        val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+\$") // Patrón para validar la contraseña

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
            seeMessageRepeatReservationShow("La contraseña debe contener al menos una letra minúscula, una letra mayúscula y un número")
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