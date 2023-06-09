package com.example.urbanfit.ui.home

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
import com.example.urbanfit.*
import com.google.firebase.storage.FirebaseStorage

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdapterClassBooking(
    private val ctx: Context,
    /**
     * almacena la referencia al recurso de
     * diseño que se utilizará para la vista de cada elemento
     * en el ListView. La anotación @LayoutRes indica que este
     * valor debe ser una referencia a un recurso de diseño válido.
     * */
    @LayoutRes private val layoutTemplate: Int,
    private val bookingGYM: List<BookingGYM>
) : ArrayAdapter<BookingGYM>(ctx, layoutTemplate, bookingGYM), AdapterCallbackBookingGym {

    private var callback: AdapterCallbackBookingGym? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            val inflater = LayoutInflater.from(ctx)
            itemView = inflater.inflate(layoutTemplate, parent, false)
        }

        // Obtener la referencia a los elementos de la vista
        // y establecer los datos de la clase en ellos

        val bookingPosition = bookingGYM[position]


        var dataBookingName = itemView?.findViewById<TextView>(R.id.dataBookingName)
        var dataBookingDate = itemView?.findViewById<TextView>(R.id.dataBookingDate)
        var dataBookingHour = itemView?.findViewById<TextView>(R.id.dataBookingHour)
        var dataBookingGYMAssociated = itemView?.findViewById<TextView>(R.id.dataBookingGYMAssociated)
        var dataImage = itemView?.findViewById<ImageView>(R.id.imageView)
        dataBookingName?.text = bookingPosition.className
        dataBookingDate?.text = getFormattedDate(bookingPosition.date)
        dataBookingHour?.text = bookingPosition.hour
        dataBookingGYMAssociated?.text = bookingPosition.associatedGym

        if (dataImage != null) {
            getFirebaseStorageImageReference(bookingPosition.className+".png", dataImage)
        }
        itemView?.setOnClickListener {
            // Acciones a realizar cuando se hace clic en el elemento
            // Obtener la posición del elemento en el que se hizo clic
            val clickedPosition = position
            // Realizar las acciones deseadas con la posición obtenida
            // Por ejemplo, puedes usarla para acceder al elemento correspondiente en tu lista de datos
        }

        itemView?.setOnClickListener {
            val clickedData = bookingGYM[position]
            callback?.onItemClicked(clickedData)
        }

        return itemView!!
    }
    fun getFormattedDate(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    fun setAdapterCallback(callback: AdapterCallbackBookingGym) {
        this.callback = callback
    }
    override fun onItemClicked(data: BookingGYM) {
        TODO("Not yet implemented")
    }

    /**
     * se encarga de obtener la referencia al objeto de Firebase Storage donde se encuentra
     * la imagen d la clase que se quiere cargar. Luego, utiliza la librería Glide para
     * cargar la imagen y establece un listener para manejar eventos de carga.
     * */
    private fun getFirebaseStorageImageReference(name: String, imageView: ImageView) {
        val storageReference = FirebaseStorage.getInstance().getReference("class/$name")
        //Toast.makeText(ctx, "class/$name", Toast.LENGTH_SHORT).show()
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
