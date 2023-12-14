package org.usf.jquery.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.usf.jquery.web.RequestParser.parse;

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
		"col123",
		"c.n",
		"noArgFn()",
		"fn(a,b,c,d)",
		"trunc(3.55,44)",
		"mod(abs(trunc(exp())))",
		"co1.mod(6.acc).trunc(3).plus(100)", //6.acc as value
		"co1.concat(co1.trunc(2).string(10), 1234)", //6.acc as value
		"co1.concat(,123)", //null value
		"aa.fn(3.3,b,c,d)",
		"aa.fn(2020-01-01,b,c,d)",
//		"aa.fn(\"a:3'\",b,c,d)", //pass
	})
	void testParse(String s) {
		assertEquals(s, parse(s).toString());
		assertEquals(s+=":tag", parse(s).toString());
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"column)",
		"column(",
		"column(1,2,3",
		"column(1,2,3))",
		"column:",
		"column.",
		"1ab",
		"123",
		"123(a,b,c)",
		"123.abc",
		"aa.fn(a:3,b,c,d)",
		"aa.fn(\"a:3,b,c,d)",
		"aa.fn(\"a:3,b,c,d&\")",
	})
	void testParse2(String s) {
		assertThrows(IllegalArgumentException.class, ()-> parse(s));
	}
}
