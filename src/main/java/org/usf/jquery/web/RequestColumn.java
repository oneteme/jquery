package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.usf.jquery.core.JDBCType.AUTO_TYPE;
import static org.usf.jquery.web.Constants.ORDER;
import static org.usf.jquery.web.CriteriaBuilder.ofComparator;
import static org.usf.jquery.web.LinkedRequestEntry.parseLinkedEntries;
import static org.usf.jquery.web.LinkedRequestEntry.parseSingleLinkedEntry;
import static org.usf.jquery.web.ParseException.cannotEvaluateException;

import java.util.List;
import java.util.stream.Stream;

import org.usf.jquery.core.BasicComparator;
import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBComparator;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.InCompartor;
import org.usf.jquery.core.SQLType;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.TypedFunction;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * <code>[table.]column[.function]*[.comparator|order][:alias]</code>
 * 
 * @author u$f
 * 
 * @see RequestFilter
 * 
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class RequestColumn implements ColumnDecorator {
	
	private final TableDecorator td;
	private final ColumnDecorator cd;
	private final List<TypedFunction> fns;
	private final String exp;
	private final String tag;

	public TableDecorator tableDecorator() {
		return td;
	}
	
	@Deprecated
	public DBOrder dbOrder(){
		if(isNull(exp)) {
			return dbColumn().order();
		}
		if("desc".equals(exp) || "asc".equals(exp)) {
			return dbColumn().order(exp.toUpperCase());
		}
		throw cannotEvaluateException(ORDER, exp);
	}
	
	public TaggableColumn dbColumn(){
		return tableDecorator().column(this);
	}
	
	@Override
	public String identity() {
		return null; //unused
	}

	@Override
	public String reference() {
		return ofNullable(tag).orElseGet(cd::reference); //join function !?
	}
	
	@Override
	public SQLType dataType() {
		var i = fns.size();
		while(--i>=0 && fns.get(i).getReturnedType().isAutoType());
		return i<0 ? cd.dataType() : fns.get(i).getReturnedType();
	}
	
	@Override
	public ColumnBuilder builder() {
		if(fns.isEmpty()) {
			ColumnDecorator.super.builder();
		}
		return t-> { //cannot apply column comparator on function
			DBColumn col = t.column(cd);
			return fns.stream() //TD check types
					.reduce(col, (c, fn)-> fn.args(c), (c1,c2)-> c1) //combiner -> sequentially collect
					.as(reference());
			};
	}

	@Override
	public DBComparator comparator(String comparator, int nArg) {
		return fns.isEmpty() 
				? cd.comparator(comparator, nArg) 
				: ColumnDecorator.super.comparator(comparator, nArg); //cannot apply column comparator on function
	}
	
	@Override
	public CriteriaBuilder<String> criteria(String name) {
		return fns.isEmpty() 
				? cd.criteria(name) 
				: ColumnDecorator.super.criteria(name); //cannot apply column criteria on function
	}

	Stream<ComparisonExpression> expression(List<RequestColumn> columns) {
		var cmp = ColumnDecorator.super.comparator(exp, 1);
		if(cmp instanceof BasicComparator) {
			return columns.stream().map(RequestColumn::dbColumn).map(cmp::expression);
		}
		throw new IllegalArgumentException("illegal column comparator " + exp);
	}
	
	//expression => criteria | comparator
	ComparisonExpression expression(String... values) {
		var criteria = criteria(exp);
		if(nonNull(criteria)) {
			return criteria.build(values);
		}
		var cmp = comparator(exp, values.length);
		if(nonNull(cmp)) {
			var type = dataType();
			if(type == AUTO_TYPE) { // logical column type can be set in table
				type = td.columnType(this).orElse(type);
			} //else : overridden
	    	var prs = requireNonNull(parser(type));
	    	if(values.length == 1) {
	    		return cmp.expression(prs.parse(values[0]));
	    	}
			return cmp instanceof InCompartor 
					? cmp.expression(prs.parseAll(values))
					: ofComparator(cmp).build(prs.parseAll(values));
		}
		throw cannotEvaluateException("expression", exp);
	}
	
	static RequestColumn decodeSingleColumn(String value, TableDecorator defaultTable, boolean allowedExp) {
		return parseSingleLinkedEntry(value).toRequestColumn(defaultTable, allowedExp);
	}
	
	static Stream<RequestColumn> decodeColumns(String value, TableDecorator defaultTable, boolean allowedExp) {
		return parseLinkedEntries(value).stream()
				.map(e-> e.toRequestColumn(defaultTable, allowedExp));
	}
}