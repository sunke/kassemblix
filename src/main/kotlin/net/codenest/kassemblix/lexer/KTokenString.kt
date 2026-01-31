package net.codenest.kassemblix.lexer

import java.io.StringReader

/**
 * A KTokenString is like a String, but it is a series of KTokens rather than a series of chars.
 * Once a KTokenString is created, it is "immutable", meaning it cannot change.
 *
 * @author Steven J. Metsker, Alan K. Sun
 */
class KTokenString private constructor(val tokens: List<KToken>) {

    /**
     * Returns the number of tokens in this token string.
     */
    val length: Int get() = tokens.size

    /**
     * Returns the token at the specified index.
     *
     * @param index the index of the desired token
     * @return the token at the specified index
     */
    operator fun get(index: Int): KToken = tokens[index]

    /**
     * Returns a string representation of this token string.
     */
    override fun toString(): String = tokens.joinToString(" ")

    companion object {
        /**
         * Constructs a KTokenString from the supplied tokens.
         *
         * @param tokens the tokens to use
         * @return a KTokenString constructed from the supplied tokens
         */
        fun fromTokens(tokens: List<KToken>): KTokenString = KTokenString(tokens.toList())

        /**
         * Constructs a KTokenString from the supplied string.
         *
         * @param s the string to tokenize
         * @return a KTokenString constructed from tokens read from the supplied string
         */
        fun fromString(s: String): KTokenString = fromTokenizer(KTokenizer(StringReader(s)))

        /**
         * Constructs a KTokenString from the supplied tokenizer.
         *
         * @param tokenizer the tokenizer that will produce the tokens
         * @return a KTokenString constructed from the tokenizer's tokens
         */
        fun fromTokenizer(tokenizer: KTokenizer): KTokenString {
            val tokens = mutableListOf<KToken>()
            var token = tokenizer.nextToken()
            while (token != KToken.END) {
                if (token != KToken.SKIP) {
                    tokens.add(token)
                }
                token = tokenizer.nextToken()
            }
            return KTokenString(tokens)
        }
    }
}
