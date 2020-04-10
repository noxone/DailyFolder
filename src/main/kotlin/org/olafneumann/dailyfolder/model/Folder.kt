package org.olafneumann.dailyfolder.model

import org.olafneumann.dailyfolder.helper.FolderHelper
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Folder(
    val path: Path
) {
    constructor() : this(FolderHelper.getDesktopFolder() ?: FileSystems.getDefault().rootDirectories.first()!!)

    companion object {
        val DEFAULT = Folder()
        private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        private val folderPattern = Regex("^\\d{4}-\\d{2}-\\d{2}$")

        private fun Path.listPaths() =
            Files.newDirectoryStream(this).use { stream -> stream.mapNotNull { it }.toList() }

        private fun Path.matchesFolderPattern() = fileName.toString().matches(folderPattern)
        private fun Path.isEmpty() = listPaths().isEmpty()
        private fun Path.listNames() = listPaths().map { it.fileName!!.toString() }

        private val currentDailyFolderName: String get() = dateFormatter.format(LocalDate.now())
    }

    val dailyFolders: List<Path> get() = path.listPaths().filter { it.matchesFolderPattern() }
    val currentDailyFolder: Path get() = path.resolve(currentDailyFolderName)

    fun getSubfolderContents() = dailyFolders.map { it to it.listNames() }

    fun createFolderForToday() = Files.createDirectories(currentDailyFolder)

    fun deleteFolders(predicate: (Path) -> Boolean)= dailyFolders.filter(predicate).forEach(Files::delete)
    fun deleteEmptyFolders() = deleteFolders { path.isEmpty() }

    val Path.name: String get() = fileName.toString()
}