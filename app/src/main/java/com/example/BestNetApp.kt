package com.example

import android.app.Application
import android.content.Context
import com.example.data.sip.SipManager

/**
 * Holds the one process-wide SipManager.
 *
 * A linphone Core owns native resources, a network socket and the audio
 * devices, and only one registration per extension makes sense — so this must
 * not be created per ViewModel. ViewModels are recreated on configuration
 * change; a Core created alongside one would tear down and re-register the SIP
 * account every time the screen rotated, dropping any call in progress.
 */
class BestNetApp : Application() {

  override fun onCreate() {
    super.onCreate()
    instance = this
  }

  companion object {
    @Volatile
    private var instance: BestNetApp? = null

    @Volatile
    private var sip: SipManager? = null

    /**
     * Falls back to the supplied context rather than requiring `instance`,
     * so this still works if the manifest's android:name is ever dropped —
     * a silent failure that would otherwise only show up as calling not
     * working at all.
     */
    fun sipManager(context: Context): SipManager =
      sip ?: synchronized(this) {
        sip ?: SipManager((instance ?: context).applicationContext).also { sip = it }
      }
  }
}
