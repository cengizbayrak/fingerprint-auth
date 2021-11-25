@file:Suppress("DEPRECATION", "KDocUnresolvedReference")

package com.cengizb.fingerprint_auth

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.hardware.fingerprint.FingerprintManager
import android.os.*
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey

/**
 * Created by cengizb on 30.05.2019
 *
 * Dialog that acts as backport of [android.hardware.fingerprint.FingerprintDialog] for android version below P.
 *
 * @author [cengizb] (https://github.com/cengizbayrak)
 */
@TargetApi(Build.VERSION_CODES.M)
class FingerprintDialogCompatV23 : androidx.fragment.app.DialogFragment() {

    // activity context
    private lateinit var mContext: Context

    // for fingerprint authentication key
    private var keyStore: KeyStore? = null

    private var cipher: Cipher? = null

    // fingerprint scan is running
    private var isScanning = false
    /**
     * [android.widget.TextView] to display the fingerprint scanner status and errors.
     */
    private var statusText: AppCompatTextView? = null

    // notify caller about authentication status
    private lateinit var callback: AuthenticationCallback

    // cancellation signal for fingerprint authentication
    private var cancellationSignal: CancellationSignal? = null

    private var statusTextRunnable: Runnable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val li = LayoutInflater.from(context)
        return li.inflate(R.layout.fingerprint_compat_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments == null) throw IllegalStateException(getString(R.string.argument_error))

        // set title
        if (arguments!!.containsKey(ARG_TITLE)) {
            val titleTv = view.findViewById<AppCompatTextView>(R.id.title_tv)
            titleTv.text = arguments!!.getString(ARG_TITLE)
            titleTv.isSelected = true
        } else {
            throw IllegalStateException(getString(R.string.title_error))
        }

        // set subtitle
        if (arguments!!.containsKey(ARG_SUBTITLE)) {
            val subtitleTv = view.findViewById<AppCompatTextView>(R.id.subtitle_tv)
            subtitleTv.text = arguments!!.getString(ARG_SUBTITLE)
        } else {
            throw IllegalStateException(getString(R.string.subtitle_error))
        }

        // set description
        if (arguments!!.containsKey(ARG_DESCRIPTION)) {
            val descriptionTv = view.findViewById<AppCompatTextView>(R.id.description_tv)
            descriptionTv.text = arguments!!.getString(ARG_DESCRIPTION)
        } else {
            throw IllegalStateException(getString(R.string.description_error))
        }

        // set negative/cancel button text
        val button = view.findViewById<AppCompatButton>(R.id.negative_btn)
        if (arguments!!.containsKey(ARG_NEGATIVE_BUTTON_TITLE)) {
            button.text = arguments!!.getString(ARG_NEGATIVE_BUTTON_TITLE)
        }
        button.setOnClickListener {
            // close dialog
            closeDialog()
        }

        // set application drawable
        try {
            val appIcon = view.findViewById<AppCompatImageView>(R.id.app_icon_iv)
            appIcon.setImageDrawable(getApplicationIcon(mContext))
        } catch (e: Exception) {
            throw IllegalStateException(e.message)
        }

        //Status text.
        statusText = view.findViewById(R.id.fingerprint_status_tv)
    }

    override fun onStart() {
        super.onStart()
        val window = dialog.window ?: return

        //Display the dialog full width of the screen
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(
            resources.displayMetrics.widthPixels,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        //Display the at the bottom of the screen
        val wlp = window.attributes
        wlp.gravity = Gravity.BOTTOM
        wlp.windowAnimations = R.style.DialogAnimation
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = wlp
    }

    override fun onResume() {
        super.onResume()

        //Check if the device has fingerprint supported hardware.
        if (FingerprintUtils.hardwareSupported(mContext)) {
            //Device has supported hardware. Start fingerprint authentication.
            startAuth()
        } else {
            callback.fingerprintAuthenticationNotSupported()
            closeDialog()
        }
    }

    override fun onPause() {
        super.onPause()
        stopAuthIfRunning()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
    }

    override fun onDetach() {
        super.onDetach()
        stopAuthIfRunning()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAuthIfRunning()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // No call for super(). Bug on API Level > 11.
        // https://stackoverflow.com/a/10261449
    }

    /**
     * Set the [AuthenticationCallback] for notifying the status of fingerprint authentication.
     * Application must have to call [.createDialog].
     *
     * @param callback [AuthenticationCallback]
     */
    fun setAuthenticationCallback(callback: AuthenticationCallback) {
        this.callback = callback
    }

    /**
     * Generate authentication key.
     *
     * @return true if the key generated successfully.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun generateKey(): Boolean {
        keyStore = null
        val keyGenerator: KeyGenerator

        //Get the instance of the key store.
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        } catch (e: NoSuchAlgorithmException) {
            return false
        } catch (e: NoSuchProviderException) {
            return false
        } catch (e: KeyStoreException) {
            return false
        }

        //generate key.
        try {
            keyStore!!.load(null)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build()
            )
            keyGenerator.generateKey()

            return true
        } catch (e: NoSuchAlgorithmException) {
            return false
        } catch (e: InvalidAlgorithmParameterException) {
            return false
        } catch (e: CertificateException) {
            return false
        } catch (e: IOException) {
            return false
        }

    }

    private val cryptoObject: FingerprintManager.CryptoObject?
        @TargetApi(Build.VERSION_CODES.M)
        get() = if (cipherInit()) FingerprintManager.CryptoObject(cipher!!) else null

    /**
     * Initialize the cipher.
     *
     * @return true if the initialization is successful.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun cipherInit(): Boolean {
        val isKeyGenerated = generateKey()

        if (!isKeyGenerated) return false

        try {
            cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7
            )
        } catch (e: NoSuchAlgorithmException) {
            return false
        } catch (e: NoSuchPaddingException) {
            return false
        }

        try {
            keyStore!!.load(null)
            val key = keyStore!!.getKey(KEY_NAME, null) as SecretKey
            cipher!!.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: KeyStoreException) {
            return false
        } catch (e: CertificateException) {
            return false
        } catch (e: UnrecoverableKeyException) {
            return false
        } catch (e: IOException) {
            return false
        } catch (e: NoSuchAlgorithmException) {
            return false
        } catch (e: InvalidKeyException) {
            return false
        }

    }

    /**
     * Start the finger print authentication by enabling the finger print sensor.
     * Note: Use this function in the onResume() of the activity/fragment. Never forget to call
     * [.stopAuthIfRunning] in onPause() of the activity/fragment.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun startAuth() {
        if (isScanning) stopAuthIfRunning()
        val fpm = mContext.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager

        //Cannot access the fingerprint manager.

        //No fingerprint enrolled.
        if (!fpm.hasEnrolledFingerprints()) {
            callback.noEnrolledFingerprints()
            return
        }

        val co = cryptoObject
        if (co != null) {
            val authCallback = object : FingerprintManager.AuthenticationCallback() {
                override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
                    displayStatusText(errString.toString(), true)

                    when (errMsgId) {
                        FingerprintManager.FINGERPRINT_ERROR_CANCELED, FingerprintManager.FINGERPRINT_ERROR_USER_CANCELED -> callback.authenticationCanceledByUser()
                        FingerprintManager.FINGERPRINT_ERROR_HW_NOT_PRESENT, FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE -> callback.fingerprintAuthenticationNotSupported()
                        else -> callback.onAuthenticationError(errMsgId, errString)
                    }
                }

                override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
                    displayStatusText(helpString.toString(), false)
                    callback.onAuthenticationHelp(helpMsgId, helpString)
                }

                override fun onAuthenticationFailed() {
                    displayStatusText(getString(R.string.fingerprint_not_recognised), false)
                    callback.onAuthenticationFailed()
                }

                override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
                    callback.onAuthenticationSucceeded()
                    closeDialog()
                }
            }

            cancellationSignal = CancellationSignal()

            fpm.authenticate(
                co,
                cancellationSignal,
                0,
                authCallback,
                Handler(Looper.getMainLooper())
            )
        } else {
            //Cannot access the secure keystore.
            callback.fingerprintAuthenticationNotSupported()
            closeDialog()
        }
    }

    /**
     * Stop the finger print authentication.
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private fun stopAuthIfRunning() {
        if (statusTextRunnable != null) {
            Handler().removeCallbacks(statusTextRunnable)
            statusTextRunnable = null
        }

        if (cancellationSignal != null) {
            isScanning = false
            cancellationSignal!!.cancel()
            cancellationSignal = null
        }
    }

    private fun closeDialog() {
        stopAuthIfRunning()
        dismiss()
    }

    /**
     * Display the text in the [.statusText] for 1 second.
     *
     * @param status  Status text to display.
     * @param dismiss True if the dialog should dismiss after status text displayed.
     */
    private fun displayStatusText(status: String, dismiss: Boolean) {
        statusText!!.text = status
        statusTextRunnable = Runnable {
            val dialog = dialog
            if (dialog != null && dialog.isShowing) {
                statusText!!.text = ""
                if (dismiss) closeDialog()
            }
        }
        Handler().postDelayed(statusTextRunnable, 1000 /* 1 seconds */)
    }

    /**
     * Get the application icon.
     *
     * @param context [Context] of the caller.
     * @return [Drawable] icon of the application.
     * @throws PackageManager.NameNotFoundException If the package npt found.
     */
    @Throws(PackageManager.NameNotFoundException::class)
    private fun getApplicationIcon(context: Context): Drawable {
        try {
            return context.packageManager.getApplicationIcon(context.packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            throw e
        }

    }

    companion object {
        private val KEY_NAME = UUID.randomUUID().toString()

        // keys of arguments
        private const val ARG_TITLE = "arg_title"
        private const val ARG_SUBTITLE = "arg_subtitle"
        private const val ARG_NEGATIVE_BUTTON_TITLE = "arg_negative_button_title"
        private const val ARG_DESCRIPTION = "arg_description"

        /**
         * Create new instance of [FingerprintDialogCompatV23].
         *
         * @param title               dialog title
         * @param subtitle            dialog subtitle of which only two lines will be displayed
         * @param description         dialog description of which only four lines will be displayed
         * @param negativeButtonTitle dialog negative/cancel button title
         * @return [FingerprintDialogCompatV23]
         */
        internal fun createDialog(
            title: String,
            subtitle: String,
            description: String,
            negativeButtonTitle: String
        ): FingerprintDialogCompatV23 {
            val fingerprintDialogCompat = FingerprintDialogCompatV23()

            // set arguments
            val bundle = Bundle()
            bundle.putString(ARG_TITLE, title)
            bundle.putString(ARG_SUBTITLE, subtitle)
            bundle.putString(ARG_DESCRIPTION, description)
            bundle.putString(ARG_NEGATIVE_BUTTON_TITLE, negativeButtonTitle)
            fingerprintDialogCompat.arguments = bundle

            return fingerprintDialogCompat
        }
    }
}
