package com.inqwise.neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Record;

import io.vertx.ext.reactivestreams.ReactiveReadStream;

class Neo4jResultTest {

	@Test
	void recordsExposesReactiveStream() {
		Record record = TestRecords.record("value", 10);
		var stub = ReactiveStubs.result(List.of("value"), List.of(record));
		var result = new Neo4jResult(stub);

		ReactiveReadStream<Record> stream = result.records();
		var records = ReadStreamCollector.collect(stream);
		assertEquals(1, records.size());
		assertEquals(10, records.get(0).get("value").asInt());
	}
}
