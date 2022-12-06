package controller.controllerComponent

import controller.controllerComponent.GameStatus.GameStatus
import model.boardComponent.boardBaseImpl.{Field, Piece, Player}
import util.Observable

trait ControllerInterface extends Observable {

  def retrieveGameStatus: GameStatus

  def movePawn(row: Int, col: Int): Unit

  def boardToString: String

  def setWall(row: Int, col: Int): Unit

  def undo(): Unit

  def redo(): Unit

  def boardSize: Int

  def cell(row: Int, col: Int): Field

  def isSet(row: Int, col: Int): Boolean

  def quit(): Boolean

  def retrievePlayerAtPosition(row: Int, col: Int): Option[Player]

}
