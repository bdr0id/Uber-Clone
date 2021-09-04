package co.ke.snilloc.uberclone

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible

class MobileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile)

        //set onclick listener on the btn for next
        val button: Button = findViewById(R.id.MobileNextButton)
        val userNumber : TextView = findViewById(R.id.MobileNumberEditText)
        val mobileError : TextView = findViewById(R.id.MobileErrorTextView)

        button.setOnClickListener {
            if (userNumber.text.isEmpty()){
                userNumber.requestFocus()
                mobileError.visibility = View.VISIBLE
            }else{
                val intent = Intent(this, MobileVerifyActivity::class.java)
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