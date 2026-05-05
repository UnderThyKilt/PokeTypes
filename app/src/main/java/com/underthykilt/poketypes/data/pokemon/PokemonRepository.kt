package com.underthykilt.poketypes.data.pokemon

import android.content.Context
import com.underthykilt.poketypes.data.PokemonType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface PokemonRepository {
    fun randomForTypes(vararg types: PokemonType, maxGeneration: Int = 9): PokemonEntry?
    fun hasForTypes(vararg types: PokemonType, maxGeneration: Int = 9): Boolean
}

@Serializable
private data class PokemonEntryJson(
    val id: Int,
    val name: String,
    val generation: Int,
    val types: List<String>,
)

class AssetPokemonRepository(context: Context) : PokemonRepository {

    private val pokemon: Map<Set<PokemonType>, List<PokemonEntry>> by lazy {
        val raw = context.assets.open("pokemon.json").bufferedReader().readText()
        val entries = Json.decodeFromString<List<PokemonEntryJson>>(raw)
        val map = mutableMapOf<Set<PokemonType>, MutableList<PokemonEntry>>()
        for (e in entries) {
            val typeSet = e.types.map { PokemonType.valueOf(it) }.toSet()
            map.getOrPut(typeSet) { mutableListOf() }.add(PokemonEntry(e.id, e.name, e.generation))
        }
        map
    }

    override fun randomForTypes(vararg types: PokemonType, maxGeneration: Int): PokemonEntry? =
        pokemon[types.toSet()]?.filter { it.generation <= maxGeneration }?.randomOrNull()

    override fun hasForTypes(vararg types: PokemonType, maxGeneration: Int): Boolean =
        pokemon[types.toSet()]?.any { it.generation <= maxGeneration } == true
}
