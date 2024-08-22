package com.nexusdev.beautywaveadmin

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import com.nexusdev.beautywaveadmin.databinding.ActivityMainBinding
import com.nexusdev.beautywaveadmin.view.products.AddProductsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var typewriterTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var index = 0
    private val delay: Long = 150

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        typewriterTextView = findViewById(R.id.txtTitle)

        val textToDisplay = "BeautyWave \nlo mejor en maquillaje."
        typewriterEffect(textToDisplay)

        // Animation call the function
        val imgBackground = binding.imgBackground
        imgBackground.doOnLayout {
            animateFloating(imgBackground)
        }

        binding.btnInsert.setOnClickListener {
            val intent = Intent(this, AddProductsActivity::class.java)
            startActivity(intent)

        }

    }

    //animation for text like write machine
    @SuppressLint("SetTextI18n")
    private fun typewriterEffect(text: String) {
        if (index < text.length) {
            typewriterTextView.text = typewriterTextView.text.toString() + text[index]
            index++
            handler.postDelayed({ typewriterEffect(text) }, delay)
        }
    }

    //Animation for img background
    private fun animateFloating(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "translationY", -15f, 15f).apply {
            duration = 4000
            interpolator = AccelerateDecelerateInterpolator()
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }

        // Begin animation
        animator.start()
    }
}