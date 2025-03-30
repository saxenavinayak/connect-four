package ui.assignments.connectfour.ui

import javafx.animation.Interpolator
import javafx.animation.TranslateTransition
import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.util.Duration
import ui.assignments.connectfour.model.Array2D
import ui.assignments.connectfour.model.Model
import ui.assignments.connectfour.model.Piece
import ui.assignments.connectfour.model.Player

class gameView(private val model: Model) : Pane(), ChangeListener<Player>{
    // Initialize paths to pieces iamges
    val redPiecePath = javaClass.getResource("/ui/assignments/connectfour/piece_red.png").toString()
    val yellowPiecePath = javaClass.getResource("/ui/assignments/connectfour/piece_yellow.png").toString()

    // Labels for player controllers, we will change these everytime a player's turn is initiated
    val playerOneLabel = Label("Player #1").apply { font= Font.font("Verdana", FontWeight.NORMAL, 30.0) }
    val playerTwoLabel = Label("Player #2").apply { font= Font.font( "Verdana", FontWeight.NORMAL, 30.0) }
    inner class Delta {
        var x = 0.0
        var y = 0.0
    }

    // Check if an x value is within "snap" range of the grid, if it is, return the x-coordinate, otherwise -1
    private fun checkSnap(x: Double) : Double {
        for (i in 1..8) {
            val leftLimit = (i * 100) + 10
            val rightLimit = leftLimit + 80
            if (x >= leftLimit && x <= rightLimit) {
                return leftLimit+48.0
            }
        }
        return -1.0
    }

    // Initial layout view
    private fun layoutView() {
        // Gamestart button
        val gameStartButton = Button("Click Here To Start Game!").apply { prefWidth=500.0;prefHeight=50.0; font=Font.font("Verdana", FontWeight.BOLD, 30.0);  }
        val gridPath = javaClass.getResource("/ui/assignments/connectfour/grid_8x7.png").toString()

        // Load grid
        val gameBoard = ImageView(gridPath)

        // Hard-code the coordinates
        gameBoard.x=150.0
        gameBoard.y=175.0
        playerOneLabel.layoutX=25.0
        playerOneLabel.layoutY=10.0
        playerTwoLabel.layoutX=930.0
        playerTwoLabel.layoutY=10.0
        gameStartButton.layoutX=300.0
        gameStartButton.layoutY=50.0

        // Start game on button press
        gameStartButton.onAction=EventHandler{
            children.remove(gameStartButton)
            model.startGame()
        }
        children.addAll(playerOneLabel, playerTwoLabel, gameBoard, gameStartButton)
    }

    init {
        layoutView()
        // Listen to onNextPlayer and onGameWin for changes
        Model.onNextPlayer.addListener(this)
        Model.onGameWin.addListener(this)
    }


    override fun changed(observable: ObservableValue<out Player>?, oldValue: Player?, newValue: Player?) {
        // Check if someone won
        val didSomeoneWin = Model.onGameWin.value
        if (didSomeoneWin != Player.NONE) {
            var someoneWon = Label()

            // Apply the correct properties to the winning player label
            if (didSomeoneWin==Player.ONE) {
                someoneWon=Label("Red Won!").apply { prefWidth=400.0;prefHeight=50.0; font=Font(30.0); alignment= Pos.CENTER;
                    background= Background(BackgroundFill(Color.RED, CornerRadii(10.0), Insets(5.0)))}
            } else {
                someoneWon=Label("Yellow Won!").apply { prefWidth=400.0;prefHeight=50.0; font=Font(30.0); alignment=Pos.CENTER
                    background= Background(BackgroundFill(Color.GOLD, CornerRadii(10.0), Insets(5.0)))}
            }

            // Add the winning player label to scene
            someoneWon.font=Font.font("Verdana", FontWeight.BOLD, 30.0)
            someoneWon.layoutX=350.0
            someoneWon.layoutY=50.0
            children.add(someoneWon)

        } else {
            // Otherwise, no one has won, add a newPiece to the scene
            var newPiece = ImageView()

            // Check which piece to spawn, and highlight whichever player's turn it is
            if (newValue == Player.ONE) {
                newPiece = ImageView(redPiecePath)
                playerOneLabel.textFill=Color.ORANGE
                playerTwoLabel.textFill=Color.BLACK
                playerOneLabel.font=Font.font("Verdana", FontWeight.BOLD, 30.0)
                playerTwoLabel.font=Font.font( "Verdana", FontWeight.NORMAL, 30.0)
            } else {
                playerOneLabel.textFill=Color.BLACK
                playerTwoLabel.textFill=Color.ORANGE
                newPiece = ImageView(yellowPiecePath)
                playerOneLabel.font=Font.font( "Verdana", FontWeight.NORMAL, 30.0)
                playerTwoLabel.font=Font.font( "Verdana", FontWeight.BOLD, 30.0)
            }

            // Initialize translation for Piece
            val dragDelta = Delta()
            val translation = TranslateTransition(Duration.seconds(1.0), newPiece)
            translation.interpolator = Interpolator.EASE_BOTH

            newPiece.onMousePressed = EventHandler {
                dragDelta.x = newPiece.layoutX - it.sceneX
                dragDelta.y = newPiece.layoutY - it.sceneY
                newPiece.cursor = Cursor.NONE
            }

            // Mouse is released over a piece
            newPiece.onMouseReleased = EventHandler {
                newPiece.cursor = Cursor.HAND
                val xval = it.sceneX + dragDelta.x
                val snapVal = checkSnap(xval)
                // Check if the piece is "snapped" -- then it will be dropped down
                if (snapVal != -1.0) {
                    val columnDropped = ((snapVal - 58.0) / 100.0) - 1
                    Model.dropPiece(columnDropped.toInt())

                    // Check if piece was dropped correctly
                    val isDropped = Model.onPieceDropped.value

                    if (isDropped != null) {

                        // Dropped correctly, play animation
                        val yCord = isDropped.y
                        newPiece.layoutY=0.0
                        translation.fromY = 0.0
                        translation.toY = 783.0 - ((6.0 - yCord) * 100)
                        translation.play()
                        newPiece.onMouseDragged = null
                        newPiece.onMousePressed = null
                        newPiece.onMouseReleased = null

                        // Check for game draw
                        val draw = Model.onGameDraw.value

                        // Apply draw label properties and add it to scene
                        if (draw) {
                            val drawLabel = Label("It's a Draw!").apply { prefWidth=500.0;prefHeight=50.0; font=Font.font("Verdana", FontWeight.BOLD, 30.0); alignment=Pos.CENTER }
                            drawLabel.textFill=Color.BLACK
                            drawLabel.layoutX=300.0
                            drawLabel.layoutY=50.0
                            children.add(drawLabel)

                        }
                    // Drop wasn't successfull, so move the piece back to initial position
                    } else {
                        if (newValue == Player.ONE) {
                            newPiece.layoutX = 50.0
                            newPiece.layoutY = 50.0
                        } else {
                            newPiece.layoutX = 950.0
                            newPiece.layoutY = 50.0
                        }

                    }
                }
            }

            // We are dragging piece
            newPiece.onMouseDragged = EventHandler {
                val xval = it.sceneX + dragDelta.x
                val yval = it.sceneY + dragDelta.y

                // Check for border constraints
                if (xval < 0 || xval > 1020  || yval > 820){
                    // Left, Right, and Bottom boundaries are hit
                }else if ((xval >= 70.0 && xval <= 950.0) && yval > 100.0){
                    // Piece interacting with the grid on left and right side
                } else if ((xval <= 70.0 || xval >= 950.0) && yval < 0.0) {
                    // Make sure piece is not going "inside" the grid
                }
                else {
                    // Check if the piece position is snappable, if it is, make sure it can only go up and down
                    val snapVal = checkSnap(xval)
                    if (snapVal != -1.0) {
                        if (it.sceneY + dragDelta.y < 0.0) {
                            newPiece.layoutY = 0.0
                        } else {
                            newPiece.layoutX = snapVal
                            newPiece.layoutY = it.sceneY + dragDelta.y
                        }
                    } else {
                        newPiece.layoutX = it.sceneX + dragDelta.x
                        newPiece.layoutY = it.sceneY + dragDelta.y
                    }
                }
            }

            children.add(newPiece)
            newPiece.toBack()
            if (newValue == Player.ONE) {
                newPiece.layoutX = 50.0
                newPiece.layoutY = 50.0
            } else {
                newPiece.layoutX = 950.0
                newPiece.layoutY = 50.0
            }
        }

    }
}
