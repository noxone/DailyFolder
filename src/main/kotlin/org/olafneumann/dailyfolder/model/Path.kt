package org.olafneumann.dailyfolder.model

import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

/** List all contents of a given path if it is a directory. Returns an empty list if the directory is empty
 * or the path is not a directory*/
internal fun Path.listPaths(): List<Path> =
    if (Files.isDirectory(this))
        Files.list(this)
            .use { stream ->
                stream.asSequence()
                    .mapNotNull { it }
                    .toList()
            }
    else
        emptyList()

/** Checks whether the current Path is an empty directory (or not a directory at all) */
internal fun Path.isEmpty() =
    if (Files.isDirectory(this))
        Files.list(this).use { it.limit(2).count() == 0L }
    else
        false

/**
 * Delete a given path and if it is a directory also delete its content
 *
 * @return `true` if the deletion was successful. `false`
 * if the path did not exists or a problem occurred while deleting.
 */
internal fun Path.deleteRecursively(): Boolean =
    Files.exists(this)
            && (!Files.isDirectory(this) || Files.list(this)
        .use { it.asSequence().all { path -> path!!.deleteRecursively() } })
            && Files.deleteIfExists(this.also { println("Delete: $it") })

/** The actual name of the path */
internal val Path.name: String get() = fileName.toString()