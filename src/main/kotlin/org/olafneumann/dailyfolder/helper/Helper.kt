package org.olafneumann.dailyfolder.helper

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

internal object FolderHelper {
    fun getDesktopFolder(): Path? =
        when (OperatingSystem.current) {
            OperatingSystem.MacOsX -> System.getProperty("user.home")?.let { Paths.get(it, "Desktop") }
            OperatingSystem.Windows -> WindowsHelper.currentUserDesktopPath
            else -> null
        }

    val desktopAvailable: Boolean get() = getDesktopFolder()?.let { it != null && Files.exists(it) } ?: false
}

private enum class OperatingSystem(private val searchStrings: List<String>) {
    Windows(listOf("windows")),
    MacOsX(listOf("mac os x")),
    Linux(listOf("nix", "nux"));

    companion object {
        val current: OperatingSystem?
            get() {
                val osName = System.getProperty("os.name")?.toString()
                return values().firstOrNull { os ->
                    os.searchStrings.any {
                        osName?.contains(other = it, ignoreCase = true) ?: false
                    }
                }
            }
    }
}


/**
 * Helper class containing methods to access Windows specific functionality
 *
 * @author neumaol
 */
private object WindowsHelper {
    /** Command to retrieve values from the Windows registry  */
    private const val REG_QUERY_COMMAND = "reg query"

    /** Some string used later  */
    private const val REG_TOKEN = "REG_SZ"

    /** Registry path to the information where the Windows desktop is  */
    private const val DESKTOP_FOLDER_CMD =
        "$REG_QUERY_COMMAND \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v DESKTOP"

    /** The path to the current user's Windows desktop */
    val currentUserDesktopPath: Path?
        get() = try {
            val process = Runtime.getRuntime().exec(DESKTOP_FOLDER_CMD)
            val reader = InputStreamCapturingThread(process.inputStream)
            reader.start()
            process.waitFor()
            reader.join()
            val result = reader.result
            val index = result.indexOf(REG_TOKEN)
            if (index >= 0)
                Paths.get(result.substring(index + REG_TOKEN.length).trim())
            else
                null
        } catch (ignore: InterruptedException) {
            null
        } catch (ignore: IOException) {
            null
        }
}

/** Thread specialized to read process output */
private class InputStreamCapturingThread(
    inputStream: InputStream
) : Thread() {
    /** Buffer to capture output in  */
    private val sw = StringWriter()
    private val reader = InputStreamReader(
        inputStream,
        Charset.defaultCharset() // using default charset in expectation that the console output will be in default charset, too
    )

    /** the captured data */
    val result: String get() = sw.toString()

    override fun run() {
        reader.copyTo(sw)
    }
}