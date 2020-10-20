package co.edu.unal.tictactoe

import android.app.Activity
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*

class MultiPlayerActivity : AppCompatActivity() {
    private lateinit var mGame: TicTacToeGame
    private lateinit var mInfoTextView: TextView
    private lateinit var mMultiInfoTextView: TextView
    private var mGameOver = false
    private var mMyTurn = false
    private var mMySymbol: Char = ' '
    private var mOpponentSymbol: Char = ' '
    private var mOpponentFound = false

    private lateinit var mPrefs : SharedPreferences

    private lateinit var mBoardView: BoardView

    var playerName = ""
    var roomName = ""
    var role = ""
    var message = ""
    var opponentName = ""

    lateinit var database: FirebaseDatabase
    lateinit var messageRef: DatabaseReference

    private lateinit var mMyMediaPlayer: MediaPlayer
    private lateinit var mOpponentMediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_player)

        mInfoTextView = findViewById(R.id.information)
        mMultiInfoTextView = findViewById(R.id.multi_info)
        mGame = TicTacToeGame()
        mBoardView = findViewById((R.id.board))
        mBoardView.setGame(mGame)
        mBoardView.setOnTouchListener(MyTouchListener())


        database = FirebaseDatabase.getInstance()

        mPrefs = getSharedPreferences("PREFS", 0)
        playerName = mPrefs.getString("playerName", "")!!

        val extras = intent.extras

        if (extras != null){
            roomName = extras.getString("roomName")!!
            if(roomName == playerName){
                role = "host"
            }else{
                role = "guest"
                mOpponentFound = true
                opponentName = roomName
            }
        }
        messageRef = database.getReference("rooms/$roomName/message")
        message = "$role:-1"
        messageRef.setValue(message)
        startNewGame()
        addPosEventListener()

    }

    override fun onResume() {
        super.onResume()
        if(role == "host") {
            mMyMediaPlayer = MediaPlayer.create(applicationContext, R.raw.hum_sound)
            mOpponentMediaPlayer = MediaPlayer.create(applicationContext, R.raw.comp_sound)
        }else{
            mMyMediaPlayer = MediaPlayer.create(applicationContext, R.raw.comp_sound)
            mOpponentMediaPlayer = MediaPlayer.create(applicationContext, R.raw.hum_sound)
        }
    }

    override fun onPause() {
        super.onPause()
        mMyMediaPlayer.release()
        mOpponentMediaPlayer.release()
    }

    private fun startNewGame() {
        mGameOver = false
        if(role == "host"){
            mMyTurn = true
            mMySymbol = TicTacToeGame.HUMAN_PLAYER
            mOpponentSymbol = TicTacToeGame.COMPUTER_PLAYER
            mMultiInfoTextView.text = "You're the host"
            val opponentRef = database.getReference("rooms/$roomName/player2")
            opponentRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        opponentName = snapshot.getValue(String::class.java)!!
                        opponentRef.removeEventListener(this)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }else{
            mMyTurn = false
            mMySymbol = TicTacToeGame.COMPUTER_PLAYER
            mOpponentSymbol = TicTacToeGame.HUMAN_PLAYER
            mMultiInfoTextView.text = getString(R.string.multi_info, role, opponentName)
        }
        mGame.clearBoard()
        mBoardView.invalidate()
        mInfoTextView.text = if(mMyTurn) getString(R.string.waiting_player) else getString(R.string.turn_opponent)
    }


    private fun addPosEventListener() {
        messageRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (role == "host"){
                    if(snapshot.getValue(String::class.java)!! == "host:done"){
                        messageRef.removeEventListener(this)
                        val roomRef = database.getReference("rooms/$playerName")
                        roomRef.removeValue()
                        return
                    }else if(snapshot.getValue(String::class.java)!! == "guest:done"){
                        mOpponentFound = false
                        //database.getReference("rooms/$roomName/player2").setValue("")
                        startNewGame()
                        return
                    }
                    if (snapshot.getValue(String::class.java)!!.contains("guest:")){
                        val pos: Int = snapshot.getValue(String::class.java)!!.replace("guest:", "").toInt()
                        if(pos == -1) {
                            mOpponentFound = true
                            mInfoTextView.text = getString(R.string.turn_human)
                            mMultiInfoTextView.text = getString(R.string.multi_info, role, opponentName)
                            return
                        }
                        opponentMove(pos)
                    }
                }else{
                    if(snapshot.getValue(String::class.java)!! == "host:done"){
                        messageRef.removeEventListener(this)
                        Toast.makeText(applicationContext, "Host closed room", Toast.LENGTH_LONG).show()
                        finish()
                        return
                    }else if(snapshot.getValue(String::class.java)!! == "guest:done"){
                        messageRef.removeEventListener(this)
                        return
                    }
                    if (snapshot.getValue(String::class.java)!!.contains("host:")){
                        val pos: Int = snapshot.getValue(String::class.java)!!.replace("host:", "").toInt()
                        if(pos == -1) {mOpponentFound = true; return}
                        opponentMove(pos)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                messageRef.setValue(message)
            }
        })
    }

    fun setMove(player: Char, location: Int): Boolean{
        if(mGame.setMove(player, location)){
            mBoardView.invalidate()
            return true
        }
        return false
    }

    private fun opponentMove(pos: Int){
        if(setMove(mOpponentSymbol, pos)){
            mOpponentMediaPlayer.start()
            val winner = mGame.checkForWinner()
            if (winner != 0) {
                gameEnd(winner)
                return
            }
            mInfoTextView.text = getString(R.string.turn_human)
            mMyTurn = true
        }
    }

    private fun gameEnd(winner: Int){
        mGameOver = true
        when (winner) {
            1 -> {
                mInfoTextView.text = getString(R.string.result_tie)
            }
            2 -> {
                if(mMyTurn){
                    mInfoTextView.text = getString(R.string.result_human_wins)
                }else{
                    mInfoTextView.text = getString(R.string.result_human_loses)
                }
            }
            else -> {
                if(mMyTurn){
                    mInfoTextView.text = getString(R.string.result_human_wins)
                }else{
                    mInfoTextView.text = getString(R.string.result_human_loses)
                }
            }
        }
    }


    override fun onBackPressed() {
        message = "$role:done"
        messageRef.setValue(message)
        setResult(Activity.RESULT_CANCELED)
        mMyMediaPlayer.release()
        mOpponentMediaPlayer.release()
        Handler(Looper.getMainLooper()).postDelayed({
            super.onBackPressed()
            finish()
        }, 100)
    }

    inner class MyTouchListener : View.OnTouchListener{
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (mMyTurn && mOpponentFound && !mGameOver) {
                v?.performClick()
                val col = (event?.x?.div(mBoardView.boardCellWidth))?.toInt()
                val row = (event?.y?.div(mBoardView.boardCellHeight))?.toInt()
                val pos: Int = (row!! * 3 + col!!)
                if(setMove(mMySymbol, pos)){
                    mMyMediaPlayer.start()
                    message = "$role:$pos"
                    messageRef.setValue(message)
                    val winner = mGame.checkForWinner()
                    if (winner != 0) {
                        gameEnd(winner)
                        return true
                    }
                    mMyTurn = false
                    mInfoTextView.text = getString(R.string.turn_opponent)
                }
            }
            return true
        }
    }
}