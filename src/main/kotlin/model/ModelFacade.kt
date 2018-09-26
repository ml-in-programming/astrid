package model

class ModelFacade {

    private val suggestion: List<String> = listOf("superName", "superDuperName", "superMegaDuperName")

    fun getSuggestions(): List<String> {
        return suggestion
    }

    fun generateSuggestions(body: String) {
        // place for magic
    }

}