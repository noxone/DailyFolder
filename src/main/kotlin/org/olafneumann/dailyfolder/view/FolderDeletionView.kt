package org.olafneumann.dailyfolder.view

import javafx.geometry.Insets
import tornadofx.*

class FolderDeletionView : View() {
    private val folderController: FolderController by inject()

    override val root = borderpane {
        center {
            vbox {
                folderController.foldersToShowWithContent
                    .sortedBy { it.first.fileName.toString() }
                    .forEach { (folder, content) ->
                        checkbox(folder.fileName.toString()) {
                            tooltip(content.joinToString(separator = "\n"))
                        }
                    }
                spacing = 5.0
                padding = Insets(5.0)
            }
        }
        bottom {
            button("Delete")
        }
        padding = Insets(5.0)
    }
}



