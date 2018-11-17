package utils

import extractor.features.ExtractFeaturesFromCodeBlock

class PathUtils {
    companion object {
        private fun getContextsFromMethodBody(methodBody: String): String {
            val extractFeaturesFromCodeBlock = ExtractFeaturesFromCodeBlock(methodBody)
            return extractFeaturesFromCodeBlock.processCodeBlock()
        }

        fun getCombinedPaths(methodBody: String): String {
            val predictLines = getContextsFromMethodBody(methodBody)
            val parts: List<String> = predictLines.split(' ')
            val methodName = parts[0]
            val currentResultLineParts = arrayListOf<String>(methodName)
            val contexts = parts.subList(1, parts.size)
            val maxContextsCount = 200
            var contextParts: List<String>
            var contextWord1: String
            var contextPath: String
            var contextWord2: String
            var hashedPath: String
            var resultLine = ""
            val list = if (contexts.size < maxContextsCount) contexts else contexts.subList(0, maxContextsCount - 1)
            for (context: String in list) {
                contextParts = context.split(',')
                contextWord1 = contextParts.get(0)
                contextPath = contextParts.get(1)
                contextWord2 = contextParts.get(2)
                hashedPath = contextPath.hashCode().toString()
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
}