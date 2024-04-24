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
import android.content.IntentSender
import android.content.pm.ActivityInfo
import android.content.res.Resources.Theme
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
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
import com.shamanshs.tatar_checkers.GoogleAuth.GoogleAuthUiClient
import com.shamanshs.tatar_checkers.GoogleAuth.UserData
import com.shamanshs.tatar_checkers.engine.Board.finishGame
import com.shamanshs.tatar_checkers.engine.Board.id
import com.shamanshs.tatar_checkers.engine.Board.new
import com.shamanshs.tatar_checkers.engine.Board.typeGame
import com.shamanshs.tatar_checkers.engine.GameInfo
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.sql.Time
import java.util.Calendar
import kotlin.random.Random
import kotlin.random.nextInt


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var launcher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var auth: FirebaseAuth
    private var sI = false
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (googleAuthUiClient.getSignedInUser() != null){
            test(googleAuthUiClient.getSignedInUser()!!)
        }
        else{
            singOutProfile()
        }
        launcher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == RESULT_OK) {
                lifecycleScope.launch {
                    val signInResult = googleAuthUiClient.signInWithIntent(
                        intent = it.data ?: return@launch
                    )
                    test(signInResult.data!!)
                    Log.d("b", "${signInResult.data.username}")
                }
            }
        }
        binding.buttonSoloGame.setOnClickListener { startGame() }
        binding.buttonOnlineGame.setOnClickListener { onlineGame() }
        binding.buttonCreateGame.setOnClickListener { createGame() }
        binding.buttonJoinGame.setOnClickListener { joinGame() }
        binding.singInButtom.setOnClickListener { singInWithGoogle() }
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

    private fun onlineGame(){
        if (sI) {
            if (binding.buttonCreateGame.visibility == View.INVISIBLE) {
                binding.buttonCreateGame.visibility = View.VISIBLE
                binding.buttonJoinGame.visibility = View.VISIBLE
                binding.idText.visibility = View.VISIBLE
            } else {
                binding.buttonCreateGame.visibility = View.INVISIBLE
                binding.buttonJoinGame.visibility = View.INVISIBLE
                binding.idText.visibility = View.INVISIBLE
            }
        }
        else{
            Toast.makeText(applicationContext, "Анонимусы не играют по сети, зайдите в аккаунт", Toast.LENGTH_SHORT).show()
        }
    }


    private fun createGame() {
        new = true
        finishGame()
        Board.youColor = 1
        typeGame = "HostGame"
//        id = Random.nextInt(1000..9999)
        auth.uid?.let {id = createId(it) }
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
            .collection("Info")
            .document("GameInfo")
            .get()
            .addOnSuccessListener {
                val dataGame = it?.toObject(GameInfo::class.java)
                if (dataGame == null){
                    binding.idText.error = "Нужен верный id"
                }
                else{
                    if (dataGame.typeGame != "GameStart") {
                        finishGame()
                        Board.youColor = -1
                        new = true
                        typeGame = "GameStart"
                        id = gameId
                        startActivity(Intent(this, GameActivity::class.java))
                    }
                    else {
                        binding.idText.error = "Игра уже идет"
                    }
                }
            }
    }


    private fun singInWithGoogle() {
        lifecycleScope.launch {
            if (googleAuthUiClient.getSignedInUser() != null) {
                googleAuthUiClient.signOut()
                singOutProfile()
            } else {
                val singIn = googleAuthUiClient.signIn()
                launcher.launch(
                    IntentSenderRequest.Builder(
                        singIn ?: return@launch
                    ).build()
                )
            }
        }
    }

    private fun test(userData: UserData) {
        binding.nameId.text = userData.username
        binding.singInButtom.text = "Sing Out"
        sI = true
        Thread{
            runOnUiThread {
            Picasso.get().load(userData.profilePictureUrl).resize(50, 50)
                .centerCrop().into(binding.iconAcc)
            }
        }.start()
    }

    private fun singOutProfile(){
        binding.nameId.apply {
            text = "Anonymous"
        }
        sI = false
        binding.iconAcc.setImageResource(R.drawable.anonimus)
        binding.singInButtom.text = "Sing In"
        binding.buttonCreateGame.visibility = View.INVISIBLE
        binding.buttonJoinGame.visibility = View.INVISIBLE
        binding.idText.visibility = View.INVISIBLE
    }

    private fun createId(string: String):String{
        val time = Calendar.getInstance().time
        val shaName = AeSimpleSHA1.SHA1(string)
        val shaTime = AeSimpleSHA1.SHA1(time.toString())
        var shaId = ""
        for (i in 0..2){
            val r = Random.nextInt(0..40)
            shaId += shaName[r]
            shaId += shaTime[r]
        }
        return  shaId
    }

}

