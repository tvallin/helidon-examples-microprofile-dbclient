# Helidon MicroProfile DbClient Pokémon Example

This example shows how to use Helidon DB Client with Helidon MP.

## Build and run

Start the database:
```shell
docker run -d \
    --name mysql \
    -e MYSQL_DATABASE=test \
    -e MYSQL_USER=test \
    -e MYSQL_PASSWORD=mysql123 \
    -p 3306:3306 \
    container-registry.oracle.com/mysql/community-server:latest
```

And then:
```shell
mvn package
java -jar target/helidon-examples-microprofile-dbclient.jar
```

## Exercise the application

```shell
# List all Pokémon
curl http://localhost:8080/db/pokemon

# List all Pokémon types
curl http://localhost:8080/db/type

# Get a single Pokémon by id
curl http://localhost:8080/db/pokemon/2

# Get a single Pokémon by name
curl http://localhost:8080/db/pokemon/name/Squirtle

# Add a new Pokémon Rattata
curl -i -X POST -H 'Content-type: application/json' -d '{"id":7,"name":"Rattata","idType":1}' http://localhost:8080/db/pokemon

# Rename Pokémon with id 7 to Raticate
curl -i -X PUT -H 'Content-type: application/json' -d '{"id":7,"name":"Raticate","idType":2}' http://localhost:8080/db/pokemon

# Delete Pokémon with id 7
curl -i -X DELETE http://localhost:8080/db/pokemon/7
```

---

Pokémon, and Pokémon character names are trademarks of Nintendo.
