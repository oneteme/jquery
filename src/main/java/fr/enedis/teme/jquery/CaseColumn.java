package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.ExpressionColumnGroup.and;
import static fr.enedis.teme.jquery.ParameterHolder.formatString;
import static fr.enedis.teme.jquery.ParameterHolder.staticSql;
import static fr.enedis.teme.jquery.Utils.isBlank;
import static fr.enedis.teme.jquery.Utils.isEmpty;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static java.util.Arrays.sort;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Map.entry;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CaseColumn implements DBColumn {

	@Getter
	private final String mappedName;
	private final Collection<DBFilter> filters;
	private final String defaultTag;
	
	@Override
	public String sql(DBTable table, ParameterHolder pc) {
		return pc.staticMode(()-> new SqlStringBuilder(filters.size() * 50) //force static values
				.append("CASE ")
				.append(filters.stream()
					.map(f-> "WHEN " + f.sql(table, pc) + " THEN " + formatString(f.tag(table)))
					.collect(joining(" "))) //optimize SQL 
				.appendIf(!isBlank(defaultTag), ()-> " ELSE " + formatString(defaultTag))
				.append(" END").toString());
	}

	@Override
	public String tag(DBTable table) {
		return mappedName;
	}
	
	@Override
	public boolean isExpression() {
		return true;
	}
	
	@Override
	public String toString() {
		return sql(mockTable(), staticSql());
	}

	public static CaseColumn betweenIntervals(@NonNull DBColumn column, @NonNull Number... serie) {
		
		return betweenIntervals(column, null, serie);
	}
	
	public static CaseColumn betweenIntervals(@NonNull DBColumn column, String mappedName, @NonNull Number... serie) {
		sort(requireNonEmpty(serie)); //must be sorted
		var filters = new ArrayList<DBFilter>(serie.length+1);
		filters.add(column.lessThanExpression("lt_"+serie[0], serie[0]));
		for(var i=0; i<serie.length-1; i++) {
			filters.add(and("bt_"+serie[i]+"_"+serie[i+1],
					column.greaterOrEqualExpression(null, serie[i]),
					column.lessThanExpression(null, serie[i+1]))
			);
		}
		filters.add(column.greaterOrEqualExpression("gt_"+serie[0], serie[serie.length-1]));
		return new CaseColumn(toMappedName(mappedName, column), unmodifiableCollection(filters), null);
	}

	@SafeVarargs
	public static <T> CaseColumn inValues(@NonNull DBColumn column, Entry<String, T[]>... values) { //map should contains defaultValue
		return inValues(column, null, values);
	}
	
	@SafeVarargs
	public static <T> CaseColumn inValues(@NonNull DBColumn column, String mappedName, Entry<String, T[]>... values) { //map should contains defaultValue
		requireNonEmpty(values);
		var filters = Stream.of(values)
			.filter(e-> e.getValue().length > 0)
			.map(e-> e.getValue().length == 1
						? column.equalExpression(e.getKey(), e.getValue()[0])
						: column.inExpression(e.getKey(), e.getValue()))
			.collect(toList());
		var defaultValue = Stream.of(values)
				.filter(e-> isEmpty(e.getValue()))
				.findAny().map(Entry::getKey)
				.orElse(null);
		return new CaseColumn(toMappedName(mappedName, column), unmodifiableCollection(filters), defaultValue);
	}
	
	private static String toMappedName(String mappedName, DBColumn column) {
		
		return isBlank(mappedName) ? "case_" + column.getMappedName() : mappedName;
	}
	
	@SafeVarargs
	public static <T> Entry<String, T[]> tagWhen(String tag, T... values) {
		return entry(tag, values);
	}
}
