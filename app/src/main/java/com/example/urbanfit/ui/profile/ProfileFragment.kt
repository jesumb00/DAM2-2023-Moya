package com.example.urbanfit.ui.profile

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.urbanfit.DataUser
import com.example.urbanfit.LoginActivity
import com.example.urbanfit.MainActivity
import com.example.urbanfit.databinding.FragmentProfileBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var email: String
    lateinit var password: String

    private lateinit var db: FirebaseFirestore

    lateinit var dateBirthdate : Date
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        email = (activity as MainActivity).email
        password = (activity as MainActivity).password
        dataRecovery()
        getFirebaseStorageImageReference()

        binding.dataModifyButton.setOnClickListener{
            startActivity(
                Intent(requireContext(), DataUser::class.java)
                    .putExtra("email", email)
                    .putExtra("password",password)
                    .putExtra("create", "false"))
            requireActivity().finish()
        }

        binding.dataDeleteButton.setOnClickListener{
            deleteUser()
            startActivity(
                Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        return root
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
            binding.dataBirthdate.setText(toDateBirthdate(documentSnapshot.get("birthdate") as Timestamp?))
            binding.radioGroup.setText(documentSnapshot.get("gender") as String?)
            binding.dataGYMAssociated.setText(documentSnapshot.get("associatedGym") as String?)
            active(documentSnapshot.get("isActive") as Boolean?)
        }
    }
    /**
     * Dependiendo del valor de isActive, se establece el texto "Activo" o "Inactivo" en el
     * TextView dataActive de la vista.
     * */
    private fun active(isActive: Boolean?) {
        // La variable 'isActive' es del tipo nullable Boolean, por lo que podría tener el valor null.
        // Si se intenta acceder a un valor nullable sin hacer la comprobación correspondiente, se puede producir un error.
        if (isActive != null) {
            if (isActive) {
                // Si 'isActive' es verdadero, se establece el texto "Activo" en el TextView 'dataActive' de la vista.
                binding.dataActive.setText("Activo")
            } else {
                // Si 'isActive' es falso, se establece el texto "Inactivo" en el TextView 'dataActive' de la vista.
                binding.dataActive.setText("Inactivo")
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
     * elimina el usuario
     * */
    private fun deleteUser(){
        //elimina un documento de Firestore en la colección "user" correspondiente al label dado como parámetro.
        db=FirebaseFirestore.getInstance()
        db.collection("user").document(email).delete()
        val user = FirebaseAuth.getInstance().currentUser
        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // El usuario se eliminó correctamente
                    Toast.makeText(requireContext(), "Se elimino correctamente", Toast.LENGTH_LONG).show()
                } else {
                    // Ocurrió un error al eliminar el usuario
                    Toast.makeText(requireContext(), "No ha sido posible eliminar el usuario", Toast.LENGTH_LONG).show()
                }
            }
        val storage = FirebaseStorage.getInstance()

        // Obtén la referencia al archivo que deseas borrar
        val storageRef = storage.reference.child("user/$email")

        // Borra el archivo
        storageRef.delete()

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}