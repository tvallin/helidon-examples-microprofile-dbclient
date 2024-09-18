package io.helidon.examples.microprofile.dbclient.pokemons;

import java.io.InputStream;
import java.util.List;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.core.GenericType;

class PokemonData {
    private static final Jsonb JSONB = JsonbBuilder.create();

    static final GenericType<List<Pokemon>> POKEMON_LIST_TYPE = new GenericType<>() {
    };
    static final GenericType<List<PokemonType>> POKEMON_TYPE_LIST_TYPE = new GenericType<>() {
    };
    static final List<Pokemon> POKEMONS = readPokemons();
    static final List<PokemonType> POKEMON_TYPES = readTypes();

    private PokemonData() {
    }

    private static List<Pokemon> readPokemons() {
        InputStream is = PokemonData.class.getResourceAsStream("/pokemons.json");
        return JSONB.fromJson(is, POKEMON_LIST_TYPE.getType());
    }

    private static List<PokemonType> readTypes() {
        InputStream is = PokemonData.class.getResourceAsStream("/pokemon-types.json");
        return JSONB.fromJson(is, POKEMON_TYPE_LIST_TYPE.getType());
    }
}
