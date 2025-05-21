package retanar.divergence

import android.app.Application

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DI.initialize(applicationContext)
    }
}
