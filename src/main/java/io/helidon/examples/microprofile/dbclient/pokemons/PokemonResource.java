package io.helidon.examples.microprofile.dbclient.pokemons;

import java.util.List;

import io.helidon.config.Config;
import io.helidon.dbclient.DbClient;
import io.helidon.dbclient.DbExecute;
import io.helidon.dbclient.DbTransaction;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("/db")
@ApplicationScoped
public class PokemonResource {

    private static final System.Logger LOGGER = System.getLogger(PokemonResource.class.getName());

    private final DbClient dbClient;
    private final boolean initSchema;
    private final boolean initData;

    @Inject
    @SuppressWarnings("CdiInjectionPointsInspection")
    PokemonResource(Config config) {
        dbClient = DbClient.create(config.get("db"));
        initSchema = config.get("init-schema").asBoolean().orElse(true);
        initData = config.get("init-data").asBoolean().orElse(true);
        init();
    }

    private void init() {
        if (initSchema) {
            initSchema();
        }
        if (initData) {
            initData();
        }
    }

    private void initSchema() {
        DbExecute exec = dbClient.execute();
        try {
            exec.namedDml("create-types");
            exec.namedDml("create-pokemons");
        } catch (Exception ex1) {
            LOGGER.log(System.Logger.Level.WARNING, "Could not create tables", ex1);
            try {
                deleteData();
            } catch (Exception ex2) {
                LOGGER.log(System.Logger.Level.WARNING, "Could not delete tables", ex2);
            }
        }
    }

    private void initData() {
        DbTransaction tx = dbClient.transaction();
        try {
            for (PokemonType type : PokemonData.POKEMON_TYPES) {
                tx.createNamedInsert("insert-type")
                        .indexedParam(type)
                        .execute();
            }
            for (Pokemon pokemon : PokemonData.POKEMONS) {
                tx.createNamedInsert("insert-pokemon")
                        .indexedParam(pokemon)
                        .execute();
            }
            tx.commit();
        } catch (Throwable t) {
            tx.rollback();
            throw t;
        }
    }

    private void deleteData() {
        DbTransaction tx = dbClient.transaction();
        try {
            tx.namedDelete("delete-all-pokemons");
            tx.namedDelete("delete-all-types");
            tx.commit();
        } catch (Throwable t) {
            tx.rollback();
            throw t;
        }
    }

    @GET
    @Produces("text/plain")
    public String index() {
        return """
                Pokemon JDBC Example:
                     GET /type                - List all pokemon types
                     GET /pokemon             - List all pokemons
                     GET /pokemon/{id}        - Get pokemon by id
                     GET /pokemon/name/{name} - Get pokemon by name
                     POST /pokemon            - Insert new pokemon:
                                                {"id":<id>,"name":<name>,"type":<type>}
                     PUT /pokemon             - Update pokemon
                                                {"id":<id>,"name":<name>,"type":<type>}
                     DELETE /pokemon/{id}     - Delete pokemon with specified id
                """;
    }


    @GET
    @Path("/type")
    @Produces("application/json")
    public List<PokemonType> listTypes() {
        return dbClient.execute()
                .namedQuery("select-all-types")
                .map(row -> row.as(PokemonType.class))
                .toList();
    }

    @GET
    @Path("/pokemon")
    @Produces("application/json")
    public List<Pokemon> listPokemons() {
        return dbClient.execute().namedQuery("select-all-pokemons")
                .map(row -> row.as(Pokemon.class))
                .toList();
    }

    @GET
    @Path("/pokemon/{id}")
    @Produces("application/json")
    public Pokemon getPokemonById(@PathParam("id") int pokemonId) {
        return dbClient.execute().createNamedGet("select-pokemon-by-id")
                .addParam("id", pokemonId)
                .execute()
                .orElseThrow(() -> new NotFoundException("Pokemon " + pokemonId + " not found"))
                .as(Pokemon.class);
    }

    @GET
    @Path("/pokemon/name/{name}")
    @Produces("application/json")
    public Pokemon getPokemonByName(@PathParam("name") String pokemonName) {
        return dbClient.execute().namedGet("select-pokemon-by-name", pokemonName)
                .orElseThrow(() -> new NotFoundException("Pokemon " + pokemonName + " not found"))
                .as(Pokemon.class);
    }

    @POST
    @Path("/pokemon")
    @Consumes("application/json")
    @Produces("text/plain")
    public Response insertPokemon(Pokemon pokemon) {
        long count = dbClient.execute()
                .createNamedInsert("insert-pokemon")
                .indexedParam(pokemon)
                .execute();
        return Response.status(Response.Status.CREATED)
                .entity("Inserted: " + count + " values\n")
                .build();
    }


    @PUT
    @Path("/pokemon")
    @Consumes("application/json")
    @Produces("text/plain")
    public String updatePokemon(Pokemon pokemon) {
        long count = dbClient.execute().createNamedUpdate("update-pokemon-by-id")
                .namedParam(pokemon)
                .execute();
        return "Updated: " + count + " values\n";
    }

    @DELETE
    @Path("/pokemon/{id}")
    public void deletePokemonById(@PathParam("id") int pokemonId) {
        dbClient.execute().createNamedDelete("delete-pokemon-by-id")
                .addParam("id", pokemonId)
                .execute();
    }
}
