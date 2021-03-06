/*
 * Copyright 2011-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.neo4j.documentation.repositories.populators;

// tag::populators[]

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.repository.init.Jackson2RepositoryPopulatorFactoryBean;
import org.springframework.data.repository.init.ResourceReaderRepositoryPopulator;

import com.fasterxml.jackson.databind.ObjectMapper;

// end::populators[]

/**
 * An example how to configure a repository populator.
 *
 * @author Michael J. Simons
 * @soundtrack Rammstein - Reise Reise
 */
// tag::populators[]
@Configuration
public class PopulatorConfig {

	@Bean
	public FactoryBean<ResourceReaderRepositoryPopulator> respositoryPopulator(ObjectMapper objectMapper, // <1>
			ResourceLoader resourceLoader) {

		Jackson2RepositoryPopulatorFactoryBean factory = new Jackson2RepositoryPopulatorFactoryBean();
		factory.setMapper(objectMapper);
		factory.setResources(new Resource[] { resourceLoader.getResource("classpath:data.json") }); // <2>
		return factory;
	}
}
// end::populators[]
