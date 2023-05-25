package com.example.urbanfit.ui.bookings

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.urbanfit.ClassGym
import com.example.urbanfit.R
import com.google.firebase.storage.FirebaseStorage

class AdapterClassClassGym(
    private val ctx: Context,
    /**
     * almacena la referencia al recurso de
     * diseño que se utilizará para la vista de cada elemento
     * en el ListView. La anotación @LayoutRes indica que este
     * valor debe ser una referencia a un recurso de diseño válido.
     * */
    @LayoutRes private val layoutTemplate: Int,
    private val classGym: List<ClassGym>
) : ArrayAdapter<ClassGym>(ctx, layoutTemplate, classGym), AdapterCallbackClassGym {
    private var callback: AdapterCallbackClassGym? = null
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            val inflater = LayoutInflater.from(ctx)
            itemView = inflater.inflate(layoutTemplate, parent, false)
        }

        // Obtener la referencia a los elementos de la vista
        // y establecer los datos de la clase en ellos

        val classPosition = classGym[position]


        var dataClassName = itemView?.findViewById<TextView>(R.id.dataClassName)
        var dataClassDescription = itemView?.findViewById<TextView>(R.id.dataClassDescription)
        var dataClassMonitor = itemView?.findViewById<TextView>(R.id.dataBookingDate)
        var dataClassSchedule = itemView?.findViewById<TextView>(R.id.dataClassHour)
        var dataImage = itemView?.findViewById<ImageView>(R.id.imageView)
        dataClassName?.text = classPosition.name
        dataClassDescription?.text = classPosition.description
        dataClassMonitor?.text = classPosition.monitor
        dataClassSchedule?.text = classPosition.schedule

        if (dataImage != null) {
            getFirebaseStorageImageReference(classPosition.name+".png", dataImage)
        }
        itemView?.setOnClickListener {
            // Acciones a realizar cuando se hace clic en el elemento
            // Obtener la posición del elemento en el que se hizo clic
            val clickedPosition = position
            // Realizar las acciones deseadas con la posición obtenida
            // Por ejemplo, puedes usarla para acceder al elemento correspondiente en tu lista de datos
            Toast.makeText(ctx, "Posición: $clickedPosition", Toast.LENGTH_SHORT).show()
        }

        itemView?.setOnClickListener {
            val clickedData = classGym[position]
            callback?.onItemClicked(clickedData)
        }

        return itemView!!
    }

    override fun onItemClicked(data: ClassGym) {
        TODO("Not yet implemented")
    }
    fun setAdapterCallback(callback: AdapterCallbackClassGym) {
        this.callback = callback
    }
    /**
     * se encarga de obtener la referencia al objeto de Firebase Storage donde se encuentra
     * la imagen d la clase que se quiere cargar. Luego, utiliza la librería Glide para
     * cargar la imagen y establece un listener para manejar eventos de carga.
     * */
    private fun getFirebaseStorageImageReference(name: String, imageView: ImageView) {
        val storageReference = FirebaseStorage.getInstance().getReference("class/$name")
        Toast.makeText(ctx, "class/$name", Toast.LENGTH_SHORT).show()
        Glide.with(ctx)
            .load(storageReference)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Oculta la barra de progreso si la carga falla
                    // Aquí debes reemplazar "progressBar" por el identificador correcto del ProgressBar en tu diseño
                    // binding.progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Oculta la barra de progreso cuando la carga se completa
                    // Aquí debes reemplazar "progressBar" por el identificador correcto del ProgressBar en tu diseño
                    // binding.progressBar.visibility = View.GONE
                    return false
                }
            })
            .into(imageView)
    }
}
