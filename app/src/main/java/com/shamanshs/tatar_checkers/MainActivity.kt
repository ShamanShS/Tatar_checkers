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
import android.content.pm.ActivityInfo
import android.content.res.Resources.Theme
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.internal.ApiExceptionUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shamanshs.tatar_checkers.engine.Board.finishGame
import com.shamanshs.tatar_checkers.engine.Board.id
import com.shamanshs.tatar_checkers.engine.Board.new
import com.shamanshs.tatar_checkers.engine.Board.typeGame
import com.shamanshs.tatar_checkers.engine.GameInfo
import kotlin.random.Random
import kotlin.random.nextInt


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    lateinit var launcher: ActivityResultLauncher<Intent>
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.show()
        auth = Firebase.auth
        test()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val acc = task.getResult(ApiException::class.java)
                if (acc != null){
                    firebaseAuthithGoogle(acc.idToken!!)
                }
            } catch (e: ApiException) {
                Log.d("auth", "auth error 1")
            }
        }
        binding.buttonSoloGame.setOnClickListener { startGame() }
        binding.buttonOnlineGame.setOnClickListener { onlineGame() }
        binding.buttonCreateGame.setOnClickListener { createGame() }
        binding.buttonJoinGame.setOnClickListener { joinGame() }
        binding.singInButtom?.setOnClickListener { singInWithGoogle() }
        Thread{
            while (true) {
                Thread.sleep(300)
                binding.gameField.invalidate()
            }
        }.start()
    }

    private fun startGame() {
        new = true
        finishGame()
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
        new = true
        finishGame()
        Board.youColor = 1
        typeGame = "HostGame"
        id = Random.nextInt(1000..9999)
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
                    finishGame()
                    Board.youColor = -1
                    new = true
                    typeGame = "GameStart"
                    id = gameId.toInt()
                    startActivity(Intent(this, GameActivity::class.java))
                }
            }
    }

    private fun getClient():GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, gso)
    }

    private fun singInWithGoogle() {
        val singInClient = getClient()
        launcher.launch(singInClient.signInIntent)
    }

    private fun firebaseAuthithGoogle(idToken: String){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if(it.isSuccessful){
                Log.d("auth", "auth good")
            }
            else
                Log.d("auth", "auth error")
        }
    }

    private fun test() {
        val ab = supportActionBar
        ab?.title = "hi"
    }

}