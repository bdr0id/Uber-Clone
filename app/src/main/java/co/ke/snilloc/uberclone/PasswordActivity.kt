package co.ke.snilloc.uberclone

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class PasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        val passwordForgotTextView :TextView = findViewById(R.id.PasswordForgotTextView)
        val passwordNoAccountTextView :TextView = findViewById(R.id.PasswordNoAccountTextView)
        val passwordSignInErrorTextView :TextView = findViewById(R.id.PasswordSignInErrorTextView)

        //change color
        passwordForgotTextView.setTextColor(Color.parseColor("#224952"))
        passwordNoAccountTextView.setTextColor(Color.parseColor("#224952"))
        passwordSignInErrorTextView.setTextColor(Color.parseColor("#224952"))

        val passwordNextButton :Button = findViewById(R.id.PasswordNextButton)
        passwordNextButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }
}