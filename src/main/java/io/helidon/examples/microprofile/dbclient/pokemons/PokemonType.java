package io.helidon.examples.microprofile.dbclient.pokemons;

/**
 * POJO representing Pokémon type.
 *
 * @param id     id of the type
 * @param name   name of the type
 */
public record PokemonType(int id, String name) {
}
