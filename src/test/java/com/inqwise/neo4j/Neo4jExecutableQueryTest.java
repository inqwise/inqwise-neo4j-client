package com.inqwise.neo4j;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class Neo4jExecutableQueryTest {

	@Test
	void constructorExists() {
		assertNotNull(new Neo4jExecutableQuery());
	}
}
