package com.flannaghan.cheetah.common.search.custompattern2


data class StringNode(val value: String? = null, val children: List<StringNode> = listOf())
data class FuncNode(val name: String? = null, val parameters: List<FuncNode> = listOf())


fun toStringTree(string: String): StringNode {
    var currentData = ""
    val children = mutableListOf<StringNode>()
    var depth = 0
    for (c in string) {
        if (c == '(') {
            if (depth == 0 && currentData.isNotEmpty()) {
                children.add(StringNode(currentData, listOf()))
                currentData = ""
            } else {
                currentData += '('
            }
            depth++
        } else if (c == ')') {
            depth--
            if (depth == 0 && currentData.isNotEmpty()) {
                children.add(toStringTree(currentData))
                currentData = ""
            } else if (depth < 0) {
                error("Unbalanced brackets")
            } else {
                currentData += ')'
            }
        } else {
            currentData += c
        }
    }
    if (depth != 0) {
        error("Unbalanced brackets")
    }
    if (currentData.isNotEmpty()) {
        children.add(StringNode(currentData, listOf()))
    }

    return if (children.size == 1) children[0] else StringNode(null, children)
}


fun splitCommasBrackets(string: String): List<List<String>> {
    var currentData = ""
    val components = mutableListOf<List<String>>()
    var component = mutableListOf<String>()
    var depth = 0
    for (c in string) {
        if (c == '(') {
            if (depth == 0 && currentData.isNotEmpty()) {
                component.add(currentData)
                currentData = ""
            }
            depth++
            currentData += c
        } else if (c == ')') {
            currentData += c
            depth--
            if (depth == 0) {
                component.add(currentData)
                currentData = ""
            }
            if (depth < 0) {
                error("Unbalanced brackets")
            }
        } else if (depth == 0 && c == ',') {
            component.add(currentData)
            components.add(component)
            component = mutableListOf()
            currentData = ""
        } else {
            currentData += c
        }
    }

    if (depth != 0) {
        error("Unbalanced brackets")
    }

    if (currentData.isNotEmpty()) {
        component.add(currentData)
    }
    components.add(component)
    return components
}

fun parseCustomPattern2(string: String): PatternElement {
    val params = splitCommasBrackets(string)
    if (params.isEmpty()) error("Empty string")
    var firstParam = params.first()
    val firstString = firstParam.first()
    var func = ""
    if (!firstString.startsWith('(') && ':' in firstString) {
        func = firstString.substringBeforeLast(':').replace(":", "")
        firstParam = listOf(firstString.substringAfterLast(':')) + firstParam.drop(1)
    }

    if (func == "" && params.size == 1 && firstParam.size == 1)
        return LetterSequence(firstParam.single())

}


fun parseFuncAndParams(func: Char, params: List<String>): Pair<PatternElement, Int> {
    return when (func) {
        // Concatenate
        'c' -> {
            Pair(
                if (params.size > 1)
                    Concatenate(
                        parseCustomPattern2(params.first()),
                        parseFuncAndParams('c', params.drop(1)).first
                    )
                else parseCustomPattern2(params.single()),
                params.size
            )
        }

        // Anagram
        'a' -> Pair(Anagram(params.first()), 1)

        // Backwards word
        'b' -> TODO()
        // Forwards word
        'f' -> TODO()

        // Inside
        'i' -> Pair(
            Contains(parseCustomPattern2(params[0]), parseCustomPattern2(params[1])),
            2
        )

        // Misprint
        '`' -> Pair(
            Misprint(parseCustomPattern2(params[0])),
            1
        )

        else -> {
            error("Unknown function ${func}")
        }
    }
}