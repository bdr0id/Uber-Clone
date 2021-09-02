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
        val userNumber : TextView = findViewById(R.id.MobileNumberEditText)

        button.setOnClickListener {
            if (userNumber.text.isEmpty()){
                userNumber.requestFocus()
                userNumber.setError("Mobile number required")
            }else{
                val intent = Intent(this, PasswordActivity::class.java)
                startActivity(intent)
            }
        }

        val textView :TextView = findViewById(R.id.MobileSocialTextView)
        textView.setTextColor(Color.parseColor("#2d71e2"))
        textView.setOnClickListener {
            val intent = Intent(this, SocialActivity::class.java)
            startActivity(intent)
        }


    }
}