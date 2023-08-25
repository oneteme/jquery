package org.usf.jquery.web;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
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
		return cd.identity();
	}

	@Override
	public String reference() {
		return isNull(tag) ? cd.reference() : tag; //join function !?
	}
	
	@Override
	public SQLType dataType() {
		var i = fns.size();
		while(--i>=0 && fns.get(i).getReturnedType().isAutoType());
		return i<0 ? cd.dataType() : fns.get(i).getReturnedType();
	}
	
	@Override
	public ColumnBuilder builder() {
		return t-> fns.stream()
				.reduce((DBColumn)t.column(cd), (c, fn)-> fn.args(c), (c1,c2)-> c1); //combiner -> sequentially collect
	}

	@Override
	public DBComparator comparator(String comparator, int nArg) {
		return fns.isEmpty()  //cannot apply column comparator on function
				? cd.comparator(comparator, nArg) 
				: ColumnDecorator.super.comparator(comparator, nArg);
	}
	
	@Override
	public CriteriaBuilder<String> criteria(String name) {
		return fns.isEmpty()  //cannot apply column criteria on function
				? cd.criteria(name) 
				: ColumnDecorator.super.criteria(name);
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
			if(type.isAutoType()) { // logical column type can be set in table
				type = td.columnType(cd).orElse(type);
			} //else : overridden
	    	var pars = requireNonNull(parser(type));
	    	if(values.length == 1) {
	    		return cmp.expression(pars.parse(values[0]));
	    	}
			return cmp instanceof InCompartor 
					? cmp.expression(pars.parseAll(values))
					: ofComparator(cmp).build(pars.parseAll(values));
		}
		throw cannotEvaluateException("expression", exp);
	}

	Stream<ComparisonExpression> expression(List<RequestColumn> columns) {
		var cmp = ColumnDecorator.super.comparator(exp, 1);
		if(cmp instanceof BasicComparator) {
			return columns.stream().map(RequestColumn::dbColumn).map(cmp::expression);
		} 
		throw new IllegalArgumentException("illegal column comparator " + exp);
	}
	
	static RequestColumn decodeSingleColumn(String value, TableDecorator defaultTable, boolean allowedExp) {
		return parseSingleLinkedEntry(value).toRequestColumn(defaultTable, allowedExp);
	}
	
	static Stream<RequestColumn> decodeColumns(String value, TableDecorator defaultTable, boolean allowedExp) {
		return parseLinkedEntries(value).stream()
				.map(e-> e.toRequestColumn(defaultTable, allowedExp));
	}
}