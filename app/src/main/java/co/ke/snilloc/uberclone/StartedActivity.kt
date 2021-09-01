package co.ke.snilloc.uberclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat

class StartedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_started)

        //change system ui color to match current activity
        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)

        //start next activity when button clicked
        val button: Button = findViewById(R.id.StartedUberButton)
        button.setOnClickListener {
            val intent = Intent(this, MobileActivity::class.java)
            startActivity(intent)
        }
    }
}