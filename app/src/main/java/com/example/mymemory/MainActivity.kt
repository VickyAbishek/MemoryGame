package com.example.mymemory

import android.animation.ArgbEvaluator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mymemory.models.BoardSize
import com.example.mymemory.models.MemoryGame
import com.example.mymemory.utils.EXTRA_BOARD_SIZE
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val CREATE_CUSTOM_GAME_REQUEST_CODE: Int = 12
    }

    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView
    private lateinit var clRoot: ConstraintLayout

    private lateinit var memoryGame: MemoryGame
    private lateinit var memoryAdapter: MemoryBoardAdapter
    private var boardSize: BoardSize = BoardSize.EASY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)

        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)

//        val intent = Intent(this, CreateActivity::class.java)
//        intent.putExtra(EXTRA_BOARD_SIZE, BoardSize.EASY)
//        startActivity(intent)

        setUpBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when ( item.itemId ) {
            R.id.mn_refresh -> {
                if( memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame() ) {
                    showAlertDialog("Quit your current game", null, View.OnClickListener {
                        setUpBoard()
                    })
                } else {
                    setUpBoard()
                }
                return true
            }
            R.id.ic_new_size -> {
                showNewSizeDialog()
                return true
            }
            R.id.mn_custom -> {
                showCreationDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCreationDialog() {
        var boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        var radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        showAlertDialog("Choose new board size", boardSizeView, View.OnClickListener {
            val desiredBoardSize = when ( radioGroupSize.checkedRadioButtonId ) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                R.id.rbHard -> BoardSize.HARD
                else -> BoardSize.EASY
            }

            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            startActivityForResult(intent, CREATE_CUSTOM_GAME_REQUEST_CODE)
        })
    }

    private fun showNewSizeDialog() {
        var boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        var radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        when ( boardSize ) {
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }

        showAlertDialog("Choose new size", boardSizeView, View.OnClickListener {
            boardSize = when ( radioGroupSize.checkedRadioButtonId ) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                R.id.rbHard -> BoardSize.HARD
                else -> BoardSize.EASY
            }
            setUpBoard()
        })

    }

    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener?) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Ok"){
                _,_ -> positiveClickListener?.onClick(null)
            }.show()

    }

    private fun setUpBoard() {
        when (boardSize) {
            BoardSize.EASY -> {
                tvNumPairs.text = "Pairs: 0/4"
                tvNumMoves.text = "Easy: 4 X 2"
            }
            BoardSize.MEDIUM -> {
                tvNumPairs.text = "Pairs: 0/9"
                tvNumMoves.text = "Easy: 6 X 3"
            }
            BoardSize.HARD -> {
                tvNumPairs.text = "Pairs: 0/18"
                tvNumMoves.text = "Easy: 6 X 6"
            }
        }

        memoryGame = MemoryGame(boardSize)
        memoryAdapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListener{
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }

        } )
        rvBoard.adapter  = memoryAdapter
        rvBoard.hasFixedSize()
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    private fun updateGameWithFlip(position: Int) {

        if(memoryGame.haveWonGame()) {
            Snackbar.make(clRoot, "Allready woan", Snackbar.LENGTH_LONG).show()
            return
        }
        if(memoryGame.isCardFaceUp(position)) {
            Snackbar.make(clRoot, "Invalid move", Snackbar.LENGTH_SHORT).show()
            return
        }
        if( memoryGame.flipCard(position) ) {
            Log.i(TAG, "Pair found ${memoryGame.numPairsFound}")
            var color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.material_on_background_disabled),
                ContextCompat.getColor(this, R.color.design_default_color_primary_dark)
            ) as Int
            tvNumPairs.setTextColor(color)
            tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if( memoryGame.haveWonGame() ) {
                Snackbar.make(clRoot, "WOan", Snackbar.LENGTH_SHORT).show()
            }
        }

        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"
        memoryAdapter.notifyDataSetChanged()
    }
}

/*
Doubts
replacement for findbyid
View view!
 */