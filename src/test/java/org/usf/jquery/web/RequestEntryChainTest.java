package org.usf.jquery.web;

import static java.util.Collections.emptyList;
import static org.usf.jquery.web.RequestParser.parseEntry;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class RequestEntryChainTest {

	@Test
	void test() {
		JQueryContext.register(emptyList(), emptyList());
		parseEntry("count()").asColumn(new TableDecorator() {
			
			@Override
			public String tableName() {
				return null;
			}
			
			@Override
			public String identity() {
				return null;
			}
			
			@Override
			public Optional<String> columnName(ColumnDecorator cd) {
				return Optional.empty();
			}
		});
	}

}
