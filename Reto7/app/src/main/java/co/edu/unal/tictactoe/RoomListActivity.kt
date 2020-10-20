package co.edu.unal.tictactoe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.database.*

class RoomListActivity : AppCompatActivity() {
    lateinit var listView: ListView
    lateinit var button: Button

    var roomsList: MutableList<String> = mutableListOf()

    var playerName = ""
    var roomName = ""

    lateinit var database: FirebaseDatabase
    lateinit var roomRef: DatabaseReference
    lateinit var roomsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_list)

        database = FirebaseDatabase.getInstance()

        val preferences = getSharedPreferences("PREFS",0)
        playerName = preferences.getString("playerName", "")!!
        roomName = playerName

        listView = findViewById(R.id.roomList)
        button = findViewById(R.id.newRoomButton)

        button.setOnClickListener {
            button.text = "CREATING ROOM"
            button.isEnabled = false
            roomName = playerName
            roomRef = database.getReference("rooms/$roomName/player1")
            database.getReference("rooms/$roomName/player2").setValue("")
            addRoomEventListener()
            roomRef.setValue(playerName)
        }

        listView.setOnItemClickListener { _, _, i, _ ->
            roomName = roomsList[i]
            roomRef = database.getReference("rooms/$roomName/player2")
            addRoomEventListener()
            roomRef.setValue(playerName)
        }

        addRoomsEventListener()
    }

    private fun addRoomEventListener() {
        roomRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                button.text = "CREATE ROOM"
                button.isEnabled = true
                val intent = Intent(applicationContext, MultiPlayerActivity::class.java)
                intent.putExtra("roomName", roomName)
                startActivityForResult(intent,0)
                roomRef.removeEventListener(this)
            }

            override fun onCancelled(error: DatabaseError) {
                button.text = "CREATE ROOM"
                button.isEnabled = true
                Toast.makeText(this@RoomListActivity, "ERROR!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        finish()
    }

    private fun addRoomsEventListener(){
        roomsRef = database.getReference("rooms")
        roomsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                roomsList.clear()
                val rooms = snapshot.children as Iterable<DataSnapshot>
                for (roomSnapshot in rooms){
                    roomsList.add(roomSnapshot.key!!)
                    val adapter = ArrayAdapter<String>(this@RoomListActivity, android.R.layout.simple_list_item_1, roomsList)
                    listView.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}