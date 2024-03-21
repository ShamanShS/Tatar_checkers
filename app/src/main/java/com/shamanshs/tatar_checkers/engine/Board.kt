package com.shamanshs.tatar_checkers.engine


object Board {
    var mapABoard: Array<Array<Int>> = Array(8) { Array(8) { 0 } }
    var mapMove: Array<Array<Int>> = Array(8) { Array(8) { 0 } }
    var moveChecker = MutableList(5, {0})
    var blackCount = 0
    var whiteCount = 0
    var massacre = false
    var choosingFigure = false
    var chooseFigure: checker = checker(0, 0, 0)
    var kill = false
    var win = 0
    var typeGame = ""
    var turn = 1
    var id = -1
    var move = false

    fun fillMapABoard() {
        for (i in 0..7){
            for (j in 0..7){
                if ((i + j) % 2 == 1 && j < 3) {
                    mapABoard[i][j] = -1
                    blackCount++
                }
                if ((i + j) % 2 == 1 && j > 4) {
                    mapABoard[i][j] = 1
                    whiteCount++
                }
            }
        }
    }

    fun mapABoardReset() {
        for (x in 0..7) {
            for (y in 0..7) {
                mapABoard[x][y] = 0
            }
        }
        blackCount = 0
        whiteCount = 0
    }

    fun finishGame(){
        mapABoardReset()
        mapMoveReset(0)
        moveChecker = MutableList(5, {0})
    }


    fun mapMoveReset(state: Int) {
        for (x in 0..7) {
            for (y in 0..7) {
                if (state == 0)
                    mapMove[x][y] = 0
                else if (state == 1 && mapMove[x][y] != 3 && mapMove[x][y] != 1)
                    mapMove[x][y] = 0
                else if (state == 2 && mapMove[x][y] != 1) {
                    mapMove[x][y] = 0
                }
            }
        }
    }

}