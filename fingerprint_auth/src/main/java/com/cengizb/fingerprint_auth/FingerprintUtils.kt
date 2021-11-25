@file:Suppress("SpellCheckingInspection", "KDocUnresolvedReference")

package com.cengizb.fingerprint_auth

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.hardware.fingerprint.FingerprintManagerCompat

/**
 * Created by cengizb on 30.05.2019
 *
 * Util class.
 *
 * @author [cengizb](https://github.com/cengizbayrak)
 */
object FingerprintUtils {
    /**
     * Open "Security" settings screen of device
     *
     * @param context caller context
     */
    fun openSecuritySettings(context: Context) {
        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
        context.startActivity(intent)
    }

    /**
     * Check if device has hardware support for fingerprint scanning
     *
     * @param context caller context
     * @return true if device has hardware support
     */
    fun hardwareSupported(context: Context): Boolean {
        return FingerprintManagerCompat.from(context).isHardwareDetected
    }

    /**
     * Check if any enrolled fingerprint exists in device
     *
     * @param context caller context
     * @return true if device has at least one enrolled fingerprint
     */
    fun fingerprintEnrolled(context: Context): Boolean {
        return FingerprintManagerCompat.from(context).hasEnrolledFingerprints()
    }
}
