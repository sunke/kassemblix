package net.codenest.kassemblix.lexer

/**
 * A KTokenStringSource enumerates over a specified tokenizer, returning KTokenStrings
 * delimited by a specified delimiter.
 *
 * For example:
 * ```
 * val s = "I came; I saw; I left in peace;"
 * val tss = KTokenStringSource(KTokenizer(StringReader(s)), ";")
 * while (tss.hasMoreTokenStrings()) {
 *     println(tss.nextTokenString())
 * }
 * ```
 * prints out:
 * ```
 * I came
 * I saw
 * I left in peace
 * ```
 *
 * @author Steven J. Metsker, Alan K. Sun
 */
class KTokenStringSource(
    private val tokenizer: KTokenizer,
    private val delimiter: String
) : Iterator<KTokenString> {

    private var cachedTokenString: KTokenString? = null
    private var exhausted = false

    /**
     * Returns true if the source has more token strings.
     */
    override fun hasNext(): Boolean {
        ensureCacheIsLoaded()
        return cachedTokenString != null
    }

    /**
     * Returns the next KTokenString from the source.
     */
    override fun next(): KTokenString {
        ensureCacheIsLoaded()
        val result = cachedTokenString ?: throw NoSuchElementException("No more token strings")
        cachedTokenString = null
        return result
    }

    /**
     * Returns the next KTokenString from the source, or null if exhausted.
     */
    fun nextTokenString(): KTokenString? {
        return if (hasNext()) next() else null
    }

    /**
     * Returns true if the source has more token strings.
     */
    fun hasMoreTokenStrings(): Boolean = hasNext()

    private fun ensureCacheIsLoaded() {
        if (cachedTokenString == null && !exhausted) {
            loadCache()
        }
    }

    private fun loadCache() {
        val tokens = mutableListOf<KToken>()
        var token = tokenizer.nextToken()

        while (token != KToken.END) {
            if (token == KToken.SKIP) {
                token = tokenizer.nextToken()
                continue
            }
            if (token.sval == delimiter) {
                break
            }
            tokens.add(token)
            token = tokenizer.nextToken()
        }

        if (tokens.isEmpty() && token == KToken.END) {
            cachedTokenString = null
            exhausted = true
        } else if (tokens.isEmpty()) {
            // Empty segment before delimiter, try next
            loadCache()
        } else {
            cachedTokenString = KTokenString.fromTokens(tokens)
        }
    }
}
