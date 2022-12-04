package controller

import controller.GameStatus.*
import model.*
import util.*
import util.Event.{FIELDCHANGED, QUIT}

import scala.collection.mutable
import scala.collection.mutable.Map

class Controller(var board: Quoridorboard[Field]) extends Observable {

  private val undoManager: UndoManager = new UndoManager
  var gameStatus: GameStatus.Value = IDLE
  private var playerCount: Int = 2
  private var currentPlayer: Int = 0
  private var playerWallCount = mutable.Map(0 -> 10, 1 -> 10)

  def createEmptyBoard(size: Int): Boolean = {
    size match {
      case 2 =>
        board = BoardCreator.createBoardWith2Players()
        playerCount = 2


      case _: Int => false
    }

    notifyObservers(FIELDCHANGED)
    true
  }

  def boardToString: String = board.toString()

  def movePawn(row: Int, col: Int): Unit = {
    val player = returnPlayerById(currentPlayer)
    val oldPosition = board.returnPositionOfPlayerPawn(player).getOrElse((0, 0))
    if (board.moveIsValid(oldPosition._1, oldPosition._2, row, col))
      playerMove(row, col, player)
      cyclePlayers()
      gameStatus = MOVED
      notifyObservers(FIELDCHANGED)
    else gameStatus = ILLEGAL_MOVE

  }

  private def playerMove(row: Int, col: Int, player: Player): Unit = {

    undoManager.doStep(new MoveCommand(row, col, player, this))
  }

  private def cyclePlayers(): Unit = currentPlayer = (currentPlayer + 1) % 2


  private def returnPlayerById(playerID: Int): Player = {
    playerID match {
      case 0 => Player1()
      case 1 => Player2()
    }
  }

  def setWall(row: Int, column: Int): Unit = {
    val wallsInPossession = playerWallCount.getOrElseUpdate(currentPlayer, 0)
    if (wallsInPossession != 0)
      val player = returnPlayerById(currentPlayer)
      undoManager.doStep(new PlaceCommand(row, column, player, this))
      cyclePlayerAndSetWall(wallsInPossession)
    else gameStatus = NO_MORE_WALLS

  }

  private def cyclePlayerAndSetWall(wallsInPossession: Int): Unit = {
    playerWallCount.update(currentPlayer, wallsInPossession - 1)
    cyclePlayers()
    notifyObservers(FIELDCHANGED)
  }

  def undo(): Unit = {
    undoManager.undoStep()
    // gameStatus = UNDO
    cyclePlayers()
    notifyObservers(FIELDCHANGED)
  }

  def redo(): Unit = {
    undoManager.redoStep()
    // gameStatus = REDO
    cyclePlayers()
    notifyObservers(FIELDCHANGED)
  }

  def boardSize: Int = board.size

  // def statusText: String = GameStatus.message(gameStatus)
  def cell(row: Int, col: Int): Field = board.cell(row, col)

  def isSet(row: Int, col: Int): Boolean = board.isSet(row, col)

  def isGiven(row: Int, col: Int): Boolean = board.isGiven(row, col)

  def quit(): Unit = notifyObservers(QUIT)
}