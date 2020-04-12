package org.olafneumann.dailyfolder

import javafx.scene.control.Alert
import javafx.scene.image.Image
import javafx.stage.Stage
import org.olafneumann.dailyfolder.helper.FolderHelper.desktopAvailable
import org.olafneumann.dailyfolder.view.FolderDeletionView
import tornadofx.*

/** main application class defining the initial view to be shown */
class DailyFolderApp : App(FolderDeletionView::class) {
    /**
     * Executed when the application is about to be started by JavaFX launcher.<p>
     * This implementation checks whether the desktop folder is known. If not an error message will be shown and the application start will be aborted.
     */
    override fun start(stage: Stage) {
        if (!desktopAvailable) {
            alert(
                type = Alert.AlertType.ERROR,
                header = "No Desktop found",
                title = "No Desktop found",
                content = "Not desktop folder could be found or the configured desktop folder does not exist."
            )
            throw RuntimeException("No desktop available")
        }

        super.start(stage)

        stage.minWidth = FolderDeletionView.MIN_WIDTH
        stage.minHeight = FolderDeletionView.MIN_HEIGHT
        stage.isResizable = false
    }

    companion object {
        fun loadImage(name: String): Image? = DailyFolderApp::class.java.getResourceAsStream(name)?.use { Image(it) }
    }
}

/** Main application entry point */
fun main() = launch<DailyFolderApp>()
