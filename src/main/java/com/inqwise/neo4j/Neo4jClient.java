package com.inqwise.neo4j;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;

/**
 * Entry point used to create {@link Neo4jDriver} instances backed by the official Neo4j Java driver.
 */
public final class Neo4jClient {

	private Neo4jClient() {
	}

	/**
	 * Creates a {@link Neo4jDriver} connected to the supplied Bolt endpoint.
	 *
	 * @param url      the Bolt URL of the Neo4j server
	 * @param username the login user name
	 * @param password the login password
	 * @return a Vert.x friendly driver wrapper
	 */
	public static Neo4jDriver create(String url, String username, String password) {
		return new Neo4jDriver(GraphDatabase.driver(url, AuthTokens.basic(username, password)));
	}
}
