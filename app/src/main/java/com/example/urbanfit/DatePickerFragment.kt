package com.example.urbanfit

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment(val listener: (day:Int, month:Int, year:Int) -> Unit): DialogFragment(),
    DatePickerDialog.OnDateSetListener{
    /*
    Unit es un tipo de retorno que se utiliza para representar la ausencia
    de un valor significativo en la función listener.

    Extiende de una clase llamada DialogFragment que nos permite mostrar los
    dialogos con los que vamos a trabajar ahorrandonos trabajo.

    Le decimos que implemente una funcion con DatePickerDialog.OnDateSetListener
    */
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        /*
        Este metodo se ejecuta una vez escogida la fecha por el usuario que nos permitira llamar
        al metodo creado listener de la clase padre(dataUser) y operar ya con los datos desde la
        clase principal
         */
        listener(day,month,year)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val c = Calendar.getInstance()
        var day = c.get(Calendar.DAY_OF_MONTH)
        var month = c.get(Calendar.MONTH)
        var year = c.get(Calendar.YEAR)

        val picker = DatePickerDialog(activity as Context, R.style.datePickerTheme,this, year, month, day)
        picker.datePicker.maxDate = c.timeInMillis
        picker.datePicker.minDate = c.timeInMillis-2209836800000//70 años de milisegundos
        return picker

    }


}