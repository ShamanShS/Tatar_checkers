package com.shamanshs.tatar_checkers

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.shamanshs.tatar_checkers.engine.Board.win

class WinDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var str = ""
        if (win == 1) str = "Белые победили"
        else str = "Черные победили"
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Game Over")
                .setMessage(str)
                .setIcon(if (win == -1) R.drawable.black_king else R.drawable.white_king)
                .setPositiveButton("Выйти") {
                        dialog, id ->  it.finish()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}