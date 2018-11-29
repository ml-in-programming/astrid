package utils

import extractors.features.Extractor

object PathUtils {
    private fun getContextsFromMethodBody(methodBody: String): String {
        val extractor = Extractor(methodBody)
        return extractor.processCodeBlock()
    }

    fun getCombinedPaths(methodBody: String): String {
        val parts = getContextsFromMethodBody(methodBody).split(' ')
        val methodName = parts[0]
        val currentResultLineParts = arrayListOf(methodName)
        val contexts = parts.subList(1, parts.size)
        val maxContextsCount = 200
        var contextParts: List<String>
        var contextWord1: String
        var contextWord2: String
        var hashedPath: String
        var resultLine = ""
        val list = if (contexts.size < maxContextsCount) contexts else contexts.subList(0, maxContextsCount - 1)
        for (context: String in list) {
            contextParts = context.split(',')
            contextWord1 = contextParts[0]
            contextWord2 = contextParts[2]
            hashedPath = contextParts[1].hashCode().toString()
            currentResultLineParts.add("$contextWord1,$hashedPath,$contextWord2")
        }
        currentResultLineParts.forEach { part ->
            resultLine += " $part"
        }
        val spaceCount = maxContextsCount - contexts.size
        for (i in 0..spaceCount - 2) {
            resultLine += " "
        }
        return resultLine
    }
}