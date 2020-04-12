package org.olafneumann.dailyfolder.view

import javafx.beans.binding.When
import javafx.beans.property.SimpleIntegerProperty
import javafx.geometry.Insets
import javafx.scene.control.CheckBox
import javafx.scene.image.Image
import org.olafneumann.dailyfolder.DailyFolderApp
import org.olafneumann.dailyfolder.model.name
import tornadofx.*
import java.nio.file.Path
import java.nio.file.Paths

class FolderDeletionView : View(title = "Daily Folder") {
    companion object {
         const val MIN_WIDTH = 150.0
         const val MIN_HEIGHT = 10.0
        private const val VIEW_PADDING = 10.0

        private const val ELEMENT_SPACING = 5.0
        private val ELEMENT_MAX_WIDTH = Double.MAX_VALUE

        private const val TOOLTIP_MAX_ITEMS = 15
        private const val TOOLTIP_ELLIPSIS = "..."
        private const val TOOLTIP_LINE_SEPARATOR = "\n"

        private const val BUTTON_TEXT_DELETE = "Delete"
        private const val BUTTON_TEXT_EXIT = "Exit"

        private val iconSizes = listOf(16, 20, 24, 32, 48, 64, 128, 256, 512)
    }

    private val folderController: FolderController by inject()

    private var folderToCheckbox: MutableMap<Path, CheckBox> = mutableMapOf()
    private val numberOfSelectedFolders = SimpleIntegerProperty(0)

    override val root = vbox {
        minWidth = MIN_WIDTH
        minHeight = MIN_HEIGHT
        padding = Insets(VIEW_PADDING)
        spacing = ELEMENT_SPACING

        iconSizes.map { "/icons/calendar_$it.png" }
            .mapNotNull { DailyFolderApp.loadImage(it) }
            .forEach { addStageIcon(it) }

        // Add checkboxes for folders
        folderToCheckbox.putAll(folderController.foldersToShowWithContent
            .sortedBy { it.first.name }
            .map { (folder, content) ->
                folder to checkbox(folder.name) {
                    maxWidth = ELEMENT_MAX_WIDTH
                    tooltip(
                        content.take(TOOLTIP_MAX_ITEMS).joinToString(TOOLTIP_LINE_SEPARATOR)
                                + if (content.size > TOOLTIP_MAX_ITEMS) "$TOOLTIP_LINE_SEPARATOR$TOOLTIP_ELLIPSIS" else ""
                    )
                    action {
                        if (isSelected) {
                            numberOfSelectedFolders.set(numberOfSelectedFolders.get() + 1)
                        } else {
                            numberOfSelectedFolders.set(numberOfSelectedFolders.get() - 1)
                        }
                    }
                }
            })

        // add delete/ exit button
        button {
            isDefaultButton = true
            maxWidth = ELEMENT_MAX_WIDTH
            bind(When(numberOfSelectedFolders.greaterThan(0)).then(BUTTON_TEXT_DELETE).otherwise(BUTTON_TEXT_EXIT))
            action {
                folderController.deleteFolders(folderToCheckbox.filterValues { it.isSelected }.map { it.key })
                close()
            }
        }
    }
}



