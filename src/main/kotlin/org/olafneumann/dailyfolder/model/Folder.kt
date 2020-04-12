package org.olafneumann.dailyfolder.model

import org.olafneumann.dailyfolder.helper.FolderHelper
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

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

        /** Predicate testing whether the current path matches the daily folder name pattern */
        private fun Path.matchesFolderPattern() = fileName.toString().matches(folderPattern)

        /** List all contents of a given path if it is a directory. Returns an empty list if the directory is empty
         * or the path is not a directory*/
        private fun Path.listPaths(): List<Path> =
            if (Files.isDirectory(this))
                Files.list(this)
                    .use { stream ->
                        stream.filter { it != null }
                            .map { it!! }
                            .collect(Collectors.toList())
                    }
            else
                emptyList()

        /**Checks whether the current Path is an empty directory (or not a directory at all)*/
        private fun Path.isEmpty() =
            if (Files.isDirectory(this))
                Files.list(this).use { it.limit(2).count() == 0L }
            else
                false
    }

    val dailyFolders: List<Path> get() = path.listPaths().filter { it.matchesFolderPattern() }
    val currentDailyFolder: Path get() = path.resolve(currentDailyFolderName)

    fun listContentNames(path: Path) = path.listPaths().map { it.fileName!!.toString() }

    fun createFolderForToday() = Files.createDirectories(currentDailyFolder)!!

    fun deleteFolders(predicate: (Path) -> Boolean) = dailyFolders.filter(predicate).forEach(Files::delete)
    fun deleteEmptyFolders() = deleteFolders { it.isEmpty() && it.name != currentDailyFolderName }

    private val Path.name: String get() = fileName.toString()
}