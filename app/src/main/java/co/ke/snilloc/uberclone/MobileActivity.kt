package co.ke.snilloc.uberclone

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MobileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile)

        //set onclick listener on the btn for next
        val button: Button = findViewById(R.id.MobileNextButton)
        button.setOnClickListener {
            val intent = Intent(this, PasswordActivity::class.java)
            startActivity(intent)
        }
        val textView :TextView = findViewById(R.id.MobileSocialTextView)
        textView.setTextColor(Color.parseColor("#0000FF"))
        textView.setOnClickListener {
            val intent = Intent(this, SocialActivity::class.java)
            startActivity(intent)
        }
    }
}