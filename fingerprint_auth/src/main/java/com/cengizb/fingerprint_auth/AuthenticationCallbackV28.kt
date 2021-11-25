@file:Suppress("SpellCheckingInspection", "KDocUnresolvedReference")

package com.cengizb.fingerprint_auth

import android.annotation.TargetApi
import android.hardware.biometrics.BiometricPrompt
import android.os.Build

/**
 * Created by cengizb on 30.05.2019
 *
 * Converts [BiometricPrompt.AuthenticationCallback] into [AuthenticationCallback] for Android version P and above.
 *
 * @author [cengizb](https://github.com/cengizbayrak)
 */
@TargetApi(Build.VERSION_CODES.P)
internal class AuthenticationCallbackV28
/**
 * Public constructor.
 *
 *
 * @param callback [AuthenticationCallback] to fire events for fingerprint authentication.
 */
    (
    /**
     * [AuthenticationCallback] implemented by caller.
     */
    private val callback: AuthenticationCallback
) :
    BiometricPrompt.AuthenticationCallback() {

    /**
     * @see BiometricPrompt.AuthenticationCallback.onAuthenticationError
     */
    override fun onAuthenticationError(code: Int, error: CharSequence) {
        super.onAuthenticationError(code, error)

        when (code) {
            // fingerprint scan is canceled by negative/cancel button
            BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED -> callback.authenticationCanceledByUser()
            // no fingerprint hardware of device
            BiometricPrompt.BIOMETRIC_ERROR_HW_NOT_PRESENT, BiometricPrompt.BIOMETRIC_ERROR_HW_UNAVAILABLE -> callback.fingerprintAuthenticationNotSupported()
            // no enrolled fingerprints
            BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS -> callback.noEnrolledFingerprints()
            // any other unrecoverable error
            else -> callback.onAuthenticationError(code, error)
        }
    }


    /**
     * @see BiometricPrompt.AuthenticationCallback.onAuthenticationFailed
     */
    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()

        callback.onAuthenticationFailed()
    }

    /**
     * @see BiometricPrompt.AuthenticationCallback.onAuthenticationHelp
     */
    override fun onAuthenticationHelp(code: Int, help: CharSequence) {
        super.onAuthenticationHelp(code, help)

        callback.onAuthenticationHelp(code, help)
    }

    /**
     * @see BiometricPrompt.AuthenticationCallback.onAuthenticationSucceeded
     */
    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)

        callback.onAuthenticationSucceeded()
    }
}
