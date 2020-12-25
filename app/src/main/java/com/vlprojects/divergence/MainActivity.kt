package com.vlprojects.divergence

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: improve design in app
        val prefs = getSharedPreferences(SHARED_FILENAME, 0)
        divergenceText.text = prefs.getInt(SHARED_DIVERGENCE, Int.MIN_VALUE).toString()

        changeDivergenceButton.setOnClickListener {
            val userDiv = userDivergence.text.toString()
            val userDivNumber = (userDiv.toDouble() * MILLION).roundToInt()

            if (userDiv.isBlank())
                return@setOnClickListener

            if (userDivNumber !in ALL_RANGE) {
                Toast.makeText(this, "Wrong value", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            with(prefs.edit()) {
                putInt(SHARED_DIVERGENCE, userDivNumber)
                apply()
            }
        }
    }
}
