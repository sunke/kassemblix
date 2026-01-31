package net.codenest.kassemblix.parser

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.lexer.KTokenizer
import java.io.Reader
import java.io.StringReader

class KTokenAssembly(reader: Reader, delimiter: String = "/"): KAssembly<KToken>(delimiter) {

    constructor(str: String, delimiter: String = "/"): this(StringReader(str), delimiter)

    init {
        val tokenizer = KTokenizer(reader)
        var next = tokenizer.nextToken()
        while (next != KToken.END) {
            if (next != KToken.START && next != KToken.SKIP) {
                addItem(next)
            }
            next = tokenizer.nextToken()
        }
    }
}