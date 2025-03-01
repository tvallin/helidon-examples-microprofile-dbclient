/*
 * Copyright (c) 2019, 2024 Oracle and/or its affiliates.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.helidon.dbclient.DbColumn;
import io.helidon.dbclient.DbMapper;
import io.helidon.dbclient.DbRow;

/**
 * Maps database statements to {@link PokemonType} class.
 */
public class PokemonTypeMapper implements DbMapper<PokemonType> {

    @Override
    public PokemonType read(DbRow row) {
        DbColumn id = row.column("id");
        DbColumn name = row.column("name");
        return new PokemonType(id.get(Integer.class), name.get(String.class));
    }

    @Override
    public Map<String, Object> toNamedParameters(PokemonType value) {
        Map<String, Object> map = new HashMap<>(2);
        map.put("id", value.id());
        map.put("name", value.name());
        return map;
    }

    @Override
    public List<Object> toIndexedParameters(PokemonType value) {
        List<Object> list = new ArrayList<>(2);
        list.add(value.id());
        list.add(value.name());
        return list;
    }
}
