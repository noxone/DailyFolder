package org.olafneumann.dailyfolder.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FolderTest {
    companion object {
        private val folderList = listOf("2020-01-23", "2020-02-23", "2019-12-31")

        private fun setupDirectories(baseFolder: Path) =
            folderList.map { baseFolder.resolve(it)!! }
                .also { list -> list.forEach { Files.createDirectory(it) } }
    }

    @Test
    fun `should create folder for current da`(@TempDir basePath: Path) {
        val folder = Folder(basePath)
        val folderName = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now())!!
        val expected = basePath.resolve(folderName)

        val actual = folder.createFolderForToday()

        assertThat(actual).isEqualTo(expected)
        assertThat(actual).hasFileName(folderName)
        assertThat(actual).exists()
    }

    @Test
    fun `should delete two folders and leave one non-empty`(@TempDir basePath: Path) {
        // prepare folder
        val folderShouldNotBeDeleted = setupDirectories(basePath).first()
        val file = folderShouldNotBeDeleted.resolve("file.txt")
        Files.write(file, listOf("unit-test"))
        val folder = Folder(basePath)
        val expectedNumberOfItemsInPath = 1

        // do the action
        folder.deleteEmptyFolders()

        // count folders
        val actualNumberOfItemsInPath = Files.newDirectoryStream(basePath)
            .use { stream -> stream.mapNotNull { it }.toList() }
            .count()

        assertThat(folderShouldNotBeDeleted).exists()
        assertThat(actualNumberOfItemsInPath).isEqualTo(expectedNumberOfItemsInPath)
    }

    @Test
    fun `should count daily folders`(@TempDir basePath: Path) {
        setupDirectories(basePath)
        val folder = Folder(basePath)
        val expectedNumberOfFolders = folderList.size

        val dailyFolders = folder.dailyFolders

        assertThat(dailyFolders).size().isEqualTo(expectedNumberOfFolders)
    }
}