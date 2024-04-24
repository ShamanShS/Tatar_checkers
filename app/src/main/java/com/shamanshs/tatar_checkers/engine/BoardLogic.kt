package com.shamanshs.tatar_checkers.engine

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.shamanshs.tatar_checkers.engine.Board.blackCount
import com.shamanshs.tatar_checkers.engine.Board.chooseFigure
import com.shamanshs.tatar_checkers.engine.Board.choosingFigure
import com.shamanshs.tatar_checkers.engine.Board.id
import com.shamanshs.tatar_checkers.engine.Board.kill
import com.shamanshs.tatar_checkers.engine.Board.mapABoard
import com.shamanshs.tatar_checkers.engine.Board.mapMove
import com.shamanshs.tatar_checkers.engine.Board.mapMoveReset
import com.shamanshs.tatar_checkers.engine.Board.massacre
import com.shamanshs.tatar_checkers.engine.Board.moveChecker
import com.shamanshs.tatar_checkers.engine.Board.turn
import com.shamanshs.tatar_checkers.engine.Board.typeGame
import com.shamanshs.tatar_checkers.engine.Board.whiteCount
import com.shamanshs.tatar_checkers.engine.Board.win
import com.shamanshs.tatar_checkers.engine.Board.youColor
import kotlin.math.abs
import kotlin.math.floor
import kotlin.random.Random
import kotlin.random.nextInt

class BoardLogic() {




    fun action(i: Int, j: Int) {
        if (id != "-1" && (youColor != turn || typeGame == "HostGame")) return
        kill = canKill(turn)
        if (mapMove[i][j] == 0 && mapABoard[i][j] == 0 && !massacre) {
            Board.mapMoveReset(0)
            choosingFigure = false
        }
        else if ((!choosingFigure || mapABoard[i][j] != 0) && myTurn(i, j) && !massacre) { //Выбор фигуры
            choosingChecker(i, j)
        }
        else if (choosingFigure && mapMove[i][j] == 3){ //Шашка рубит шашку
            killCommonTurn(i, j)
            mapMoveReset(0)
            isKing(i, j)
            if (abs(mapABoard[i][j]) == 1) {
                if (!checkKillMove(i, j, mapABoard[i][j], false)) {
                    turn *= -1
                    mapMoveReset(2)
                    choosingFigure = false
                    massacre = false
                    kill = false
                } else {
                    choosingChecker(i, j)
                    massacre = true

                }
            }
            else{
                if (!checkKillKingMove(i, j, mapABoard[i][j] / 2, false)) {
                    turn *= -1
                    mapMoveReset(2)
                    choosingFigure = false
                    massacre = false
                    kill = false
                } else {
                    choosingChecker(i, j)
                    massacre = true
                }
            }

        }
        else if (choosingFigure && mapMove[i][j] == 2) { //Ход обычной фигуры
            commonTurn(i, j)
            turn *= -1
            mapMoveReset(0)
            choosingFigure = false
            isKing(i, j)
            
        }

    }

    private fun choosingChecker(i: Int, j: Int): Boolean{
        Board.mapMoveReset(0)
        val flag = choiceChecker(i, j)
        choosingFigure = true
        return flag
    }



    fun isKing(i: Int, j: Int): Boolean{
        var flag = false
        if (mapABoard[i][j] == 1 && j == 0){
            mapABoard[i][j] = 2
            flag = true
        }
        if (mapABoard[i][j] == -1 && j == 7){
            mapABoard[i][j] = -2
            flag = true
        }
        return flag
    }

    private fun myTurn(i: Int,  j: Int): Boolean {
        return (mapABoard[i][j] == turn || mapABoard[i][j] == turn * 2)
    }

    fun choiceChecker(i: Int, j: Int): Boolean {
        mapMove[i][j] = 1
        var flag = false
        chooseFigure.apply {
            this.i = i
            this.j = j
            this.type = mapABoard[i][j]
        }
        if (abs(mapABoard[i][j]) == 1) {
            flag = if (!kill) {
                checkCommonMove(i, j, mapABoard[i][j])
            } else
                false

            flag = if(checkKillMove(i, j, mapABoard[i][j], false)) {
                Board.mapMoveReset(1)
                true
            } else
                flag
        }
        else if (abs(mapABoard[i][j]) == 2) {
            flag = if (!kill) {
                checkCommonKingMove(i, j, false)
            } else
                false

            flag = if(checkKillKingMove(i, j, mapABoard[i][j] / 2, false)) {
                Board.mapMoveReset(1)
                true
            } else
                flag
        }
        return flag
    }

    fun checkCommonMove(i: Int, j: Int, color: Int): Boolean {
        var flag = false
        if (i < 7 && (j - color) < 8 && (j - color) > -1)
            if (mapABoard[i + 1][j - color] == 0) {
                mapMove[i + 1][j - color] = 2
                flag = true
            }
        if (i > 0 && (j - color) < 8 && (j - color) > -1)
            if (mapABoard[i - 1][j - color] == 0) {
                mapMove[i - 1][j - color] = 2
                flag = true
            }
        return flag
    }

    fun checkKillMove(i: Int, j: Int, color: Int, type: Boolean): Boolean {
        val vector = Array(2) { 1 }
        var flag = false
        for (x in 0..3){
            vector[x % 2] *= -1
            if (i + vector[0] * 2 in 0..7 && j + vector[1] * 2 in 0..7) {
                if (mapABoard[i + vector[0]][j + vector[1]] == -color || mapABoard[i + vector[0]][j + vector[1]] == -color * 2) {
                    if (mapABoard[i + vector[0] * 2][j + vector[1] * 2] == 0) {
                        if (type)
                            return true
                        mapMove[i + vector[0] * 2][j + vector[1] * 2] = 3
                        flag = true
                    }
                }
            }
        }
        return flag
    }

    fun checkCommonKingMove(i: Int, j: Int, type: Boolean): Boolean {
        val vector = Array(2) { 1 }
        var flag = false
        for (x in 0..3){
            var temp = true
            var coef = 1
            vector[x % 2] *= -1
            while (temp) {
                if (i + vector[0] * coef in 0..7 && j + vector[1] * coef in 0..7) {
                    if (mapABoard[i + (coef * vector[0])][j + (coef * vector[1])] == 0) {
                        if (type)
                            return true
                        mapMove[i + (coef * vector[0])][j + (coef * vector[1])] = 2
                        coef++
                        flag = true
                    }
                    else
                        temp = false
                }
                else
                    temp = false
                }
            }
        return flag
    }

    fun checkKillKingMove (i: Int, j: Int, color: Int, type: Boolean):Boolean {
        val vector = Array(2) { 1 }
        var flag = false
        for (x in 0..3) {
            var killKing = false
            var temp = true
            var coef = 1
            vector[x % 2] *= -1
            while (temp) {
                if (i + vector[0] * coef in 0..7 && j + vector[1] * coef in 0..7) {
                    if ((mapABoard[i + (coef * vector[0])][j + (coef * vector[1])] == color * -1 ||
                                mapABoard[i + (coef * vector[0])][j + (coef * vector[1])] == color * -2) && !killKing) {
                        killKing = true
                    }
                    else if (mapABoard[i + (coef * vector[0])][j + (coef * vector[1])] == 0 && killKing) {
                        if (type)
                            return true
                        mapMove[i + (coef * vector[0])][j + (coef * vector[1])] = 3
                        flag = true
                        killKing = true
                    }
                    else if (mapABoard[i + (coef * vector[0])][j + (coef * vector[1])] != 0 && killKing) {
                        temp = false
                    }
                }
                else
                    temp = false
                coef++
            }
        }
        return flag
    }



    private fun commonTurn(i: Int, j: Int) {
        mapABoard[i][j] = chooseFigure.type
        mapABoard[chooseFigure.i][chooseFigure.j] = 0
        addGlue(i, j)
    }

    private fun killCommonTurn(i: Int, j: Int) {
        mapABoard[i][j] = chooseFigure.type
        mapABoard[chooseFigure.i][chooseFigure.j] = 0
        if (abs(chooseFigure.type) == 1 )
            mapABoard[i - ((i - chooseFigure.i) / 2)][j - ((j - chooseFigure.j) / 2)] = 0
        else
            findDead(i, j, chooseFigure.i, chooseFigure.j)
        if (turn == 1) blackCount--
        else whiteCount--
        addGlue(i, j)
    }


    fun findDead(x: Int, y: Int, oldx: Int, oldy: Int) {
        val vecX = if (x > oldx) 1
        else -1
        val vecY = if (y > oldy) 1
        else -1
        var temp = true
        var i = 1
        while(temp) {
            if (mapABoard[oldx + vecX * i][oldy + vecY * i] != 0){
                mapABoard[oldx + vecX * i][oldy + vecY * i] = 0
                temp = false
            }
            i++
        }
    }



    private fun addGlue(i: Int, j: Int) {
        moveChecker[0] = i
        moveChecker[1] = j
        moveChecker[2] = chooseFigure.i
        moveChecker[3] = chooseFigure.j
        moveChecker[4] = 1
    }

    private fun canKill(color: Int): Boolean {
        for (i in 0..7) {
            for (j in 0.. 7) {
                if (mapABoard[i][j] == color){
                    if (checkKillMove(i, j, color, true)){
                        return true
                    }
                }
                else if (mapABoard[i][j] == color * 2){
                    if (checkKillKingMove(i, j, color, true)){
                        return true
                    }
                }
            }
        }
        return false
    }


    companion object {
        val WIN_BLACK = -1
        val WIN_WHITE = 1
        val WIN_NO_INFO = 0
    }



}