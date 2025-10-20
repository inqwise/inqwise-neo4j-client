package com.inqwise.neo4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.Record;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.InternalRecord;

final class TestRecords {

	private TestRecords() {
	}

	static Record record(String key, Object value) {
		return new InternalRecord(List.of(key), List.of(Values.value(value)));
	}

	static Record record(Map<String, Object> entries) {
		List<String> keys = new ArrayList<>();
		List<org.neo4j.driver.Value> values = new ArrayList<>();
		for (Map.Entry<String, Object> entry : entries.entrySet()) {
			keys.add(entry.getKey());
			values.add(Values.value(entry.getValue()));
		}
		return new InternalRecord(keys, values);
	}
}
