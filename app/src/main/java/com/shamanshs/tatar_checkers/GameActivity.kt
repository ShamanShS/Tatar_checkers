package com.shamanshs.tatar_checkers

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.shamanshs.tatar_checkers.engine.Board.choosingFigure
import com.shamanshs.tatar_checkers.engine.Board.destroyGame
import com.shamanshs.tatar_checkers.engine.Board.finishGame
import com.shamanshs.tatar_checkers.engine.Board.id
import com.shamanshs.tatar_checkers.engine.Board.kill
import com.shamanshs.tatar_checkers.engine.Board.mapABoard
import com.shamanshs.tatar_checkers.engine.Board.mapABoardReset
import com.shamanshs.tatar_checkers.engine.Board.massacre
import com.shamanshs.tatar_checkers.engine.Board.move
import com.shamanshs.tatar_checkers.engine.Board.moveChecker
import com.shamanshs.tatar_checkers.engine.Board.new
import com.shamanshs.tatar_checkers.engine.Board.rotateIs
import com.shamanshs.tatar_checkers.engine.Board.turn
import com.shamanshs.tatar_checkers.engine.Board.typeGame
import com.shamanshs.tatar_checkers.engine.Board.whiteCount
import com.shamanshs.tatar_checkers.engine.Board.win
import com.shamanshs.tatar_checkers.engine.Board.youColor
import com.shamanshs.tatar_checkers.engine.BoardLogic
import com.shamanshs.tatar_checkers.engine.GameInfo

class GameActivity : AppCompatActivity(), FieldView.Listener  {
    private lateinit var binding : ActivityGameBinding
    private val game = BoardLogic()

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
        val str = "$id"
        binding.statusTurnText?.text = str
        binding.gameField.listener = this
        if (new) {
            Board.test()
            turn = 1
            win = 0
            new = false
        }
        else if (typeGame != "P2P") {
            Firebase.firestore.collection("Games")
                .document(id.toString())
                .get().addOnSuccessListener { task ->
                    val dataGame = task.toObject(GameInfo::class.java)
                    if (dataGame != null)
                        convertToGameModel(dataGame)
                }
        }
        if (typeGame != "P2P") {
            onChangeListener()
            rotateIs = false
            if (youColor == -1) {
                binding.gameField.rotation = 180f
            }
            sendToDataBase()
        }
    }



    override fun onCLick(i: Int, j: Int) {
        isOver(turn)
        if (win == 0)
            game.action(i, j)
        binding.gameField.update()
        sendToDataBase()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (win != 0)
            finishGame()
        Log.d("MyLog", "Destroy")
    }

    private fun isOver(turnCheck: Int) {
        if (kill || choosingFigure) {
            Log.d("0", "$win  ${Board.whiteCount}")
            return
        }
        if (blackCount <= 0) {
            win = 1
            Log.d("1", "$win  ${Board.whiteCount}")
            val myDialogFragment = WinDialog()
            val manager = supportFragmentManager
            val transaction: FragmentTransaction = manager.beginTransaction()
            myDialogFragment.show(transaction, "dialog")
            return
        }
        else if(whiteCount <= 0) {
            win = -1
            Log.d("2", "$win  ${Board.whiteCount}")
            val myDialogFragment = WinDialog()
            val manager = supportFragmentManager
            val transaction: FragmentTransaction = manager.beginTransaction()
            myDialogFragment.show(transaction, "dialog")
            return
        }
        Log.d("3", "$win  ${Board.whiteCount}")
        var temp = true
        for (i in 0..7){
            for(j in 0..7){
                if (mapABoard[i][j] == turnCheck){
                    if (game.checkCommonMove(i, j, mapABoard[i][j]))
                        temp = false
                    else if (game.checkKillMove(i, j, mapABoard[i][j], true))
                        temp = false
                }
                else if (mapABoard[i][j] == turnCheck * 2) {
                    if (game.checkCommonKingMove(i, j, true))
                        temp = false
                    else if (game.checkKillKingMove(i, j, mapABoard[i][j], true))
                        temp = false
                }
            }
        }
        if (temp) {
            win = -turnCheck
            val myDialogFragment = WinDialog()
            val manager = supportFragmentManager
            val transaction: FragmentTransaction = manager.beginTransaction()
            myDialogFragment.show(transaction, "dialog")
        }
        Board.mapMoveReset(0)
    }
    private fun sendToDataBase(){
        val dataGame = GameInfo()
        convertToDataModel(dataGame)
        Firebase.firestore.collection("Games")
            .document(id.toString())
            .set(dataGame)
    }

    private fun convertToDataModel(dataGame: GameInfo) {
        dataGame.id = id.toString()
        dataGame.turn = turn
        dataGame.win = win
        dataGame.blackCount = blackCount
        dataGame.whiteCount = whiteCount
        dataGame.typeGame = typeGame
        for (i in 0..4){
            dataGame.moveChecker[i] = moveChecker[i]
        }
        for (x in 0..7){
            for (y in 0..7){
                dataGame.field[(y * 8) + x] = mapABoard[x][y]
            }
        }
    }

    private fun onChangeListener(){
        if (id != -1){
            Firebase.firestore.collection("Games")
                .document(id.toString())
                .addSnapshotListener { value, _ ->
                    if (value != null){
                        val dataGame = value.toObject(GameInfo::class.java)
                        if (dataGame != null){
                            turn = dataGame.turn
                            typeGame = dataGame.typeGame
                            if (comparison(dataGame)) {
                                move = true
                                convertToGameModel(dataGame)
                            }
                            binding.gameField.update()
                            isOver(turn)
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
        id = dataGame.id.toInt()
        moveChecker = dataGame.moveChecker
        blackCount = dataGame.blackCount
        whiteCount = dataGame.whiteCount
        typeGame = dataGame.typeGame
        win = dataGame.win
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