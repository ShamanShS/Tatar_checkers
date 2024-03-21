package com.shamanshs.tatar_checkers.engine

class GameInfo {
    var typeGame = ""
    var moveChecker = MutableList(5, {0})
    var turn = 1
    var id = ""
    var field: MutableList<Int> = MutableList(64) { 0}
    var blackCount: Int = 0
    var whiteCount: Int = 0
}