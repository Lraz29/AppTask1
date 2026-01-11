package com.example.apptask1

interface GameCallback {
    fun onCrash(livesLeft: Int)
    fun onGameOver()
    fun onLivesChanged(lives: Int)
    fun onVerifyCollected(count: Int)


}