package com.cengizb.fingerprint_auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_pin_authentication.*

class PinAuthenticationActivity : AppCompatActivity() {

    private val correctPin = "1234"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_pin_authentication)

        pin_code_et.setOnPinEnteredListener {
            if (it.length == correctPin.length) {
                if (it.toString() == correctPin) {
                    startActivity(Intent(this@PinAuthenticationActivity, AuthenticationSuccessActivity::class.java))
                } else {
                    startActivity(Intent(this@PinAuthenticationActivity, AuthenticationFailActivity::class.java))
                }
            }

            finish()
        }
    }
}
