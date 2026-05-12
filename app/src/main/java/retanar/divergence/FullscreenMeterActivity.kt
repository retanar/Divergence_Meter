package retanar.divergence

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import retanar.divergence.databinding.ActivityFullscreenMeterBinding

class FullscreenMeterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullscreenMeterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenMeterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideBars()
    }

    private fun hideBars() {
        with(WindowCompat.getInsetsController(window, window.decorView)) {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}
