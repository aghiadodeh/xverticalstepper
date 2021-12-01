package com.aghiadodehgithub.verticalstepper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.aghiadodeh.xstepper.Stepper
import com.aghiadodeh.xstepper.interfaces.IStepper
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val stepper: Stepper = findViewById(R.id.stepper)
        val nameInput: TextInputEditText = findViewById(R.id.name_input)
        val emailInput: TextInputEditText = findViewById(R.id.email_input)

        stepper.setListener(object : IStepper {
            override fun onStepOpening(step: Int) {
                // Toast.makeText(this@MainActivity, "onStepOpening: $step", Toast.LENGTH_LONG).show()
            }

            override fun onWaitingForOpen(step: Int) {
                // Toast.makeText(this@MainActivity, "onWaitingForOpen: $step", Toast.LENGTH_LONG).show()
                when(stepper.activeStep) {
                    0 -> {
                        if (isNameValid(nameInput.text.toString())) {
                            nameInput.error = null
                            stepper.goToNextStep()
                        } else {
                            nameInput.error = "name is required"
                        }
                    }

                    1 -> {
                        if (emailInput.text.toString().isValidEmail()) {
                            emailInput.error = null
                            stepper.goToNextStep()
                        } else {
                            emailInput.error = "email is not valid"
                        }
                    }
                }
            }

            override fun onFinished() {
                //  Toast.makeText(this@MainActivity, "onFinished", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun isNameValid(name: String): Boolean {
        return name.isNotEmpty()
    }

    fun String.isValidEmail() = isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}