@file:Suppress("KDocUnresolvedReference")

package com.cengizb.fingerprint_auth

/**
 * Created by cengizb on 30.05.2019
 * <p>
 * Callback contract to get results of fingerprint authentication process.
 * This defines homogeneous way of getting callbacks for the process across Android versions.
 *
 * @author [cengizb](https://github.com/cengizbayrak)
 */
interface AuthenticationCallback {

    /**
     * This will notify, whenever,
     *  * There is no finger print hardware on the device
     *  * The device is running on the android version below [android.os.Build.VERSION_CODES.M]
     *
     * Fingerprint dialog will not be displayed, and authentication will not be performed.
     *
     * Other authentication ways such as pin, password should be used.
     */
    fun fingerprintAuthenticationNotSupported()

    /**
     * This will notify, no fingerprint enrolled into phone settings.
     *
     * Fingerprint dialog will not be displayed, and authentication will not be performed.
     *
     * User should be redirected to "Settings".
     *
     * Use [FingerprintUtils.openSecuritySettings], and prompt user to enroll
     * at least one fingerprint.
     *
     * @see FingerprintUtils.openSecuritySettings
     */
    fun noEnrolledFingerprints()

    /**
     * This will notify, user cancels fingerprint authentication by clicking negative/cancel button.
     */
    fun authenticationCanceledByUser()


    /**
     * This will notify, an unrecoverable error occurs during authentication.
     *
     * Fingerprint scan will stop after this.
     *
     * @param code  code of error [ErrorCode]
     * @param error message of error
     * @see ErrorCode
     */
    fun onAuthenticationError(@ErrorCode code: Int, error: CharSequence?)

    /**
     * This will notify, a recoverable error occurs during authentication.
     *
     * Help string will be provided to give guidance to user about what goes wrong.
     * e.g. "Dirty sensor, clean it."
     *
     * Fingerprint scan will continue after this.
     *
     * @param code code of error [HelperCode]
     * @param help message of error
     * @see HelperCode
     */
    fun onAuthenticationHelp(@HelperCode code: Int, help: CharSequence?)

    /**
     * This will notify, scanned finger does not match with any enrolled finger.
     *
     * Fingerprint scan will continue after this.
     */
    fun onAuthenticationFailed()

    /**
     * This will notify, fingerprint auth is successful.
     *
     * Fingerprint scan will stop after this and dialog will be dismissed.
     */
    fun onAuthenticationSucceeded()
}
