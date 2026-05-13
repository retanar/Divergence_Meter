package retanar.divergence

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import retanar.divergence.databinding.ActivityFullscreenMeterBinding
import retanar.divergence.logic.DivergenceMeter
import retanar.divergence.util.DI

class FullscreenMeterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFullscreenMeterBinding

    private lateinit var tubes: Array<ImageView>
    private val tubeStates = arrayOf(0, 0, 0, 0, 0, 0, 0, 0)
    private val tubeImages = arrayOf(
        R.drawable.nixie0,
        R.drawable.nixie1,
        R.drawable.nixie2,
        R.drawable.nixie3,
        R.drawable.nixie4,
        R.drawable.nixie5,
        R.drawable.nixie6,
        R.drawable.nixie7,
        R.drawable.nixie8,
        R.drawable.nixie9,
        R.drawable.nixie_minus,
        R.drawable.nixie_dot,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenMeterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideBars()
        setupViews()
    }

    private fun setupViews() {
        tubes = arrayOf(
            binding.tube0,
            binding.tube1,
            binding.tube2,
            binding.tube3,
            binding.tube4,
            binding.tube5,
            binding.tube6,
            binding.tube7,
        )

        // Put current divergence as a start value
        val div = DI.preferences.getDivergenceOrCreate()
        val digits = DivergenceMeter.splitIntegerToDigits(div.intValue)
        for (i in 0..<(tubes.size - 2)) {
            setTube(i, digits[i])
        }
        setTube(tubes.size - 2, tubeImages.indexOf(R.drawable.nixie_dot))
        setTube(tubes.size - 1, digits.last())
    }

    private fun setTube(index: Int, state: Int) {
        tubeStates[index] = state
        tubes[index].setImageResource(tubeImages[state])
    }

    private fun hideBars() {
        with(WindowCompat.getInsetsController(window, window.decorView)) {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}
