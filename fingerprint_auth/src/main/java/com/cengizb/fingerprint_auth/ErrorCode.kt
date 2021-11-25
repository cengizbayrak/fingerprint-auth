@file:Suppress(
    "DEPRECATED_JAVA_ANNOTATION"
    , "DEPRECATION"
    , "InlinedApi"
    , "SpellCheckingInspection"
    , "KDocUnresolvedReference"
)

package com.cengizb.fingerprint_auth

import android.hardware.fingerprint.FingerprintManager
import androidx.annotation.IntDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created by cengizb on 30.05.2019
 *
 * Codes to detect unrecoverable error from fingerprint authentication.
 *
 * Fingerprint authentication will terminate once the any of these error code occurs.
 *
 * @author [cengizb](https://github.com/cengizbayrak)
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef(
    FingerprintManager.FINGERPRINT_ERROR_CANCELED,
    FingerprintManager.FINGERPRINT_ERROR_LOCKOUT,
    FingerprintManager.FINGERPRINT_ERROR_LOCKOUT_PERMANENT,
    FingerprintManager.FINGERPRINT_ERROR_NO_FINGERPRINTS,
    FingerprintManager.FINGERPRINT_ERROR_NO_SPACE,
    FingerprintManager.FINGERPRINT_ERROR_TIMEOUT,
    FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS,
    FingerprintManager.FINGERPRINT_ERROR_VENDOR
)
annotation class ErrorCode
