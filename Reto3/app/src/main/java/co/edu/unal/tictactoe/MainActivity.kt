package co.edu.unal.tictactoe

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var mGame: TicTacToeGame
    private lateinit var mBoardButtons: Array<Button?>
    private lateinit var mInfoTextView: TextView
    private lateinit var mHumanWinText: TextView
    private lateinit var mTiesText: TextView
    private lateinit var mCompWinText: TextView
    private var turn = true
    private var humanScore = 0
    private var ties = 0
    private var compScore = 0

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.add("New Game")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startNewGame()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBoardButtons = arrayOfNulls(TicTacToeGame.BOARD_SIZE)
        mBoardButtons[0] = findViewById(R.id.one)
        mBoardButtons[1] = findViewById(R.id.two)
        mBoardButtons[2] = findViewById(R.id.three)
        mBoardButtons[3] = findViewById(R.id.four)
        mBoardButtons[4] = findViewById(R.id.five)
        mBoardButtons[5] = findViewById(R.id.six)
        mBoardButtons[6] = findViewById(R.id.seven)
        mBoardButtons[7] = findViewById(R.id.eight)
        mBoardButtons[8] = findViewById(R.id.nine)

        mInfoTextView = findViewById(R.id.information)

        mHumanWinText = findViewById(R.id.winHuman)
        mTiesText = findViewById(R.id.ties)
        mCompWinText = findViewById(R.id.winComp)
        mHumanWinText.text = getString(R.string.humanScore, 0)
        mTiesText.text = getString(R.string.tieScore, 0)
        mCompWinText.text = getString(R.string.compScore, 0)

        mGame = TicTacToeGame()

        startNewGame()

    }

    private fun startNewGame(){
        mGame.clearBoard()

        for (i in mBoardButtons.indices){
            mBoardButtons[i]?.text = ""
            mBoardButtons[i]?.isEnabled = true
            mBoardButtons[i]?.setOnClickListener(ButtonClickListener(i))
        }
        if (turn) {
            mInfoTextView.text = getString(R.string.first_human)
            turn = !turn
        }
        else{
            computerMove()
            mInfoTextView.text = getString(R.string.turn_human)
            turn = !turn
        }

    }

    private fun gameEnd(winner: Int){
        for (button in mBoardButtons)
            button?.isEnabled = false
        when (winner) {
            1 -> {
                mInfoTextView.text = getString(R.string.result_tie)
                ties++
            }
            2 -> {
                mInfoTextView.text = getString(R.string.result_human_wins)
                humanScore++
            }
            else -> {
                mInfoTextView.text = getString(R.string.result_computer_wins)
                compScore++
            }
        }
        mHumanWinText.text = getString(R.string.humanScore, humanScore)
        mTiesText.text = getString(R.string.tieScore, ties)
        mCompWinText.text = getString(R.string.compScore, compScore)
    }

    fun setMove(player: Char, location: Int){
        mGame.setMove(player, location)
        mBoardButtons[location]?.isEnabled = false
        mBoardButtons[location]?.text = player.toString()
        if(player == TicTacToeGame.HUMAN_PLAYER)
            mBoardButtons[location]?.setTextColor(Color.rgb(0, 200, 0))
        else
            mBoardButtons[location]?.setTextColor(Color.rgb(200, 0, 0))
    }

    fun computerMove(){
        mInfoTextView.text = getString(R.string.turn_computer)
        val move: Int = mGame.computerMove()
        setMove(TicTacToeGame.COMPUTER_PLAYER, move)
    }

    inner class ButtonClickListener(i: Int) : View.OnClickListener{
        private var location = i

        override fun onClick(view: View?) {
            if (mBoardButtons[location]!!.isEnabled){
                setMove(TicTacToeGame.HUMAN_PLAYER, location)
                var winner: Int = mGame.checkForWinner()
                if(winner == 0){
                    computerMove()
                    winner = mGame.checkForWinner()
                }
                if(winner == 0)
                    mInfoTextView.text = getString(R.string.turn_human)
                else
                    gameEnd(winner)
                }
        }

    }

}