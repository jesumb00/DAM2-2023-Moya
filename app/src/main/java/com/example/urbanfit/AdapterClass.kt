package com.example.urbanfit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes

class AdapterClass(
    private val ctx: Context,
    /**
     * almacena la referencia al recurso de
     * diseño que se utilizará para la vista de cada elemento
     * en el ListView. La anotación @LayoutRes indica que este
     * valor debe ser una referencia a un recurso de diseño válido.
     * */
    @LayoutRes private val layoutTemplate: Int,
    private val classGym: List<ClassGym>
) : ArrayAdapter<ClassGym>(ctx, layoutTemplate, classGym),  AdapterCallback{
    private var callback: AdapterCallback? = null
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
        var dataClassMonitor = itemView?.findViewById<TextView>(R.id.dataClassMonitor)
        var dataClassSchedule = itemView?.findViewById<TextView>(R.id.dataClassSchedule)
        dataClassName?.text = classPosition.name
        dataClassDescription?.text = classPosition.description
        dataClassMonitor?.text = classPosition.monitor
        dataClassSchedule?.text = classPosition.schedule

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
    fun setAdapterCallback(callback: AdapterCallback) {
        this.callback = callback
    }

}
