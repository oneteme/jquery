package org.usf.jquery.web;

import static java.util.Collections.emptyList;
import static org.usf.jquery.web.RequestParser.parseEntry;

import org.junit.jupiter.api.Test;

class RequestEntryChainTest {

	@Test
	void test() {
		JQueryContext.register(emptyList(), emptyList());
		parseEntry("count()").evalColumn(new ViewDecorator() {
			
			@Override
			public String identity() {
				return null;
			}
			
			@Override
			public String columnName(ColumnDecorator cd) {
				return null;
			}
		});
	}

}
