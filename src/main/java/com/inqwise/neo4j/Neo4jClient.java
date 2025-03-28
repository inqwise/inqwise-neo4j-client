package com.inqwise.neo4j;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;

public class Neo4jClient {
	public static Neo4jDriver create(String url, String username, String password) {
		return new Neo4jDriver(GraphDatabase.driver(url, AuthTokens.basic(username, password)));
	}
}
