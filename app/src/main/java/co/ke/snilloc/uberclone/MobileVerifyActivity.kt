package co.ke.snilloc.uberclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MobileVerifyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile_verify)

        val button: Button = findViewById(R.id.VerifyNextButton)
        val userNumber:EditText = findViewById(R.id.VerifyEditTextNumber)
        val codeError :TextView = findViewById(R.id.VerifyCodeErrorTextView)
        val skipVerify: TextView = findViewById(R.id.VerifySkipTextView)

        skipVerify.setOnClickListener {
            val intent = Intent(this,PasswordActivity::class.java)
            startActivity(intent)
        }

        button.setOnClickListener {
            if (userNumber.text.isEmpty()){
                userNumber.requestFocus()
                codeError.visibility = View.VISIBLE
            }else{
                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)
            }
        }
    }
}