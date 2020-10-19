package co.edu.unal.tictactoe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.*

class MultiLoginActivity : AppCompatActivity() {
    lateinit var editText: EditText
    lateinit var button: Button

    var playerName = ""

    lateinit var database: FirebaseDatabase
    lateinit var playerRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_login)

        editText = findViewById(R.id.nameInput)
        button = findViewById(R.id.loginButton)

        database = FirebaseDatabase.getInstance()

        val preferences = getSharedPreferences("PREFS", 0)
        playerName = preferences.getString("playerName", "")!!
        if(playerName != ""){
            playerRef = database.getReference("players/$playerName")
            addEventListener()
            playerRef.setValue("")
        }

        button.setOnClickListener {
            playerName = editText.text.toString()
            editText.setText("")
            if(playerName != ""){
                button.text = "LOGGING IN"
                button.isEnabled = false
                playerRef = database.getReference("players/$playerName")
                addEventListener()
                playerRef.setValue("")
            }
        }
    }

    private fun addEventListener(){
        playerRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(playerName != ""){
                    val preferences = getSharedPreferences("PREFS", 0)
                    val editor = preferences.edit()
                    editor.putString("playerName", playerName)
                    editor.apply()

                    startActivity(Intent(applicationContext, RoomListActivity::class.java))
                    finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                button.text = "LOG IN"
                button.isEnabled = true
                Toast.makeText(this@MultiLoginActivity, "ERROR!", Toast.LENGTH_SHORT).show()
            }
        })
    }
}