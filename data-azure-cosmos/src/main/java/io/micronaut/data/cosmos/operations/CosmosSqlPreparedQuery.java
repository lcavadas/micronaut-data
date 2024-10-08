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
package io.micronaut.data.cosmos.operations;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.query.builder.sql.AbstractSqlLikeQueryBuilder2;
import io.micronaut.data.model.query.builder.sql.SqlQueryBuilder2;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.QueryParameterBinding;
import io.micronaut.data.model.runtime.StoredQuery;
import io.micronaut.data.runtime.operations.internal.sql.DefaultSqlPreparedQuery;

/**
 * Azure Cosmos DB implementation of {@link io.micronaut.data.runtime.operations.internal.sql.SqlPreparedQuery}.
 *
 * @author radovanradic
 * @since 3.9.0
 *
 * @param <E> The entity type
 * @param <R> The result type
 */
@Internal
final class CosmosSqlPreparedQuery<E, R> extends DefaultSqlPreparedQuery<E, R> {

    public CosmosSqlPreparedQuery(PreparedQuery<E, R> preparedQuery) {
        super(preparedQuery);
    }

    /**
     * Check if query need to be modified to expand parameters.
     *
     * @param entity The entity instance
     */
    @Override
    public void prepare(E entity) {
        if (isExpandableQuery()) {
            SqlQueryBuilder2 queryBuilder = sqlStoredQuery.getQueryBuilder();
            StringBuilder q = new StringBuilder(sqlStoredQuery.getExpandableQueryParts()[0]);
            int queryParamIndex = 1;
            int inx = 1;
            for (QueryParameterBinding parameter : sqlStoredQuery.getQueryBindings()) {
                AbstractSqlLikeQueryBuilder2.Placeholder placeholder = queryBuilder.formatParameter(inx++);
                if (!parameter.isExpandable()) {
                    q.append(placeholder.name());
                } else {
                    appendExpandedParameter(q, parameter, placeholder.name());
                }
                q.append(sqlStoredQuery.getExpandableQueryParts()[queryParamIndex++]);
            }
            this.query = q.toString();
        }
    }

    /**
     * @return the update statement for update operation
     */
    public String getUpdate() {
        CosmosSqlStoredQuery<E, R> cosmosSqlStoredQuery = getCosmosSqlStoredQuery(sqlStoredQuery);
        return cosmosSqlStoredQuery.getUpdate();
    }

    private <T, K> CosmosSqlStoredQuery<T, K> getCosmosSqlStoredQuery(StoredQuery<T, K> storedQuery) {
        if (storedQuery instanceof CosmosSqlStoredQuery<T, K> cosmosSqlStoredQuery) {
            return cosmosSqlStoredQuery;
        }
        throw new IllegalStateException("Expected for stored query query to be of type: CosmosSqlStoredQuery got: " + sqlStoredQuery.getClass().getName());
    }

    private void appendExpandedParameter(StringBuilder q, QueryParameterBinding parameter, String parameterName) {
        int size = Math.max(1, getQueryParameterValueSize(parameter));
        for (int k = 0; k < size; k++) {
            if (size == 1) {
                q.append(String.format("%s", parameterName));
            } else {
                q.append(String.format("%s_%d", parameterName, k + 1));
            }
            if (k + 1 != size) {
                q.append(",");
            }
        }
    }
}
