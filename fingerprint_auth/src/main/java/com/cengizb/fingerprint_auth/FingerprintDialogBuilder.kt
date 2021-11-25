@file:Suppress(
    "unused"
    , "SpellCheckingInspection"
    , "KDocUnresolvedReference", "MemberVisibilityCanBePrivate"
)

package com.cengizb.fingerprint_auth

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.text.TextUtils
import androidx.annotation.StringRes

/**
 * Created by cengizb on 30.05.2019
 *
 * Builder for fingerprint dialog. This will display dialog based on the android version.
 *
 * @author [cengizb](https://github.com/cengizbayrak)
 */
class FingerprintDialogBuilder(private val context: Context /* caller context */) {

    // dialog title
    private var title: String? = null

    // dialog subtitle
    private var subTitle: String? = null

    // dialog description
    private var description: String? = null

    // dialog negative/cancel button title
    private var buttonTitle: String? = null

    /**
     * Set dialog title. This field is required.
     *
     * @param title title string
     * @return [FingerprintDialogBuilder]
     * @see [FingerprintDialogBuilder.title]
     */
    fun title(title: String): FingerprintDialogBuilder {
        this.title = title
        return this
    }

    /**
     * Set dialog title. This field is required.
     *
     * @param title title string resource
     * @return [FingerprintDialogBuilder]
     * @see .title
     */
    fun title(@StringRes title: Int): FingerprintDialogBuilder {
        this.title = context.getString(title)
        return this
    }

    /**
     * Set dialog subtitle. This field is required.
     *
     * @param subtitle subtitle string
     * @return [FingerprintDialogBuilder]
     * @see .subtitle
     */
    fun subtitle(subtitle: String): FingerprintDialogBuilder {
        subTitle = subtitle
        return this
    }

    /**
     * Set dialog subtitle. This field is required.
     *
     * @param subtitle subtitle string resource
     * @return [FingerprintDialogBuilder]
     * @see .subtitle
     */
    fun subtitle(@StringRes subtitle: Int): FingerprintDialogBuilder {
        subTitle = context.getString(subtitle)
        return this
    }

    /**
     * Set dialog description. This field is required.
     *
     * @param description description string
     * @return [FingerprintDialogBuilder]
     * @see .description
     */
    fun description(description: String): FingerprintDialogBuilder {
        this.description = description
        return this
    }

    /**
     * Set dialog description. This field is required.
     *
     * @param description description string resource
     * @return [FingerprintDialogBuilder]
     * @see .description
     */
    fun description(@StringRes description: Int): FingerprintDialogBuilder {
        this.description = context.getString(description)
        return this
    }

    /**
     * Set dialog negative/cancel button title. Default title is "Cancel".
     *
     * @param text title string
     * @return [FingerprintDialogBuilder]
     * @see .negativeButtonTitle
     */
    fun negativeButtonTitle(text: String?): FingerprintDialogBuilder {
        buttonTitle = text
        return this
    }

    /**
     * Set dialog negative/cancel button title. Default title is "Cancel".
     *
     * @param text title string resource
     * @return [FingerprintDialogBuilder]
     * @see .negativeButtonTitle
     */
    fun negativeButtonTitle(@StringRes text: Int): FingerprintDialogBuilder {
        buttonTitle = context.getString(text)
        return this
    }

    /**
     * Build the [FingerprintDialogCompatV23].
     *
     * The dialog will be displayed for android version M and above.
     */
    fun show(manager: androidx.fragment.app.FragmentManager, callback: AuthenticationCallback) {
        // validate title
        if (TextUtils.isEmpty(title)) {
            throw IllegalArgumentException(context.getString(R.string.title_warning))
        }

        // validate subtitle
        if (TextUtils.isEmpty(subTitle)) {
            throw IllegalArgumentException(context.getString(R.string.subtitle_warning))
        }

        // validate description
        if (TextUtils.isEmpty(description)) {
            throw IllegalArgumentException(context.getString(R.string.description_warning))
        }

        if (TextUtils.isEmpty(buttonTitle)) {
            // set default button title
            buttonTitle = context.getString(android.R.string.cancel)
        }

        // check if android version supports fingerprint authentication
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callback.fingerprintAuthenticationNotSupported()
            return
        }

        // check if device has fingerprint sensor
        if (!FingerprintUtils.hardwareSupported(context)) {
            callback.fingerprintAuthenticationNotSupported()
            return
        }

        // check if there are any fingerprints enrolled
        if (!FingerprintUtils.fingerprintEnrolled(context)) {
            callback.noEnrolledFingerprints()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            showFingerprintDialog(callback)
        } else {
            val fpd = FingerprintDialogCompatV23.createDialog(title!!, subTitle!!, description!!, buttonTitle!!)
            fpd.setAuthenticationCallback(callback)
            fpd.show(manager, FingerprintDialogCompatV23::class.java.name)
        }
    }

    @SuppressLint("MissingPermission")
    @TargetApi(Build.VERSION_CODES.P)
    private fun showFingerprintDialog(callback: AuthenticationCallback) {
        val executor = context.mainExecutor
        val listener = DialogInterface.OnClickListener { _, _ -> callback.authenticationCanceledByUser() }
        val callbackV28 = AuthenticationCallbackV28(callback)

        BiometricPrompt.Builder(context)
            .setTitle(title!!)
            .setSubtitle(subTitle!!)
            .setDescription(description!!)
            .setNegativeButton(buttonTitle!!, executor, listener)
            .build()
            .authenticate(CancellationSignal(), executor, callbackV28)
    }
}
