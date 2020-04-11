package org.olafneumann.dailyfolder

import javafx.scene.control.Alert
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
                content = "Not desktop folder could be found or the configured desktop folder does not exist.",
                title = "No Desktop found"
            )
            throw RuntimeException("No desktop available")
        }

        super.start(stage)
    }
}

/** Main application entry point */
fun main() = launch<DailyFolderApp>()
