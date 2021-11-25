package com.cengizb.fingerprint_auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_secure.*


/**
 * Test activity
 */
class SecureActivity : AppCompatActivity(), View.OnClickListener, AuthenticationCallback {

    companion object {
        private const val Finger = 1
        private const val Pin = 2

        @IntDef(Finger, Pin)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Mode

        private const val TAG = "SecureActivity"
    }

    @Mode
    private var mode: Int = Finger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_secure)

        authFingerprint()
        authenticate_btn.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        Log.d(TAG, "onClick: $v")
        when (v.id) {
            R.id.authenticate_btn -> {
                when (mode) {
                    Pin -> {
                        startActivity(Intent(this@SecureActivity, PinAuthenticationActivity::class.java))
                        authFingerprint()
                    }
                    Finger -> {
                        authDialog()
                    }
                }
            }
        }
    }

    override fun fingerprintAuthenticationNotSupported() {
        // Device doesn't support fingerprint authentication.
        // Switch to alternate authentication method.

        val text: CharSequence = getString(R.string.error_fingerprints_not_supported)
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        authPin()
    }

    override fun noEnrolledFingerprints() {
        // User has no fingerprint enrolled.
        // Redirecting to the settings.
        val text: CharSequence = getString(R.string.error_no_fingerprints_enrolled)
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        FingerprintUtils.openSecuritySettings(this)
    }

    override fun onAuthenticationError(code: Int, error: CharSequence?) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        // Unrecoverable error.
        // Switch to alternate authentication method.
        authPin()
    }

    override fun onAuthenticationHelp(code: Int, help: CharSequence?) {
        // Authentication process has some warning.
        // Handle it if you want.
    }

    override fun authenticationCanceledByUser() {
        val text: CharSequence = getString(R.string.error_auth_canceled_by_user)
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onAuthenticationSucceeded() {
        // Authentication success
        // You user is now authenticated.
        startActivity(Intent(this, AuthenticationSuccessActivity::class.java))
    }

    override fun onAuthenticationFailed() {
        // Authentication failed.
        // Fingerprint scanning is still running.
        val shakeAnim = AnimationUtils.loadAnimation(applicationContext, R.anim.shake)
        authentication_iv.startAnimation(shakeAnim)
    }

    private fun authDialog() {
        FingerprintDialogBuilder(this)
            .title(R.string.fingerprint_dialog_title)
            .subtitle(R.string.fingerprint_dialog_subtitle)
            .description(R.string.fingerprint_dialog_description)
            .negativeButtonTitle(R.string.fingerprint_dialog_button_title)
            .show(supportFragmentManager, this)

        /*FingerprintDialogBuilder(this)
                .title(R.string.fingerprint_dialog_title)
                .subtitle(R.string.fingerprint_dialog_subtitle)
                .description(R.string.fingerprint_dialog_description)
                .negativeButtonTitle(R.string.fingerprint_dialog_button_title)
                .show(supportFragmentManager, object : AuthenticationCallback {
                    override fun noEnrolledFingerprints() {
                    }

                    override fun onAuthenticationError(code: Int, error: CharSequence?) {
                    }

                    override fun onAuthenticationHelp(code: Int, help: CharSequence?) {
                    }

                    override fun onAuthenticationSucceeded() {
                    }

                    override fun onAuthenticationFailed() {
                    }

                    override fun fingerprintAuthenticationNotSupported() {
                    }

                    override fun authenticationCanceledByUser() {
                    }
                })*/
    }

    private fun authFingerprint() {
        mode = Finger
        authenticate_btn.text = getString(R.string.authenticate_using_fingerprint)
    }

    private fun authPin() {
        mode = Pin
        authenticate_btn.text = getString(R.string.pin_authentication_btn_tite)
    }
}
