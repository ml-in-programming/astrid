package inspections

/**
 * This class contains information about suggestion for method name
 *
 */
class Suggestion(val names: List<String>) {
    var needRecalculate: Boolean = false

    var ignore: Boolean = false

    fun setRecalculate() {
        this.needRecalculate = true
    }

    fun setIgnore() {
        this.ignore = true
    }
}