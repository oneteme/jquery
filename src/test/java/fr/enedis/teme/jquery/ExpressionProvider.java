package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Operator.EQ;
import static fr.enedis.teme.jquery.Operator.GE;
import static fr.enedis.teme.jquery.Operator.GT;
import static fr.enedis.teme.jquery.Operator.IN;
import static fr.enedis.teme.jquery.Operator.IS_NOT_NULL;
import static fr.enedis.teme.jquery.Operator.IS_NULL;
import static fr.enedis.teme.jquery.Operator.LE;
import static fr.enedis.teme.jquery.Operator.LIKE;
import static fr.enedis.teme.jquery.Operator.LT;
import static fr.enedis.teme.jquery.Operator.NE;
import static fr.enedis.teme.jquery.Operator.NOT_IN;
import static fr.enedis.teme.jquery.Operator.NOT_LIKE;
import static fr.enedis.teme.jquery.ParameterHolder.staticSql;
import static java.lang.String.format;
import static java.util.function.Function.identity;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

public abstract interface ExpressionProvider {

	static final Map<Operator, String> enumMap = enumMap();
	
	
	static Stream<Arguments> caseProviderValuesOnly(){
		return caseProvider().filter(a-> a.get()[3] == staticSql());
	}

	static Stream<Arguments> caseProvider() {
		return Stream.of(
				signCaseProvider(),
				likeCases(),
				inCases(),
				nullCases())
				.flatMap(identity());
	}

	static Stream<Arguments> signCaseProvider() {
		var ph = ParameterHolder.parametredSql();
		return Stream.of(EQ, NE, LT, LE, GT, GE).flatMap(op->{
			var exp = enumMap.get(op);
			return Stream.of(
					Arguments.of(op, exp + "null", null, staticSql()), //int
					Arguments.of(op, exp + "?", 	null, ph),
					Arguments.of(op, exp + "1", 1, staticSql()), //int
					Arguments.of(op, exp + "?", 1, ph),
					Arguments.of(op, exp + "'0'", "0", staticSql()), //string
					Arguments.of(op, exp + "?", "0", ph),
					Arguments.of(op, exp + "'2020-01-01'", LocalDate.of(2020, 1, 1), staticSql()), //date
					Arguments.of(op, exp + "?", LocalDate.of(2020, 1, 1), ph));
		});
	}

	static Stream<Arguments> likeCases() {
		var ph = ParameterHolder.parametredSql();
		return Stream.of(LIKE, NOT_LIKE).flatMap(op->{
			var exp = " " + enumMap.get(op);
			return Stream.of(
					Arguments.of(op, exp + " '1'", "1", staticSql()), //int
					Arguments.of(op, exp + " ?", "'1'", ph),
					Arguments.of(op, exp + " '%test%'", "%test%", staticSql()), //int
					Arguments.of(op, exp + " ?", "%test%", ph));
		});                          
	}                                
                                     
                                     
	static Stream<Arguments> inCases() {
		var ph = ParameterHolder.parametredSql();
		return Stream.of(IN, NOT_IN).flatMap(op->{
			var exp = " " + enumMap.get(op) + "(%s)";
			return Stream.of(
					Arguments.of(op, format(exp, "1,2,3"), new int[] {1,2,3}, staticSql()),
					Arguments.of(op, format(exp, "?,?,?"), new int[] {1,2,3}, ph),
					Arguments.of(op, format(exp, "'1','2','3'"), new String[] {"1","2","3"}, staticSql()),
					Arguments.of(op, format(exp, "?,?,?"), new String[] {"1","2","3"}, ph));
		});
	}

	static Stream<Arguments> nullCases() {
		var ph = ParameterHolder.parametredSql();
		return Stream.of(IS_NULL, IS_NOT_NULL).flatMap(op->{
			var exp = " " + enumMap.get(op);
			return Stream.of(
				Arguments.of(op, exp, null, staticSql()),
				Arguments.of(op, exp, null, ph));
		});
	}
	
	static EnumMap<Operator, String> enumMap() {
		EnumMap<Operator, String> map = new EnumMap<>(Operator.class);
		map.put(EQ, "=");
		map.put(NE, "<>");
		map.put(LT, "<");
		map.put(LE, "<=");
		map.put(GT, ">");
		map.put(GE, ">=");
		map.put(LIKE, "LIKE");
		map.put(NOT_LIKE, "NOT LIKE");
		map.put(IN, "IN");
		map.put(NOT_IN, "NOT IN");
		map.put(IS_NULL, "IS NULL");
		map.put(IS_NOT_NULL, "IS NOT NULL");
		return map;
	}

}
