package retanar.divergence.util

import android.util.Log
import retanar.divergence.BuildConfig

/** Another attempt to (over)engineer logging
 *
 * [logd] was specifically made to print only for Debug builds.
 * All `msg` parameters are "lazy", even though they are always executed, except in [logd].
 */

private const val DEFAULT_TAG = "DEBUGLOG"

fun logd(tag: String = DEFAULT_TAG, msg: () -> String) {
    if (BuildConfig.DEBUG) {
        Log.d(tag, msg())
    }
}

fun loge(tag: String = DEFAULT_TAG, msg: () -> String) {
    Log.e(tag, msg())
}
