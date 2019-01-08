/*
 * Copyright (c)  [2011-2019] "Pivotal Software, Inc." / "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.springframework.data.neo4j.test;

import java.util.Optional;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.neo4j.test.Neo4jIntegrationTest.IntegrationTestMode;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * This class represents the configuration of a Spring Data Neo4j integration test.
 *
 * @author Michael J. Simons
 * @since 5.2
 * @soundtrack Die Ärzte - Nach uns die Sintflut
 */
@Configuration
@EnableTransactionManagement
class Neo4jTestServerConfiguration {

	@Bean
	ServerControls neo4jTestServer() {
		return TestServerBuilders.newInProcessBuilder().newServer();
	}

	@Bean
	GraphDatabaseService graphDatabaseService(ServerControls neo4jTestServer) {
		return neo4jTestServer.graph();
	}

	@Bean
	@Conditional(OnMissingOGMConfigurationCondition.class)
	org.neo4j.ogm.config.Configuration neo4jOGMConfiguration(ServerControls neo4jTestServer) {

		// @Value isn't used on purpose. Some tests interfer with the conversion services and
		// it would be much more effort to use Springs system property integration and SpEL than
		// this manual setup.
		IntegrationTestMode integrationTestMode = Optional.ofNullable(System.getProperty("integration-test-mode"))
				.map(IntegrationTestMode::valueOf).orElse(IntegrationTestMode.BOLT);

		String uri;
		switch (integrationTestMode) {
			case BOLT:
				uri = neo4jTestServer.boltURI().toString();
				break;
			case HTTP:
				uri = neo4jTestServer.httpURI().toString();
				break;
			case EMBEDDED:
				uri = null; // This is on purpose, the configuration than uses embedded.
				break;
			default:
				throw new UnsupportedOperationException("Unsupported mode for integration tests: " + integrationTestMode);
		}

		return new org.neo4j.ogm.config.Configuration.Builder() //
				.uri(uri) //
				.build();
	}

	@Bean
	public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
		return new Neo4jTransactionManager(sessionFactory);
	}

	@Bean
	public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
		return new TransactionTemplate(transactionManager);
	}

	static class OnMissingOGMConfigurationCondition implements ConfigurationCondition {

		@Override
		public ConfigurationPhase getConfigurationPhase() {
			return ConfigurationPhase.REGISTER_BEAN;
		}

		@Override
		public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {

			ObjectProvider<org.neo4j.ogm.config.Configuration> objectProvider = conditionContext.getBeanFactory()
					.getBeanProvider(org.neo4j.ogm.config.Configuration.class);

			boolean configurationAvailable = objectProvider.getIfUnique() != null;
			return !configurationAvailable;
		}
	}
}
