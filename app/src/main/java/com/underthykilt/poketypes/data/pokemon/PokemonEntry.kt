package com.underthykilt.poketypes.data.pokemon

data class PokemonEntry(val id: Int, val name: String, val generation: Int) {
    val spriteUrl: String
        get() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
}
