package retanar.divergence

import android.app.Application
import retanar.divergence.util.DI

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DI.initialize(applicationContext)
    }
}
