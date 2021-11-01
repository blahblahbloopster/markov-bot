import kotlin.collections.HashMap
import kotlin.random.Random

class MarkovChain {
    private val words = HashMap<String, HashMap<String, Double>>()
    private val count = 5

    private fun randomWord(previous: String?): String {
        val entry = words[previous]

        entry ?: run {
            if (previous != null) {
                val startsWith = words.keys.find { it.startsWith(previous) }
                if (startsWith != null) {
//                    println("Couldn't find continuing word for '$previous', but found '$startsWith'")
                    return startsWith.removePrefix("$previous ").split(' ').first()  // todo: weighted
                }
            }

            if (previous?.contains(' ') == true) {
//                println("Couldn't find continuing word for '$previous', dropping extra and trying again")
                return randomWord(previous.split(' ').drop(1).joinToString(" "))
            }

//            println("Couldn't find continuing word for '$previous', picking random")
            var sum = words.values.sumOf { it.size } * Random.nextDouble()
            return words.entries.first { sum -= it.value.size; it.value.size > sum }.value.keys.random()
        }

        var thresh = entry.values.sum() * Random.nextDouble()
        return entry.entries.firstOrNull { thresh -= it.value; it.value > thresh }?.key!!//.apply { println("Found $this to go after $previous") }
    }

    fun generateSentence(inp: String?): String {
        val inpWords = inp?.split(" ")
        val output = mutableListOf(randomWord(inpWords?.takeLast(count)?.joinToString(" ") ?: randomWord(null)))
        if (inpWords != null) output.addAll(0, inpWords)
        for (i in 0..15) {
            output.add(randomWord(output.takeLast(count).joinToString(" ")))
        }
        return output.joinToString(" ")
    }

    private fun String.stripWhitespace() = dropWhile { it.isWhitespace() }.dropLastWhile { it.isWhitespace() }

    fun train(lines: List<String>) {
        val r = "([A-Z]+\\s?)+ ?\\(?[A-Z]+\\)?".toRegex()
        val inp = lines.filter { !it.matches(r) && it.isNotBlank() }.joinToString(" ").lowercase()
        val sentences = inp.split('.', '?', '!')

        val whitespaceStripped = StringBuilder()
        for (s in sentences) {
            whitespaceStripped.clear()
            var lastWasWhitespace = false
            for (c in s) {
                if (lastWasWhitespace && c.isWhitespace()) {
                    lastWasWhitespace = c.isWhitespace()
                    continue
                }
                lastWasWhitespace = c.isWhitespace()
                whitespaceStripped.append(c)
            }
            val parsed = whitespaceStripped.toString().stripWhitespace().split(' ')
            val previous = mutableListOf(parsed.first())
            for (w in parsed.drop(1)) {
                if (w.isBlank()) continue
                val map = words.getOrPut(previous.takeLast(count).joinToString(" ")) { hashMapOf() }
                val item = map[w] ?: 0.0
                map[w] = item + 1.0
                previous.add(w)
            }
        }
    }
}
