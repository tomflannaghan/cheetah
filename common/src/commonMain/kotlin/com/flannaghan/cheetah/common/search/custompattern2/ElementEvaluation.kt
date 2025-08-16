package com.flannaghan.cheetah.common.search.custompattern2

import com.flannaghan.cheetah.common.search.custompattern.PrefixSearchNode


data class EvaluationResult(val match: Boolean, val continuations: List<ElementEvaluation>)

val NO_MATCH = EvaluationResult(false, listOf())
val MATCH = EvaluationResult(true, listOf())


sealed class ElementEvaluation {
    /**
     * Take a step requiring a match for char.
     */
    abstract fun step(char: Char): EvaluationResult

    /**
     * Take a step, requiring a non-match for char.
     */
    abstract fun stepNoMatch(char: Char): EvaluationResult

    /**
     * Take a step, disregarding one character of the pattern. Always matches if any char could have matched.
     */
    abstract fun skipCharacter(): EvaluationResult

    /**
     * Does the element match an entire string.
     */
    fun matches(string: String): Boolean {
        var continuations = listOf(this)
        for ((i, c) in string.withIndex()) {
            val newContinuations = mutableListOf<ElementEvaluation>()
            for (ele in continuations) {
                val result = ele.step(c)
                if (result.match && i == string.length - 1) return true
                newContinuations.addAll(result.continuations)
            }
            continuations = newContinuations
        }
        return false
    }
}

data class MisprintEvaluation(val element: ElementEvaluation) : ElementEvaluation() {
    override fun step(char: Char): EvaluationResult {
        val resultMatch = element.step(char)
        val resultMismatch = element.stepNoMatch(char)
        // We switch to the underlying element evaluation once we have used the misprint so we only match
        // a mismatch if we are still in MisprintEvaluation (v counterintuitive!).
        return EvaluationResult(
            resultMismatch.match,
            resultMatch.continuations.map { this.copy(element = it) } +
                    resultMismatch.continuations  // Once the misprint has happened, we carry on normally.
        )
    }

    override fun stepNoMatch(char: Char): EvaluationResult {
        // This bit is counterintuitive. Because we are allowed to use a misprint, nothing can not match.
        return NO_MATCH
    }

    override fun skipCharacter(): EvaluationResult {
        val r = element.skipCharacter()
        return r.copy(continuations = r.continuations.map { MisprintEvaluation(it) })
    }
}

/**
 * A letter of the answer is not matched (i.e. there is a letter of the answer that is not clued by the wordplay)
 */
data class ExtraLetterInStringEvaluation(val element: ElementEvaluation) : ElementEvaluation() {
    override fun step(char: Char): EvaluationResult {
        val r = element.step(char)
        return EvaluationResult(
            false, // Can only match if the previous was a match (effectively skipping char)
            continuations = r.continuations.map { ExtraLetterInStringEvaluation(it) } +  // not found the unmatched letter
                    listOf(element) +  // Skip this letter, leaving the evaluation unchanged
                    if (r.match) listOf(ExtraLetterInStringAfterMatchEvaluation) else listOf()
        )
    }

    override fun stepNoMatch(char: Char): EvaluationResult {
        // Can always have an unmatched letter so nothing matches.
        return NO_MATCH
    }

    override fun skipCharacter(): EvaluationResult {
        val r = element.skipCharacter()
        return EvaluationResult(
            false, // Can only match if the previous was a match (effectively skipping char)
            continuations = r.continuations.map { ExtraLetterInStringEvaluation(it) } +  // not found the unmatched letter
                    listOf(element) +  // Skip this letter, leaving the evaluation unchanged
                    if (r.match) listOf(ExtraLetterInStringAfterMatchEvaluation) else listOf()
        )
    }
}


/**
 * A special evaluation state that represents having matched the previous step, but have an extra letter to consume.
 */
object ExtraLetterInStringAfterMatchEvaluation: ElementEvaluation() {
    override fun step(char: Char): EvaluationResult {
        return MATCH
    }

    override fun stepNoMatch(char: Char): EvaluationResult {
        return NO_MATCH
    }

    override fun skipCharacter(): EvaluationResult {
        return MATCH
    }

}


/**
 * A letter of the wordplay is not used.
 */
data class ExtraLetterInPatternEvaluation(val element: ElementEvaluation, val initialStep: Boolean) :
    ElementEvaluation() {
    override fun step(char: Char): EvaluationResult {
        return doStep { it.step(char) }
    }

    override fun stepNoMatch(char: Char): EvaluationResult {
        return doStep { it.stepNoMatch(char) }
    }

    override fun skipCharacter(): EvaluationResult {
        return doStep { it.skipCharacter() }
    }

    private fun doStep(s: (ElementEvaluation) -> EvaluationResult): EvaluationResult {
        val r = s(element)
        val rSkipped = r.continuations.map { it.skipCharacter() }
        // Can only match if we have skipped.
        var match = rSkipped.any { it.match }
        // If we have skipped, we use the underlying continuations as we are done.
        var continuations = r.continuations.map { ExtraLetterInPatternEvaluation(it, initialStep = false) } +
                rSkipped.flatMap { it.continuations }
        if (initialStep) {
            // On the first step, also try to skip first.
            val rSkippedFirst = element.skipCharacter().continuations.map { s(it) }
            match = match || rSkippedFirst.any { it.match }
            continuations += rSkippedFirst.flatMap { it.continuations }
        }
        return EvaluationResult(match, continuations)
    }
}


data class ConcatenateEvaluation(val left: ElementEvaluation, val right: ElementEvaluation) : ElementEvaluation() {
    override fun step(char: Char): EvaluationResult {
        return handleLeftResult(left.step(char))
    }

    override fun stepNoMatch(char: Char): EvaluationResult {
        return handleLeftResult(left.stepNoMatch(char))
    }

    override fun skipCharacter(): EvaluationResult {
        return handleLeftResult(left.skipCharacter())
    }

    private fun handleLeftResult(r: EvaluationResult): EvaluationResult {
        var continuations: List<ElementEvaluation> = r.continuations.map { this.copy(left = it) }
        // If the left matches, move onto the right.
        if (r.match) continuations += listOf(right)
        // Can never be true, as if we are in a concatenate eval we are still to match the right.
        return EvaluationResult(false, continuations)
    }
}


data class ContainsEvaluation(
    val outside: ElementEvaluation,
    val inside: ElementEvaluation,
    val isInside: Boolean
) : ElementEvaluation() {
    override fun step(char: Char): EvaluationResult {
        return if (isInside) {
            val r = inside.step(char)
            var continuations: List<ElementEvaluation> = r.continuations.map { this.copy(inside = it) }
            // Once we switch back to the outside, we don't need to be in our ContainsEvaluation any more.
            if (r.match) continuations += outside
            EvaluationResult(false, continuations)
        } else {
            val r = outside.step(char)
            // Can switch to the inside at any point so add that option.
            val continuations = r.continuations.map { this.copy(outside = it) } +
                    r.continuations.map { this.copy(outside = it, isInside = true) }
            EvaluationResult(false, continuations)
        }
    }

    override fun stepNoMatch(char: Char): EvaluationResult {
        return if (isInside) {
            val r = inside.stepNoMatch(char)
            var continuations: List<ElementEvaluation> = r.continuations.map { this.copy(inside = it) }
            // Once we switch back to the outside, we don't need to be in our ContainsEvaluation any more.
            if (r.match) continuations += outside
            EvaluationResult(false, continuations)
        } else {
            val r = outside.stepNoMatch(char)
            // Can switch to the inside at any point so add that option.
            val continuations = r.continuations.map { this.copy(outside = it) } +
                    r.continuations.map { this.copy(outside = it, isInside = true) }
            EvaluationResult(false, continuations)
        }
    }

    override fun skipCharacter(): EvaluationResult {
        return if (isInside) {
            val r = inside.skipCharacter()
            var continuations: List<ElementEvaluation> = r.continuations.map { this.copy(inside = it) }
            // Once we switch back to the outside, we don't need to be in our ContainsEvaluation any more.
            if (r.match) continuations += outside
            EvaluationResult(false, continuations)
        } else {
            val r = outside.skipCharacter()
            // Can switch to the inside at any point so add that option.
            val continuations = r.continuations.map { this.copy(outside = it) } +
                    r.continuations.map { this.copy(outside = it, isInside = true) }
            EvaluationResult(false, continuations)
        }
    }
}


data class LetterSequenceEvaluation(val pattern: String) : ElementEvaluation() {
    override fun step(char: Char): EvaluationResult {
        return handleMatch(pattern[0] == char || pattern[0] == '.')
    }

    override fun stepNoMatch(char: Char): EvaluationResult {
        return handleMatch(pattern[0] != char && pattern[0] != '.')
    }

    override fun skipCharacter(): EvaluationResult {
        return handleMatch(true)
    }

    private fun handleMatch(match: Boolean): EvaluationResult {
        return if (pattern.length == 1) EvaluationResult(match, listOf())
        else if (match) EvaluationResult(false, listOf(this.copy(pattern = pattern.substring(1))))
        else NO_MATCH
    }
}

data class AnagramEvaluation(
    val letterCounts: Map<Char, Int>,
    val numberOfDots: Int,
    val nonMatches: Int = 0
) : ElementEvaluation() {
    override fun step(char: Char): EvaluationResult {
        return if (char in letterCounts) {
            val lc = letterCounts.toMutableMap()
            if (letterCounts[char] == 1) lc.remove(char) else lc[char] = lc[char]!! - 1
            handleResult(lc, numberOfDots, nonMatches)
        } else if (numberOfDots > 0) {
            handleResult(letterCounts, numberOfDots - 1, nonMatches)
        } else {
            NO_MATCH
        }
    }

    override fun stepNoMatch(char: Char): EvaluationResult {
        return if (char !in letterCounts && numberOfDots == 0) {
            handleResult(letterCounts, numberOfDots, nonMatches + 1)
        } else {
            NO_MATCH
        }
    }

    override fun skipCharacter(): EvaluationResult {
        return handleResult(letterCounts, numberOfDots, nonMatches + 1)
    }

    private fun handleResult(letterCounts: Map<Char, Int>, numberOfDots: Int, nonMatches: Int): EvaluationResult {
        return if (letterCounts.values.sum() + numberOfDots - nonMatches == 0) {
            EvaluationResult(true, listOf())
        } else {
            EvaluationResult(false, listOf(AnagramEvaluation(letterCounts, numberOfDots, nonMatches)))
        }
    }
}

data class SubWordEvaluation(val node: PrefixSearchNode) : ElementEvaluation() {
    override fun step(char: Char): EvaluationResult {
        return handleResult(char == node.char)
    }

    override fun stepNoMatch(char: Char): EvaluationResult {
        return handleResult(char != node.char)
    }

    override fun skipCharacter(): EvaluationResult {
        return handleResult(true)
    }

    private fun handleResult(match: Boolean): EvaluationResult {
        return if (match) {
            EvaluationResult(node.isWord, node.children.map { SubWordEvaluation(it) })
        } else {
            NO_MATCH
        }
    }
}
