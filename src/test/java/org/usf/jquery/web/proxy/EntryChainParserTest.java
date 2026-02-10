package org.usf.jquery.web.proxy;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.usf.jquery.web.proxy.EntryChainParser.parseEntry;
import static org.usf.jquery.web.proxy.EntryChainParser.TokenKind.TXT;
import static org.usf.jquery.web.proxy.EntryChainParser.TokenKind.VAL;
import static org.usf.jquery.web.proxy.EntryChainParser.TokenKind.VAR;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.usf.jquery.web.proxy.EntryChain;
import org.usf.jquery.web.EntrySyntaxException;
import org.usf.jquery.web.proxy.EntryChainParser.Lexer;
import org.usf.jquery.web.proxy.EntryChainParser.TokenKind;

/**
 * 
 * @author u$f
 *
 */
class EntryChainParserTest {
	
	@ParameterizedTest
	@MethodSource("numberTestCaseFactory")
	void testParseEntry_Number(String s) {
		testParseEntry_value(s, false);
		testParseEntry_value("-"+s, false);
	}
	
	@ParameterizedTest
	@MethodSource("latinTestCaseFactory")
	void testParseEntry_Latin(String s) {
		testParseEntry_value(s, false);
		testParseEntry_value(s.toUpperCase(), false);
	}
	
	@ParameterizedTest
	@MethodSource("temporalTestCaseFactory")
	void testParseEntry_Temporal(String s) {
		testParseEntry_value(s, false);
	}
	
	@ParameterizedTest
	@MethodSource("otherTestCaseFactory")
	void testParseEntry_Other(String s) {
		testParseEntry_value(s, false);
	}

	@ParameterizedTest
	@MethodSource("variableTestCaseFactory")
	void testParseEntry_Variable(String s) {
		testParseEntry_value(s, true);
		testParseEntry_value(s.toUpperCase(), true);
	}
	
	@ParameterizedTest
	@MethodSource("badIdTestCaseFactory")
	void testParseEntry_badID(String s) {
		testParseEntry_badValue("v."+s);
		testParseEntry_badValue(s+"()");
		testParseEntry_badValue("v:"+s);
	}
	
	void testParseEntry_badValue(String s) {
		assertThrows(EntrySyntaxException.class, ()-> parseEntry(s));
		assertThrows(EntrySyntaxException.class, ()-> parseEntry(s.toUpperCase()));
	}
	
	void testParseEntry_value(String s, boolean exp) {
		var knd = exp ? VAR : VAL;
		assertEquals(new EntryChain(s, knd), parseEntry(s));
		assertEquals(new EntryChain(s, TXT), parseEntry("\""+s+"\""));
		assertEquals(new EntryChain("fn", VAR, new EntryChain[] {new EntryChain(s, knd)}, null, null), parseEntry("fn("+s+")"));
		assertEquals(
				new EntryChain("fn", VAR, new EntryChain[] {new EntryChain(s,knd), new EntryChain(s,knd), new EntryChain(s,knd)}, null, null), 
				parseEntry(format("fn(%s,%s,%s)", s,s,s)));
		if(exp) {
			assertEquals(new EntryChain("obj", knd, null, new EntryChain(s, knd), null), parseEntry("obj."+s)); //as member
			assertEquals(new EntryChain(s, knd, new EntryChain[0], null, null), parseEntry(s+"()")); //as function
			assertEquals(new EntryChain(s, knd, null, null, "tag"), parseEntry(s+":tag")); //using tag
		}
	}

	@Disabled
	@ParameterizedTest
	@ValueSource(strings = {
		"toto_titi@mail.com",
		"dummy",
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
//		"co1.concat(,123)", //null value
//		"co1.concat(123,)", //null value
//		"co1.concat(,,123,)", //null value
		"aa.fn(3.3,b,c,d)",
//		"aa.fn(,3.3,b,c,d,)",
		"aa.fn(2020-01-01,b,c,d)",
	})
	void testParse(String s) {
		assertEquals(s, parseEntry(s).toString());
		assertEquals(s+=":tag", parseEntry(s).toString());
	}

	@Disabled
	@ParameterizedTest
	@ValueSource(strings = {
//		"",
//		"12345",
//		"1name",
//		"_column",
		"column.\"val\"",
		"column.:tag",
		"column.a-b",
//		"_.column",
		"(column",
		")column",
		"\"column",
		",column",
//		":column",
//		".column",
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
//		"fn(3@c)",
		"fn(\"",
		"fn(\"arg",
		"fn(\"arg,",
		"aa.fn(\"a:3,b,c,d)",
		"func@()",
		"()",
		"@()"
//		"aa.fn(\"a:3,b,c,d&\")",
	})
	void testParse2(String s) {
		assertThrows(EntrySyntaxException.class, ()-> parseEntry(s));
	}
	
	static Stream<Arguments> temporalTestCaseFactory() {
		return Stream.of(
				arguments(Year.now().toString()),
				arguments(YearMonth.now().toString()),
				arguments(MonthDay.now().toString()),
				arguments(LocalTime.now().toString()),
				arguments(LocalDate.now().toString()),
				arguments(LocalDateTime.now().toString()),
				arguments(OffsetDateTime.now().toString()),
				arguments(ZonedDateTime.now().toString()),
				arguments(Instant.now().toString()));
	}
	
	static Stream<Arguments> otherTestCaseFactory() {
		return Stream.of(
				arguments(UUID.randomUUID().toString()),
				arguments("usf-alami@gmail.com"),
				arguments("#JQuery"));
	}

	static Stream<Arguments> numberTestCaseFactory() {
		return Stream.of(
				arguments("1"),
				arguments("12"),
				arguments("123"),
				arguments(".1"),
				arguments(".12"),
				arguments(".123"),
				arguments("1.23"),
				arguments("12.3"),
				arguments("123."),
				arguments("1E-3"),
				arguments(".1E-2"),
				arguments(".01E-1"));
	}
	
	static Stream<Arguments> latinTestCaseFactory() {
		return Stream.of(
				arguments("c'est"),
				arguments("aujourd'hui"),
				arguments("œuvre"),
				arguments("nœud"),
				arguments("âgé"),
				arguments("câble"),
				arguments("être"),
				arguments("guêpe"),
				arguments("île"),
				arguments("abîmé"),				
				arguments("côté"),
				arguments("impôt"),
				arguments("kärcher"),
				arguments("citroën"),
				arguments("maïs"),
				arguments("hawaï"),
				arguments("ça"),
				arguments("français"),
				arguments("µtorrent"));	
	}
	
	static Stream<Arguments> variableTestCaseFactory() {
		return Stream.of(
				arguments("v"),
				arguments("v0"),
				arguments("v_12"),
				arguments("v1_2"),
				arguments("v12_"),
				arguments("function"),
				arguments("came1CaseFn"),
				arguments("snake_ca5e_fn"));	
	}
	
	static Stream<Arguments> badIdTestCaseFactory() {
		return Stream.of(
				arguments(""),
				arguments("1"),
				arguments("123"),
				arguments("1ab"),
				arguments("_"),
				arguments("_12"),
				arguments("_ab"),
				arguments("_çà"),
				arguments("à"),
				arguments("àbc"),
				arguments("abç"));	
	}
}
