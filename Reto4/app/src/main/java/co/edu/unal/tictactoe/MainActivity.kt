package co.edu.unal.tictactoe

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder


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
    private val DIALOG_DIFFICULTY_ID = 0
    private val DIALOG_QUIT_ID = 1
    private val DIALOG_ABOUT_ID = 2

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.options_menu, menu)
        if(menu is MenuBuilder){
            val m: MenuBuilder = menu
            m.setOptionalIconsVisible(true)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.new_game -> {
                startNewGame()
                return true
            }
            R.id.ai_difficulty -> {
                showDialog(DIALOG_DIFFICULTY_ID)
                return true
            }
            R.id.quit -> {
                showDialog(DIALOG_QUIT_ID)
                return true
            }
            R.id.about -> {
                showDialog(DIALOG_ABOUT_ID)
            }
        }
        return false
    }

    override fun onCreateDialog(id: Int): Dialog? {
        var dialog: Dialog? = null
        val builder : AlertDialog.Builder = AlertDialog.Builder(this)
        val selected: Int
        when(id){
            DIALOG_DIFFICULTY_ID -> {
                builder.setTitle((getString(R.string.difficulty_choose)))
                val levels = arrayOf<CharSequence>(getString(R.string.difficulty_easy),
                                                  getString(R.string.difficulty_harder),
                                                  getString(R.string.difficulty_expert))
                selected = when(mGame.difficultyLevel){
                    TicTacToeGame.DifficultyLevel.Easy -> {0}
                    TicTacToeGame.DifficultyLevel.Harder -> {1}
                    else -> {2}
                }
                builder.setSingleChoiceItems(levels, selected) { tDialog: DialogInterface, item: Int ->
                    tDialog.dismiss()
                    mGame.difficultyLevel = when(item){
                        0 -> {TicTacToeGame.DifficultyLevel.Easy}
                        1 -> {TicTacToeGame.DifficultyLevel.Harder}
                        else -> {TicTacToeGame.DifficultyLevel.Expert}
                    }
                    Toast.makeText(applicationContext, levels[item], Toast.LENGTH_SHORT).show()
                }
                dialog = builder.create()
            }
            DIALOG_QUIT_ID -> {
                builder.setMessage(getString(R.string.quit_question))
                    .setCancelable((false))
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        this.finish()
                    }
                    .setNegativeButton(getString(R.string.no), null)
                dialog = builder.create()
            }
            DIALOG_ABOUT_ID -> {
                val image = ImageView(this)
                image.setImageResource(R.drawable.about)
                builder.setView(image)
                builder.setPositiveButton(getString(R.string.ok),null)
                dialog = builder.create()
            }
        }
        return dialog
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