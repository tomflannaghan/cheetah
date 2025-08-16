package com.flannaghan.cheetah.common.search.custompattern2

import com.flannaghan.cheetah.common.search.Matcher
import com.flannaghan.cheetah.common.search.SearchContext
import com.flannaghan.cheetah.common.search.custompattern.PrefixSearchNode


sealed class PatternElement {
    abstract suspend fun toEvaluation(context: SearchContext): ElementEvaluation
}

data class Misprint(val element: PatternElement) : PatternElement() {
    override suspend fun toEvaluation(context: SearchContext): ElementEvaluation {
        return MisprintEvaluation(element.toEvaluation(context))
    }
}

data class ExtraLetterInString(val element: PatternElement) : PatternElement() {
    override suspend fun toEvaluation(context: SearchContext): ElementEvaluation {
        return ExtraLetterInStringEvaluation(element.toEvaluation(context))
    }
}

data class ExtraLetterInPattern(val element: PatternElement) : PatternElement() {
    override suspend fun toEvaluation(context: SearchContext): ElementEvaluation {
        return ExtraLetterInPatternEvaluation(element.toEvaluation(context), true)
    }
}

data class Contains(val outside: PatternElement, val inside: PatternElement) : PatternElement() {
    override suspend fun toEvaluation(context: SearchContext): ElementEvaluation {
        return ContainsEvaluation(outside.toEvaluation(context), inside.toEvaluation(context), false)
    }
}

data class Concatenate(val left: PatternElement, val right: PatternElement) : PatternElement() {
    override suspend fun toEvaluation(context: SearchContext): ElementEvaluation {
        return ConcatenateEvaluation(left.toEvaluation(context), right.toEvaluation(context))
    }
}

data class LetterSequence(val pattern: String) : PatternElement() {
    override suspend fun toEvaluation(context: SearchContext): ElementEvaluation {
        return LetterSequenceEvaluation(pattern)
    }
}

data class Anagram(val pattern: String) : PatternElement() {
    override suspend fun toEvaluation(context: SearchContext): ElementEvaluation {
        val letterCounts = pattern.replace(".", "").groupingBy { it }.eachCount()
        val dots = pattern.count { it == '.' }
        return AnagramEvaluation(letterCounts, dots)
    }
}

data class SubWord(val backwards: Boolean) : PatternElement() {
    override suspend fun toEvaluation(context: SearchContext): ElementEvaluation {
        return SubWordEvaluation(context.getPrefixSearchTree(backwards))
    }
}

data class SubWordMatcher(val matcher: Matcher, val backwards: Boolean) : PatternElement() {
    override suspend fun toEvaluation(context: SearchContext): ElementEvaluation {
        return SubWordEvaluation(context.getPrefixSearchTreeForMatcher(matcher, backwards))
    }
}