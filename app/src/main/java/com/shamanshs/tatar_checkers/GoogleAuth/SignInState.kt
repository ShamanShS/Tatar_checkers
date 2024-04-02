package com.shamanshs.tatar_checkers.GoogleAuth

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)