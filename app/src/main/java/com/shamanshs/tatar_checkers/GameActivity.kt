package com.shamanshs.tatar_checkers

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginTop
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.shamanshs.tatar_checkers.GoogleAuth.GoogleAuthUiClient
import com.shamanshs.tatar_checkers.GoogleAuth.UserData
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
import com.squareup.picasso.Picasso

class GameActivity : AppCompatActivity(), FieldView.Listener  {
    private lateinit var binding : ActivityGameBinding
    private val game = BoardLogic()
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }
    private var leavOpponent = false

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
        addIcon()
        if (typeGame != "P2P") {
            val str = "$id"
            binding.statusTurnText.text = "ID:" + str
        }
        else
            binding.statusTurnText.text = ""
        binding.gameField.listener = this
        if (new) {
            Board.fillMapABoard()
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
            onChangeListenerGameInfo()
            rotateIs = false
            if (youColor == -1) {
                binding.gameField.rotation = 180f
            }
            sendToDataBase()
        }
    }

    private fun addIcon(){
        if (typeGame == "P2P"){
            binding.iconAccOne.setImageResource(R.drawable.anonimus)
            binding.nameIdOne.text = "White"
            binding.iconAccTwo.setImageResource(R.drawable.anonimous_black)
            binding.nameIdTwo.text = "Black"
        }
        else {
            val user = googleAuthUiClient.getSignedInUser()
            if (user != null) {
                binding.nameIdOne.text = user.username
            }
            Thread{
                runOnUiThread {
                    if (user != null) {
                        Picasso.get().load( user.profilePictureUrl).resize(50, 50)
                            .centerCrop().into(binding.iconAccOne)
                    }
                }
            }.start()
            if (youColor == 1){
                sendToDataBaseInfoPlayerWhite()
                onChangeListenerInfoPlayer(-1)
            }
            else {
                sendToDataBaseInfoPlayerBlack()
                onChangeListenerInfoPlayer(1)
            }
        }
    }


    override fun onCLick(i: Int, j: Int) {
        isOver(turn)
        if (win == 0)
            game.action(i, j)
        binding.gameField.update()
        if (typeGame != "P2P")
            sendToDataBase()
        binding.turnView.text = if (turn == 1) "Ход белых" else "Ход черных"
    }

    override fun onDestroy() {
        super.onDestroy()
        if (win != 0)
            finishGame()
        deletePlayer(youColor)
        if (leavOpponent)
            deleteGame(youColor)
        Log.d("MyLog", "Destroy")
    }

    private fun deleteGame(color: Int){
        Firebase.firestore.collection("Games")
            .document(id.toString())
            .collection("Info")
            .document("GameInfo")
            .delete()
            .addOnSuccessListener { Log.d("Firebase delete", "DocumentSnapshot GameInfo successfully deleted!") }
            .addOnFailureListener { e -> Log.w("Firebase delete", "Error deleting document", e) }
        Firebase.firestore.collection("Games")
            .document(id.toString())
            .delete()
            .addOnSuccessListener { Log.d("Firebase delete", "DocumentSnapshot Game successfully deleted!") }
            .addOnFailureListener { e -> Log.w("Firebase delete", "Error deleting document", e) }
    }

    private fun deletePlayer(color: Int) {
        Firebase.firestore.collection("Games")
            .document(id.toString())
            .collection("Info")
            .document(if (color == 1) "InfoPlayerWhite" else "InfoPlayerBlack")
            .delete()
            .addOnSuccessListener { Log.d("Firebase delete", "DocumentSnapshot Player successfully deleted!") }
            .addOnFailureListener { e -> Log.w("Firebase delete", "Error deleting document", e) }
    }

    private fun isOver(turnCheck: Int) {
        if (kill || choosingFigure) {
            Log.d("0", "$win  ${whiteCount}")
            return
        }
        if (blackCount <= 0) {
            win = 1
            Log.d("1", "$win  ${whiteCount}")
            val myDialogFragment = WinDialog()
            val manager = supportFragmentManager
            val transaction: FragmentTransaction = manager.beginTransaction()
            myDialogFragment.show(transaction, "dialog")
            return
        }
        else if(whiteCount <= 0) {
            win = -1
            Log.d("2", "$win  ${whiteCount}")
            val myDialogFragment = WinDialog()
            val manager = supportFragmentManager
            val transaction: FragmentTransaction = manager.beginTransaction()
            myDialogFragment.show(transaction, "dialog")
            return
        }
        Log.d("3", "$win  ${whiteCount}")
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
            .collection("Info")
            .document("GameInfo")
            .set(dataGame)
    }

    private fun convertToDataModel(dataGame: GameInfo) {
        dataGame.id = id
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

    private fun onChangeListenerGameInfo(){
        if (id != "-1"){
            Firebase.firestore.collection("Games")
                .document(id.toString())
                .collection("Info")
                .document("GameInfo")
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
                            binding.turnView.text = if (turn == 1) "Ход белых" else "Ход черных"
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
        id = dataGame.id
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

    private fun onChangeListenerInfoPlayer(color: Int):Boolean {
        var flag = true
        Firebase.firestore.collection("Games")
            .document(id.toString())
            .collection("Info")
            .document(if (color == 1) "InfoPlayerWhite" else "InfoPlayerBlack")
            .addSnapshotListener { value, _ ->
                val userWhite = value?.toObject(UserData::class.java)
                if (userWhite != null) {
                    binding.nameIdTwo.text = userWhite.username
                    Thread{
                        runOnUiThread {
                            Picasso.get().load( userWhite.profilePictureUrl).resize(50, 50)
                                .centerCrop().into(binding.iconAccTwo)
                        }
                    }.start()
                }
                else {
                    binding.nameIdTwo.text = "Ожидание игрока"
                    binding.iconAccTwo.setImageResource(R.drawable.anonimous_black)
                    flag = false
                    leavOpponent = true
                }
            }
        return flag
    }



    private fun sendToDataBaseInfoPlayerBlack() {
        val user = googleAuthUiClient.getSignedInUser()
        Firebase.firestore.collection("Games")
            .document(id.toString())
            .collection("Info")
            .document("InfoPlayerBlack")
            .set(user!!)
    }

    private fun sendToDataBaseInfoPlayerWhite() {
        val user = googleAuthUiClient.getSignedInUser()
        Firebase.firestore.collection("Games")
            .document(id.toString())
            .collection("Info")
            .document("InfoPlayerWhite")
            .set(user!!)
    }

}