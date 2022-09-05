/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.data.cosmos.config;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

/**
 * The Azure Cosmo Client factory.
 *
 * @author Denis Stepanov
 * @since TODO
 */
@Factory
public final class CosmoClientFactory {

    @Bean(preDestroy = "close")
    @Requires(beans = CosmoClientConfiguration.class)
    CosmosClient buildCosmosClient(CosmoClientConfiguration configuration) {
        return configuration.getCosmosClientBuilder().buildClient();
    }

    @Bean(preDestroy = "close")
    @Requires(beans = CosmoClientConfiguration.class)
    CosmosAsyncClient buildCosmosAsyncClient(CosmoClientConfiguration configuration) {
        return configuration.getCosmosClientBuilder().buildAsyncClient();
    }

    @Bean
    @Requires(beans = CosmoClientConfiguration.class)
    ThroughputConfiguration throughputConfiguration(CosmoClientConfiguration configuration) {
        return configuration.getThroughputConfiguration();
    }

}
