package model

case class Quoridorboard[A <: Field](rows: Vector[Vector[Field]]):

  val size: Int = rows.size

  def this(size: Int) = this(Vector.tabulate(size * 2 - 1, size * 2 - 1) { (row, col) =>
    (row % 2, col % 2) match {
      case (0, 0) => new PieceField(None)
      case (_, _) => new WallField(None)
    }
  })

  def row(row: Int): Seq[Field] = rows(row)

  def isSet(row: Int, col: Int): Boolean = rows(row)(col).isSet

  def isGiven(row: Int, col: Int): Boolean = rows(row)(col).isGiven

  def placeWall(row: Int, col: Int, player: Player): Either[BoardError, Quoridorboard[Field]] = {
    if (cell(row, col).content.isDefined) {
      Left(BoardError.AlreadyOccupied(row, col))
    } else {
      placeWallToBoard(row, col, player)
    }
  }

  def cell(row: Int, col: Int): Field = rows(row)(col)

  private def placeWallToBoard(row: Int, col: Int, player: Player): Either[BoardError, Quoridorboard[Field]] = {
    (row % 2, col % 2) match {
      case (1, _) =>
        if (col == size - 1) {
          Right(replaceCell(row, col, WallField(Some(Wall(player)))))
        } else {
          Right(
            replaceCell(row, col, WallField(Some(Wall(player))))
              .replaceCell(row, col + 1, WallField(Some(Wall(player))))
              .replaceCell(row, col + 2, WallField(Some(Wall(player))))
          )
        }

      case (0, 1) =>
        if (row == size - 1) {
          Right(replaceCell(row, col, WallField(Some(Wall(player)))))
        } else {
          Right(
            replaceCell(row, col, WallField(Some(Wall(player))))
              .replaceCell(row + 1, col, WallField(Some(Wall(player))))
              .replaceCell(row + 2, col, WallField(Some(Wall(player))))
          )
        }
      case _ => Left(BoardError.SomeWeirdError())
    }
  }

  def replaceCell(row: Int, col: Int, cell: Field): Quoridorboard[Field] = {
    cell match {
      case _: PieceField => copy(rows.updated(row, rows(row).updated(col, cell)))
      case _: WallField  => copy(rows.updated(row, rows(row).updated(col, cell)))
    }
  }

  def movePawn(toRow: Int, toCol: Int, player: Player): Either[BoardError, Quoridorboard[Field]] = {
    if (returnBoardIndexes().contains((toRow, toCol)))
      cell(toRow, toCol) match {
        case PieceField(None, _, _, _) => movePawnTo(toRow, toCol, player)
        case PieceField(_, _, _, _)    => Left(BoardError.AlreadyOccupied(toRow, toCol))
        case _                         => Left(BoardError.WrongField(toRow, toCol))
      }
    else
      Left(BoardError.IllegalMove(toRow, toCol))
  }

  def moveIsValid(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean = {
    val positions = returnBoardIndexes()
    val obstructedPosition =
      List((fromRow + 1, fromCol), (fromRow - 1, fromCol), (fromRow, fromCol + 1), (fromRow, fromCol - 1))
    val possibleMoves = List((fromRow + 2, fromCol), (fromRow - 2, fromCol), (fromRow, fromCol + 2), (fromRow, fromCol - 2))
    val allowedMoves = obstructedPosition
      .zip(possibleMoves)
      .filter(p => positions.contains(p._1) && possibleMoves.contains(p._2))
      .filter(x =>
        cell(x._1._1, x._1._2) match
          case WallField(None, _, _, _) => true
          case WallField(_, _, _, _)    => false
          case _                        => false
      )
      .map(_._2)
    allowedMoves.contains((toRow, toCol))
  }

  def returnPositionOfPlayerPawn(player: Player): Option[(Int, Int)] = {
    returnBoardIndexes() find { case (i, j) =>
      cell(i, j).equals(PieceField(Some(Pawn(player))))
    }
  }

  override def toString: String = {
    var temp = ""
    for (row <- this.rows) {
      for (cell <- row) {
        temp = temp ++ cell.toString
      }
      temp = temp ++ "\n"
    }
    temp
  }

  private def movePawnTo(toRow: Int, toCol: Int, player: Player): Either[BoardError, Quoridorboard[Field]] = {
    val playerPawnPosition = returnPositionOfPlayerPawn(player).getOrElse(None)
    playerPawnPosition match {
      case (fromRow: Int, fromCol: Int) =>
        if (moveIsValid(fromRow, fromCol, toRow, toCol)) {
          Right(replaceCell(toRow, toCol, cell(fromRow, fromCol)).replaceCell(fromRow, fromCol, PieceField(None)))
        } else {
          Left(BoardError.IllegalMove(toRow, toCol))
        }
      case None =>
        Left(BoardError.SomeWeirdError())
    }
  }

  private def returnBoardIndexes(): Vector[(Int, Int)] = {
    Iterator
      .range(start = 0, end = this.size)
      .flatMap { i =>
        Iterator.range(start = 0, end = row(i).size).map { j =>
          (i, j)
        }
      }
      .toVector
  }
