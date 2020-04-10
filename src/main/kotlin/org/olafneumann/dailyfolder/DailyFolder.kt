package org.olafneumann.dailyfolder

import javafx.scene.control.Alert
import javafx.stage.Stage
import org.olafneumann.dailyfolder.helper.FolderHelper.desktopAvailable
import org.olafneumann.dailyfolder.view.FolderDeletionView
import tornadofx.*

class DailyFolderApp : App(FolderDeletionView::class) {
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

fun main() = launch<DailyFolderApp>()
