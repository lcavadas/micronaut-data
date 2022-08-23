/*
 * Copyright 2017-2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.data.mongodb.operations;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.PersistentEntity;

/**
 * MongoDB database name provider.
 *
 * @author Denis Stepanov
 * @since 3.8.0
 */
public interface MongoDatabaseNameProvider {

    /**
     * Provides the database name based on the persistent entity and the repository class.
     *
     * @param persistentEntity The persistent entity
     * @param repositoryClass  The repository class used
     * @return The collection name
     */
    @NonNull
    String provide(@NonNull PersistentEntity persistentEntity, @Nullable Class<?> repositoryClass);

    /**
     * Provides the database name based on the persistent entity type.
     *
     * @param type The entity type
     * @return The collection name
     */
    @NonNull
    String provide(@NonNull Class<?> type);

    /**
     * Provides the database name based on the persistent entity and the repository class.
     *
     * @param persistentEntity The persistent entity
     * @return The collection name
     */
    @NonNull
    default String provide(@NonNull PersistentEntity persistentEntity) {
        return provide(persistentEntity, null);
    }

}
