package com.example.urbanfit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast


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

            if (password.equals(repeatPassword) && checkEmpty(email,password, repeatPassword)){
                startActivity(Intent(this, DataUser::class.java)
                    .putExtra("email", email)
                    .putExtra("password",password)
                    .putExtra("create", "true"))
                finish()
            }else{
                Toast.makeText(applicationContext, "Revisa los datos introducidos", Toast.LENGTH_LONG).show()
            }
        }

        registerGoLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }


    //Comprueba si los campos estan rellenos
    private fun checkEmpty(email: String, password:String, repeatPassword: String): Boolean{
        return email.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty()
    }
}