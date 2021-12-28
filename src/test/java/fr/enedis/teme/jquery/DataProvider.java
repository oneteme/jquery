package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.GenericColumn.*;
import static fr.enedis.teme.jquery.Helper.array;
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
import static java.lang.String.format;
import static java.util.function.Function.identity;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

public abstract interface DataProvider {
	
	static Stream<Arguments> filterCaseProvider() {
		var cols = new DBColumn[] {c1,c2,c3,c4};
		var rand = new Random();
		return operationCaseProvider()
			.map(a->
				of(new ColumnFilter(cols[rand.nextInt(4)], new OperatorExpression<>((Operator)a.get()[0], a.get()[1])), a.get()[2]));
	}
		

	static Stream<Arguments> expressionCaseProvider() {
		return operationCaseProvider()
			.map(a-> 
				of(new OperatorExpression<>((Operator)a.get()[0], a.get()[1]), a.get()[2]));
	}
		
	static Stream<Arguments> operationCaseProvider() {
		return Stream.of(signCaseProvider(),likeCases(),inCases(), nullCases())
				.flatMap(identity());
	}

	static Stream<Arguments> signCaseProvider() {
		return Stream.of(EQ, NE, LT, LE, GT, GE).flatMap(op->{
			var exp = enumMap.get(op);
			return Stream.of(
				of(op, null, array(exp + "?", exp + "null")), //null
				of(op,    1, array(exp + "?", exp + "1")), //int
				of(op,  "0", array(exp + "?", exp + "'0'")), //string
				of(op, date, array(exp + "?", exp + "'"+date+"'"))); //date
		});
	}

	static Stream<Arguments> likeCases() {
		return Stream.of(LIKE, NOT_LIKE).flatMap(op->{
			var exp = " " + enumMap.get(op) + " ";
			return Stream.of(
				of(op,   "123", array(exp + "?", exp + "'123'")),
				of(op, "%expr", array(exp + "?", exp + "'%expr'")),
				of(op, "val__", array(exp + "?", exp + "'val__'")));
		});                          
	}                                
                                     
                                     
	static Stream<Arguments> inCases() {
		return Stream.of(IN, NOT_IN).flatMap(op->{
			var exp = " " + enumMap.get(op) + "(%s)";
			return Stream.of(
				of(op,  new int[]{1,2,3,4,5}, array(format(exp, "?,?,?,?,?"), format(exp, "1,2,3,4,5"))), //int  array
				of(op, new String[]{"1","2"}, array(format(exp, "?,?"),       format(exp, "'1','2'"))), //string array
				of(op, new LocalDate[]{date}, array(format(exp, "?"),         format(exp, "'"+date+"'")))); //date array
		});
	}

	static Stream<Arguments> nullCases() {
		return Stream.of(IS_NULL, IS_NOT_NULL).flatMap(op->{
			var exp = " " + enumMap.get(op);
			return Stream.of(of(op, null, array(exp, exp)));
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

	static final LocalDate date = LocalDate.now();
	static final Map<Operator, String> enumMap = enumMap();

}
