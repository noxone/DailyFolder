package org.olafneumann.dailyfolder.view

import org.olafneumann.dailyfolder.model.Folder
import tornadofx.Controller
import java.nio.file.Path

class FolderController : Controller() {
    private val folder = Folder.DEFAULT

    init {
        folder.createFolderForToday()
    }

    private val foldersToShow: List<Path> get() = folder.dailyFolders.filter { it != folder.currentDailyFolder }

    val foldersToShowWithContent get() = foldersToShow.map { it to folder.listContentNames(it) }
}