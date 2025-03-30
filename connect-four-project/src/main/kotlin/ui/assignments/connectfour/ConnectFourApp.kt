package ui.assignments.connectfour

import javafx.application.Application
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.text.Font
import javafx.stage.Stage
import java.util.Stack
import ui.assignments.connectfour.model.Model
import ui.assignments.connectfour.ui.gameView

class ConnectFourApp : Application() {

    override fun start(stage: Stage) {



        val model = Model
        val gameView = gameView(model)

        val scene = Scene(gameView, 1100.0, 900.0)
        stage.title = "Connect Four"
        stage.scene = scene
        stage.isResizable = false
        stage.show()
    }
}