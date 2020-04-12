package org.olafneumann.dailyfolder.model

import org.olafneumann.dailyfolder.helper.FolderHelper
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class Folder(
    private val path: Path
) {
    constructor() : this(FolderHelper.getDesktopFolder() ?: FileSystems.getDefault().rootDirectories.first()!!)

    companion object {
        /** default object using the platforms desktop folder */
        val DEFAULT = Folder()

        /** Formatter that produces the desired folder name */
        private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

        /** regex describing the desired folder name format */
        private val folderPattern = Regex("^(?<year>[0-9]{4})-(?<month>[0-9]{1,2})-(?<day>[0-9]{1,2})$")

        /** This value returns the today's current folder name */
        private val currentDailyFolderName: String get() = dateFormatter.format(LocalDate.now())
    }

    val dailyFolders: List<Path> get() = path.listPaths().filter { folderPattern.matches(it.name) }
    val currentDailyFolder: Path get() = path.resolve(currentDailyFolderName)

    fun listContentNames(path: Path) = path.listPaths().map { it.fileName!!.toString() }

    fun createFolderForToday() = Files.createDirectories(currentDailyFolder)!!

    fun deleteFolders(predicate: (Path) -> Boolean) = dailyFolders.filter(predicate).forEach(Files::delete)
    fun deleteEmptyFolders() = deleteFolders { it.isEmpty() && it.name != currentDailyFolderName }
}