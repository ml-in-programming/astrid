package model

import com.intellij.psi.PsiMethod
import downloader.Downloader.getModelPath
import downloader.Downloader.modelSubDir
import org.tensorflow.SavedModelBundle
import org.tensorflow.Session
import org.tensorflow.Tensor
import helpers.TensorConverter.convertBinaryToString
import utils.PathUtils.getCombinedPaths
import utils.PsiUtils
import kotlin.collections.ArrayList

class ModelFacade {

    companion object {
        private val tfModel: SavedModelBundle = SavedModelBundle.load(getModelPath().toString() + modelSubDir, "serve")
    }

    fun getSuggestions(method: PsiMethod): List<String> {
        val methodBody = PsiUtils.getMethodBody(method)
        val suggestionsList: List<String> = generatePredictions(methodBody)
        return parseResults(suggestionsList)
    }

    fun getSuggestions(methodBody: String): List<String> {
        val suggestionsList: List<String> = generatePredictions(methodBody)
        return parseResults(suggestionsList)
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

    private fun generatePredictions(methodBody: String): List<String> {
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
        return predictions
    }
}