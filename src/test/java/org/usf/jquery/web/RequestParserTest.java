package org.usf.jquery.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.usf.jquery.web.RequestParser.parseEntry;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * 
 * @author u$f
 *
 */
class RequestParserTest {

	@ParameterizedTest
	@ValueSource(strings = {
		"dymmy",
		"camelCaseCol0123",
		"snake_column_123",
		"view.column",
		"view.column.fn1.fn2.exp",
		"noArgFn()",
		"oneArgFn(c1)",
		"concat(a,b,c,d)",
		"trunc(3.55,2)",
		"convert(2020-01-01)",
		"extract(2020-01-01T00:00:00Z,epoch)",
		"timestamp_diff(2022-01-01T00:00:00+01:00,2020-01-01T00:00:00+07:30)",
		"concat(v.c,\"abc\",\"123\")",
		"mod(abs(trunc(exp())))",
		"co1.mod(6.acc).trunc(3).plus(100)", //6.acc as value
		"co1.concat(co1.trunc(2).string(10),1234)", //6.acc as value
		"co1.concat(,123)", //null value
		"co1.concat(123,)", //null value
		"co1.concat(,,123,)", //null value
		"aa.fn(3.3,b,c,d)",
		"aa.fn(,3.3,b,c,d,)",
		"aa.fn(2020-01-01,b,c,d)",
	})
	void testParse(String s) {
		assertEquals(s, parseEntry(s).toString());
		assertEquals(s+=":tag", parseEntry(s).toString());
	}

	@ParameterizedTest
	@ValueSource(strings = {
//		"",
//		"12345",
//		"1name",
		"_column",
		"(column",
		")column",
		"\"column",
		",column",
		":column",
		".column",
		"column(",
		"column)",
		"column\"",
		"column,",
		"column:",
		"column.",
//		"\"column\"",
		"function(arg",
		"function(arg))",
		"123(a,b,c)",
//		"123.abc",
		"fn(3@c)",
		"fn(\"",
		"fn(\"arg",
		"fn(\"arg,",
		"aa.fn(\"a:3,b,c,d)",
		"aa.fn(\"a:3,b,c,d&\")",
	})
	void testParse2(String s) {
		assertThrows(EntrySyntaxException.class, ()-> parseEntry(s));
	}
}
