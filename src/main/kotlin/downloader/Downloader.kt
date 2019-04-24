package downloader

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.ProjectManager
import utils.FileUtils
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object Downloader {
    private const val archiveName = "model.zip"
    private const val dirName = "model"
    private const val pluginName = "astrid_plugin"
    const val modelSubDir = "/model"
    const val beamSubDir = "/beam_search/_beam_search_ops.so"
    const val dictSubDir = "/model/dict/targets.dict"
    const val modelLink = "https://www.dropbox.com/s/44xurm93j2lcg06/model.zip?dl=1"
    private val tmp: String = System.getProperty("java.io.tmpdir")

    fun getArchivePath(): Path = Paths.get(tmp, pluginName, archiveName)
    fun getPluginPath(): Path = Paths.get(tmp, pluginName)
    fun getModelPath(): Path = Paths.get(tmp, pluginName, dirName)

    fun checkArchive() {
        val progressManager: ProgressManager = ProgressManager.getInstance();
        progressManager.run(object : Task.Backgroundable(ProjectManager.getInstance().defaultProject,
                "Generating suggestions", true) {
            override fun run(indicator: ProgressIndicator) {
                if (!Files.exists(getModelPath())) {
                    getPluginPath().toFile().mkdir()
                    downloadArchive(URL(modelLink), getArchivePath(),
                            ProgressManager.getInstance().progressIndicator)
                    if (indicator.isCanceled) return
                    ProgressManager.getInstance().progressIndicator.text = "Extracting archive"
                    FileUtils.unzip(getArchivePath().toString(), getModelPath().toString())
                }
            }
        })
    }

    fun downloadArchive(url: URL, path: Path, indicator: ProgressIndicator) {
        indicator.text = "astrid: Downloading model..."
        path.toFile().parentFile.mkdirs()
        val urlConnection = url.openConnection()
        val contentLength = urlConnection.contentLength

        BufferedInputStream(url.openStream()).use {
            val out = FileOutputStream(path.toFile())
            val data = ByteArray(1024)
            var totalCount = 0
            var count = it.read(data, 0, 1024)
            while (count != -1 && !indicator.isCanceled) {
                out.write(data, 0, count)
                totalCount += count
                if (contentLength == 0) {
                    indicator.fraction = 0.0
                } else {
                    indicator.fraction = totalCount.toDouble() / contentLength
                }
                count = it.read(data, 0, 1024)
            }
        }
        indicator.fraction = 1.0
    }
}