package net.codenest.kassemblix.lexer

import java.io.PushbackReader

/**
 * A tokenizerState returns a token, given a reader, an initial character read from the reader, and a tokenizer
 * that is conducting an overall tokenization of the reader. The tokenizer will typically have a character state
 * table that decides which state to use, depending on an initial character. If a single character is insufficient,
 * a state such as `SlashState` will read a second character, and may delegate to another state,
 * such as `SlashStarState`. This prospect of delegation is the reason that the `nextToken()`
 * method has a tokenizer argument.
 *
 * @author Steven J. Metsker, Alan K. Sun
 */
interface KTokenizerState {
    fun nextToken(ch: Char, reader: PushbackReader): KToken
}