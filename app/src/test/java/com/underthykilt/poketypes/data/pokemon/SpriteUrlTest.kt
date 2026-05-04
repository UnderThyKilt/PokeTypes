package com.underthykilt.poketypes.data.pokemon

import org.junit.Assert.fail
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL

/**
 * Verifies that every PokemonEntry in the type map has a reachable sprite URL.
 *
 * Run on demand (requires internet):
 *   ./gradlew test --tests "*.SpriteUrlTest"
 */
class SpriteUrlTest {

    @Test
    fun allSpriteUrlsReturn200() {
        val failures = mutableListOf<String>()

        POKEMON.forEach { (types, entries) ->
            val typeLabel = types.joinToString("/") { it.name }
            entries.forEach { entry ->
                val code = headRequest(entry.spriteUrl)
                val tag = if (code == 200) "OK  " else "FAIL"
                println("[$tag] #${entry.id} ${entry.name} ($typeLabel)  ->  HTTP $code")
                if (code != 200) {
                    failures += "#${entry.id} ${entry.name} [$typeLabel] -> HTTP $code  ${entry.spriteUrl}"
                }
            }
        }

        if (failures.isNotEmpty()) {
            fail(
                "\n${failures.size} sprite URL(s) failed:\n" +
                    failures.joinToString("\n")
            )
        }
    }

    private fun headRequest(url: String): Int = try {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "HEAD"
        conn.connectTimeout = 8_000
        conn.readTimeout = 8_000
        conn.instanceFollowRedirects = true
        conn.responseCode.also { conn.disconnect() }
    } catch (e: Exception) {
        -1
    }
}
