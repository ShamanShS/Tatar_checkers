package com.shamanshs.tatar_checkers

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import com.shamanshs.tatar_checkers.databinding.ActivityMainBinding
import com.shamanshs.tatar_checkers.engine.Board
import android.animation.ObjectAnimator
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shamanshs.tatar_checkers.engine.Board.id
import com.shamanshs.tatar_checkers.engine.Board.typeGame
import com.shamanshs.tatar_checkers.engine.GameInfo
import kotlin.random.Random
import kotlin.random.nextInt


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.buttonSoloGame.setOnClickListener { startGame() }
        binding.buttonOnlineGame.setOnClickListener { onlineGame() }
        binding.buttonCreateGame.setOnClickListener { createGame() }
        binding.buttonJoinGame.setOnClickListener { joinGame() }
        Thread{
            while (true) {
                Thread.sleep(100)
                binding.gameField.invalidate()
            }
        }.start()
    }

    private fun startGame() {
        Board.fillMapABoard()
        typeGame = "P2P"
        startActivity(Intent(this, GameActivity::class.java))
    }

    fun onlineGame(){
        if (binding.buttonCreateGame.visibility == View.INVISIBLE) {
            binding.buttonCreateGame.visibility = View.VISIBLE
            binding.buttonJoinGame.visibility = View.VISIBLE
            binding.idText.visibility = View.VISIBLE
        }
        else{
            binding.buttonCreateGame.visibility = View.INVISIBLE
            binding.buttonJoinGame.visibility = View.INVISIBLE
            binding.idText.visibility = View.INVISIBLE
        }
    }


    fun createGame() {
        Board.fillMapABoard()
        typeGame = "HostGame"
        startActivity(Intent(this, GameActivity::class.java))
    }

    fun joinGame() {
        val gameId =binding.idText.text.toString()
        if (gameId.isEmpty()) {
            binding.idText.error = "Нужен id"
            return
        }
        Firebase.firestore.collection("Games")
            .document(gameId)
            .get()
            .addOnSuccessListener {
                val dataGame = it?.toObject(GameInfo::class.java)
                if (dataGame == null){
                    binding.idText.error = "Нужен верный id"
                }
                else{
                    Board.fillMapABoard()
                    typeGame = "GameStart"
                    id = gameId.toInt()
                    startActivity(Intent(this, GameActivity::class.java))
                }
            }
    }




}