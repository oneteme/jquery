package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.GenericColumn.c1;
import static fr.enedis.teme.jquery.GenericColumn.c2;
import static fr.enedis.teme.jquery.GenericColumn.c3;
import static fr.enedis.teme.jquery.GenericColumn.c4;
import static fr.enedis.teme.jquery.Helper.array;
import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.LogicalOperator.OR;
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
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

public abstract interface DataProvider {
	
	static Stream<Arguments> filterGroupCaseProvider() {
		var rand = new Random();
		var list = operationCaseProvider().collect(toList());
		return IntStream.range(1, 5)
			.mapToObj(n->{
				var exList = new ArrayList<DBFilter>();
				var coList = new ArrayList<DBColumn>();
				var sqList = IntStream.range(0, 2).mapToObj(i-> new ArrayList<String>(n)).collect(toList());
				for(int i=0; i<n; i++) {
					var arg = list.get(rand.nextInt(list.size()));
					var col = COLUMNS[rand.nextInt(4)];
					coList.add(col);
					exList.add(new ColumnFilter(col, new OperatorExpression<>((Operator)arg.get()[0], arg.get()[1])));
					String[] sql = (String[]) arg.get()[2];
					sqList.get(0).add(sql[0]);
					sqList.get(1).add(sql[1]);
				}
				return of(new ColumnFilterGroup(rand.nextBoolean() ? AND : OR, exList.toArray(DBFilter[]::new)),
						coList.toArray(DBColumn[]::new),
						sqList.stream().map(c-> c.toArray(String[]::new)).toArray(String[][]::new));
			});
	}
	
	static Stream<Arguments> filterCaseProvider() {
		var rand = new Random();
		return operationCaseProvider()
			.map(a->
				of(new ColumnFilter(COLUMNS[rand.nextInt(4)], new OperatorExpression<>((Operator)a.get()[0], a.get()[1])), a.get()[2]));
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

	DBColumn[] COLUMNS = {c1,c2,c3,c4};

	static final LocalDate date = LocalDate.now();
	static final Map<Operator, String> enumMap = enumMap();

}
