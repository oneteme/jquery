package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ColumnFilterGroup.and;
import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;
import static fr.enedis.teme.jquery.ParameterHolder.formatString;
import static fr.enedis.teme.jquery.Taggable.prefix;
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
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CaseColumn implements DBColumn {

	private final Collection<NamedFilter> filters;
	private final String defaultTag;
	private final Supplier<String> tagFn;
	
	@Override
	public String sql(DBTable table, ParameterHolder pc) {
		return pc.staticMode(()-> new SqlStringBuilder(filters.size() * 50) //force static values
				.append("CASE ")
				.append(filters.stream()
					.map(f-> f.sql(table, pc))
					.collect(joining(" "))) //optimize SQL 
				.appendIf(!isBlank(defaultTag), ()-> " ELSE " + formatString(defaultTag))
				.append(" END").toString());
	}

	@Override
	public String getTag() {
		return tagFn.get();
	}
	
	@Override
	public boolean isExpression() {
		return true;
	}
	
	@Override
	public boolean isAggregation() {
		return false;
	}
	
	@Override
	public boolean isConstant() {
		return false;
	}
	
	@Override
	public String toString() {
		return sql(mockTable(), addWithValue());
	}

	public static CaseColumn betweenIntervals(@NonNull DBColumn column, @NonNull Number... serie) {
		sort(requireNonEmpty(serie)); //must be sorted
		var filters = new ArrayList<NamedFilter>(serie.length+1);
		filters.add(column.lessThanFilter(serie[0]).as("lt_"+serie[0]));
		for(var i=0; i<serie.length-1; i++) {
			filters.add(and(
					column.greaterOrEqualFilter(serie[i]),
					column.lessThanFilter(serie[i+1]))
					.as("bt_"+serie[i]+"_"+serie[i+1]));
		}
		filters.add(column.greaterOrEqualFilter(serie[serie.length-1]).as("gt_"+serie[serie.length-1]));
		return new CaseColumn(unmodifiableCollection(filters), null, tagFunction(column));
	}

	@SafeVarargs
	public static <T> CaseColumn inValues(@NonNull DBColumn column, @NonNull Entry<String, T[]>... values) { //map should contains defaultValue
		requireNonEmpty(values);
		var filters = Stream.of(values)
			.filter(e-> e.getValue().length > 0)
			.map(e-> e.getValue().length == 1
						? column.equalFilter(e.getValue()[0]).as(e.getKey())
						: column.inFilter(e.getValue()).as(e.getKey()))
			.collect(toList());
		var defaultValue = Stream.of(values)
				.filter(e-> isEmpty(e.getValue()))
				.findAny().map(Entry::getKey)
				.orElse(null);
		return new CaseColumn(unmodifiableCollection(filters), defaultValue, tagFunction(column));
	}
	
	private static Supplier<String> tagFunction(DBColumn column) {
		return ()-> prefix("case", column);
	}
	
	@SafeVarargs
	public static <T> Entry<String, T[]> tagWhen(String tag, T... values) {
		return entry(tag, values);
	}
}
