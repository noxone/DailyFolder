package org.olafneumann.dailyfolder.view

import org.olafneumann.dailyfolder.model.FolderController
import tornadofx.*

class FolderDeletionView : View() {
    private val folderController: FolderController by inject()

    override val root = borderpane {
        top {
            vbox {
                button("Press me")
                label("Waiting")
            }
        }
        center {
            vbox {
                folderController.folder
                    .getSubfolderContents()
                    .sortedBy { it.first.fileName.toString() }
                    .forEach { (folder, content) ->
                        checkbox(folder.fileName.toString()) {
                            tooltip(content.joinToString(separator = "\\n"))
                        }
                    }
                spacing = 5.0
            }
        }
        bottom {
            button("Delete")
        }
    }
}



