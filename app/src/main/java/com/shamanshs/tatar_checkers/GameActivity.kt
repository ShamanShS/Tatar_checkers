package com.shamanshs.tatar_checkers

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shamanshs.tatar_checkers.databinding.ActivityGameBinding
import com.shamanshs.tatar_checkers.engine.Board
import com.shamanshs.tatar_checkers.engine.Board.blackCount
import com.shamanshs.tatar_checkers.engine.Board.finishGame
import com.shamanshs.tatar_checkers.engine.Board.id
import com.shamanshs.tatar_checkers.engine.Board.kill
import com.shamanshs.tatar_checkers.engine.Board.mapABoard
import com.shamanshs.tatar_checkers.engine.Board.mapABoardReset
import com.shamanshs.tatar_checkers.engine.Board.move
import com.shamanshs.tatar_checkers.engine.Board.moveChecker
import com.shamanshs.tatar_checkers.engine.Board.turn
import com.shamanshs.tatar_checkers.engine.Board.typeGame
import com.shamanshs.tatar_checkers.engine.Board.whiteCount
import com.shamanshs.tatar_checkers.engine.Board.win
import com.shamanshs.tatar_checkers.engine.BoardLogic
import com.shamanshs.tatar_checkers.engine.GameInfo

class GameActivity : AppCompatActivity(), FieldView.Listener  {
    lateinit var binding : ActivityGameBinding
    val game = BoardLogic()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val str = "${game.id}"
        binding.statusTurnText?.text = str
        binding.gameField.listener = this
        turn = 1
        win = 0
        if (typeGame != "P2P") {
            if (typeGame == "GameStart") {
                game.id = id.toInt()
                binding.gameField.rotation = 180f
            }
            game.sendToDataBase()
            onChangeListener()
        }
    }



    override fun onCLick(i: Int, j: Int) {
        if (win == 0)
            game.action(i, j)
        game.isOver()
        if (win != 0) {
            val myDialogFragment = WinDialog()
            val manager = supportFragmentManager
            val transaction: FragmentTransaction = manager.beginTransaction()
            myDialogFragment.show(transaction, "dialog")
        }
        val str = "$i $j $win $kill"
        binding.statusTurnText?.text = str
        binding.gameField.update()
    }

    override fun onDestroy() {
        super.onDestroy()
        finishGame()
        Log.d("MyLog", "Destroy")
    }



    fun onChangeListener(){
        if (game.id != -1){
            Firebase.firestore.collection("Games")
                .document(game.id.toString())
                .addSnapshotListener { value, error ->
                    if (value != null){
                        val dataGame = value.toObject(GameInfo::class.java)
                        if (dataGame != null){
                            turn = dataGame.turn
                            typeGame = dataGame.typeGame
                            if (comparison(dataGame)) {
                                move = true
                                convertToGameModel(dataGame)
                                binding.gameField.update()
                            }
                        }
                    }
                }
        }
    }

    private fun comparison(dataGame: GameInfo): Boolean{
        for (i in 0..4){
            if (moveChecker[i] != dataGame.moveChecker[i])
                return true
        }
        return false
    }

    private fun convertToGameModel(dataGame: GameInfo) {
        moveChecker = dataGame.moveChecker
        blackCount = dataGame.blackCount
        whiteCount = dataGame.whiteCount
        typeGame = dataGame.typeGame
        for (i in 0..4){
            moveChecker[i] = dataGame.moveChecker[i]
        }
        for (x in 0..7){
            for (y in 0..7){
                mapABoard[x][y] = dataGame.field[(y * 8) + x]
            }
        }
    }

}