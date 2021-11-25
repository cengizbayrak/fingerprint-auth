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
 * Codes to detect recoverable error from fingerprint authentication.
 *
 * @author [cengizb](https://github.com/cengizbayrak)
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef(
    FingerprintManager.FINGERPRINT_ACQUIRED_GOOD,
    FingerprintManager.FINGERPRINT_ACQUIRED_IMAGER_DIRTY,
    FingerprintManager.FINGERPRINT_ACQUIRED_INSUFFICIENT,
    FingerprintManager.FINGERPRINT_ACQUIRED_PARTIAL,
    FingerprintManager.FINGERPRINT_ACQUIRED_TOO_FAST,
    FingerprintManager.FINGERPRINT_ACQUIRED_TOO_SLOW
)
annotation class HelperCode
