package co.edu.unal.tictactoe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        findViewById<Button>(R.id.single_player_button).setOnClickListener(){
            startActivity(Intent(this, SinglePlayerActivity::class.java))
        }
        findViewById<Button>(R.id.multi_player_button).setOnClickListener(){
            startActivity(Intent(this, MultiLoginActivity::class.java))
        }
    }
}