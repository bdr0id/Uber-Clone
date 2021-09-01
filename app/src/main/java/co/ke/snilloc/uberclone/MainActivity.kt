package co.ke.snilloc.uberclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //hide the status bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        //open getstarted activity after five seconds
        Handler().postDelayed({
            val intent = Intent(this,StartedActivity::class.java)
            startActivity(intent)
            //kill the activity
            finish()
        },5000) //delayed for 5 seconds
    }
}