package model

import downloader.Downloader.getModelPath
import downloader.Downloader.modelSubDir
import org.tensorflow.SavedModelBundle
import org.tensorflow.Session
import org.tensorflow.Tensor
import helpers.TensorConverter.convertBinaryToString
import utils.PathUtils.Companion.getCombinedPaths
import kotlin.collections.ArrayList

class ModelFacade {
    private val tfModel: SavedModelBundle
    private var suggestions: List<String>

    init {
        tfModel = SavedModelBundle.load(getModelPath().toString() + modelSubDir, "serve")
        suggestions = emptyList()
    }

    fun getSuggestions(): List<String> {
        return this.suggestions
    }

    private fun parseResults(predictions: List<String>): List<String> {
        var parsedPredictions = ArrayList<String>()
        for (p in predictions) {
            if (p.contains("|")) {
                val words = p.split("|")
                var parsedMethodName = words[0]
                for (word in words.subList(1, words.size)) {
                    parsedMethodName += word.substring(0, 1).toUpperCase() + word.substring(1)
                }
                parsedPredictions.add(parsedMethodName)
            } else {
                parsedPredictions.add(p)
            }
        }
        return parsedPredictions
    }

    fun generateSuggestions(methodBody: String) {
        val paths = getCombinedPaths(methodBody)
        val session: Session = tfModel.session()
        val runner = session.runner()
        val tokens: List<String> = paths.split("\\s+")
        val matrix = Array(1) { arrayOfNulls<ByteArray>(tokens.size) }
        for (i in 0 until tokens.size) {
            matrix[0][i] = tokens[i].toByteArray(Charsets.UTF_8)
        }
        val inputTensor = Tensor.create(matrix, String::class.java)
        val outputTensor: Tensor<*> = runner.feed("Placeholder:0", inputTensor).fetch("hash_table_Lookup:0").run()[0]
        val predictions: List<String> = convertBinaryToString(outputTensor)
        outputTensor.close()
        this.suggestions = parseResults(predictions)
    }
}