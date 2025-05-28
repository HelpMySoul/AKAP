package playlistMenu.managers

import playlistMenu.interfaces.ISong

object FilterManager {
    private val fullTags = mapOf(
        "duration"      to "Длительность трека (сек)",
        "introduration" to "Длительность интро (сек)",
        "outroduration" to "Длительность аутро (сек)"
    )

    private val supportedOperators = listOf(">", "<", ">=", "<=", "==", "=", "!=")

    private fun isOperator(token: String): Boolean {
        return supportedOperators.contains(token)
    }

    private fun findFullTag(potentialTag: String): String? {
        return fullTags.keys.find { it.startsWith(potentialTag.lowercase()) }
    }

    fun getTagsDescription(): Map<String, String> {
        return fullTags
    }

    fun isValidTagValue(value: String): Boolean {
        return value.toLongOrNull() != null
    }

    fun compareValues(tagValue: Long, operator: String, value: Long): Boolean {
        return when (operator) {
            ">"       -> tagValue > value
            "<"       -> tagValue < value
            ">="      -> tagValue >= value
            "<="      -> tagValue <= value
            "==", "=" -> tagValue == value
            "!="      -> tagValue != value
            else      -> false
        }
    }

    fun extractFilterRules(keywords: List<String>): Pair<List<Triple<String, String, String>>, Set<Int>> {
        val filterRules     = mutableListOf<Triple<String, String, String>>()
        val excludedIndices = mutableSetOf<Int>()

        for (i in 0 until keywords.size - 2) {
            val potentialTag = keywords[i]
            val operator     = keywords[i+1]
            val value        = keywords[i+2]

            val fullTag = findFullTag(potentialTag)
            if (fullTag != null && isOperator(operator) && isValidTagValue(value)) {
                filterRules.add(Triple(fullTag, operator, value))
                excludedIndices.addAll(listOf(i, i+1, i+2))
            }
        }

        return Pair(filterRules, excludedIndices)
    }
}