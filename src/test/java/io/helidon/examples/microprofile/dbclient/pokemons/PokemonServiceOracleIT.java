/*
 * Copyright (c) 2024 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.helidon.examples.microprofile.dbclient.pokemons;

import java.util.List;

import io.helidon.microprofile.testing.junit5.HelidonTest;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static io.helidon.examples.microprofile.dbclient.pokemons.PokemonData.POKEMON_LIST_TYPE;
import static io.helidon.examples.microprofile.dbclient.pokemons.PokemonData.POKEMON_TYPE_LIST_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("SpellCheckingInspection")
@Testcontainers(disabledWithoutDocker = true)
@HelidonTest
class PokemonServiceOracleIT {

    private static final DockerImageName IMAGE = DockerImageName.parse("container-registry.oracle.com/mysql/community-server")
            .asCompatibleSubstituteFor("mysql");

    @Container
    @SuppressWarnings("resource")
    static final MySQLContainer<?> CONTAINER = new MySQLContainer<>(IMAGE)
            .withUsername("test")
            .withPassword("mysql123")
            .withDatabaseName("test");

    private final WebTarget target;

    @Inject
    @SuppressWarnings("CdiInjectionPointsInspection")
    PokemonServiceOracleIT(WebTarget target) {
        this.target = target;
    }

    @BeforeAll
    static void beforeAll() {
        System.setProperty("db.connection.url", CONTAINER.getJdbcUrl());
    }

    @Test
    void testListAllPokemons() {
        Response response = target.path("/db/pokemon").request().get();
        assertThat(response.getStatus(), is(200));
        List<String> names = response.readEntity(POKEMON_LIST_TYPE).stream().map(Pokemon::name).toList();
        assertThat(names, is(PokemonData.POKEMONS.stream().map(Pokemon::name).toList()));
    }

    @Test
    void testListAllPokemonTypes() {
        Response response = target.path("/db/type").request().get();
        assertThat(response.getStatus(), is(200));
        List<String> names = response.readEntity(POKEMON_TYPE_LIST_TYPE).stream().map(PokemonType::name).toList();
        assertThat(names, is(PokemonData.POKEMON_TYPES.stream().map(PokemonType::name).toList()));
    }

    @Test
    void testGetPokemonById() {
        Response response = target.path("/db/pokemon/2").request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(Pokemon.class).name(), is("Charmander"));
    }

    @Test
    void testGetPokemonByName() {
        Response response = target.path("/db/pokemon/name/Squirtle").request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(Pokemon.class).id(), is(3));
    }

    @Test
    void testAddUpdateDeletePokemon() {
        // add a new Pokémon Rattata
        try (Response response = target.path("/db/pokemon").request()
                .post(Entity.entity(new Pokemon(7, "Rattata", 1), MediaType.APPLICATION_JSON))) {
            assertThat(response.getStatus(), is(201));
        }

        // rename Pokémon with id 7 to Raticate
        try (Response response = target.path("/db/pokemon").request()
                .put(Entity.entity(new Pokemon(7, "Raticate", 2), MediaType.APPLICATION_JSON))) {
            assertThat(response.getStatus(), is(200));
        }

        // delete Pokémon with id 7
        try (Response response = target.path("/db/pokemon/7").request().delete()){
            assertThat(response.getStatus(), is(204));
        }

        try (Response response = target.path("/db/pokemon/7").request().get()) {
            assertThat(response.getStatus(), is(404));
        }
    }
}
