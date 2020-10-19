package co.edu.unal.tictactoe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.firebase.database.*

class MultiPlayerActivity : AppCompatActivity() {
    lateinit var button: Button

    var playerName = ""
    var roomName = ""
    var role = ""
    var message = ""

    lateinit var database: FirebaseDatabase
    lateinit var messageRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_player)

        button = findViewById(R.id.pokeButton)
        button.isEnabled = false

        database = FirebaseDatabase.getInstance()

        val preferences = getSharedPreferences("PREFS", 0)
        playerName = preferences.getString("playerName", "")!!

        val extras = intent.extras

        if (extras != null){
            roomName = extras.getString("roomName")!!
            role = if(roomName == playerName){
                "host"
            }else{
                "guest"
            }
        }
        button.setOnClickListener {
            button.isEnabled = false
            message = "$role:Poked!"
            messageRef.setValue(message)
        }

        messageRef = database.getReference("rooms/$roomName/message")
        message = "$role:Poked!"
        messageRef.setValue(message)
        addRoomEventListener()
    }

    private fun addRoomEventListener() {
        messageRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
             if (role == "host"){
                 if (snapshot.getValue(String::class.java)!!.contains("guest:")){
                     button.isEnabled = true
                     Toast.makeText(this@MultiPlayerActivity, snapshot.getValue(String::class.java)!!.replace("guest:", ""), Toast.LENGTH_SHORT).show()
                 }
             }else{
                 if (snapshot.getValue(String::class.java)!!.contains("host:")){
                     button.isEnabled = true
                     Toast.makeText(this@MultiPlayerActivity, snapshot.getValue(String::class.java)!!.replace("host:", ""), Toast.LENGTH_SHORT).show()
                 }
             }
            }
            override fun onCancelled(error: DatabaseError) {
                messageRef.setValue(message)
            }
        })
    }
}