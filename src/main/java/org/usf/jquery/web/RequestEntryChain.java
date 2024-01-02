package org.usf.jquery.web;

import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Operator.lookupNoArgFunction;
import static org.usf.jquery.core.Operator.lookupOperator;
import static org.usf.jquery.core.OverClause.clauses;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Validation.VAR_PATTERN;
import static org.usf.jquery.web.ArgumentParsers.javaTypeParser;
import static org.usf.jquery.web.CriteriaBuilder.ofComparator;
import static org.usf.jquery.web.JQueryContext.context;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.InCompartor;
import org.usf.jquery.core.JavaType;
import org.usf.jquery.core.OperationColumn;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.WindowView;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PACKAGE)
@RequiredArgsConstructor
final class RequestEntryChain {
	
	private static final ColumnDecorator DEFAUL_COLUMN = ()-> null; //unused identity
	
	private static final String OVER_FN =  "OVER"; 

	private final String value;
	private final boolean text; //"string"
	private RequestEntryChain next;
	private List<RequestEntryChain> args;
	private String tag;

	public RequestEntryChain(String value) {
		this(value, false);
	}

	public TaggableColumn asColumn(TableDecorator td) {
		var t = lookup(td, true);
		var c = t.buildColumn();
		var e = t.entry;
		DBColumn oc = c;
		if(e.next()) {
			do {
				e = e.next;
				oc = e.toOperation(td, oc);
				if(isNull(oc)) {
					throw cannotEvaluateException(e);
				}
			} while(e.next()); //preserve last non null entry
		}
		return oc.as(isNull(e.tag) ? c.tagname() : e.tag);
	}

	public DBFilter asFilter(TableDecorator td, List<RequestEntryChain> values) {
		var t = lookup(td, false);
		var c = t.buildColumn();
		var e = t.entry.next;
		DBColumn oc = c;
		while(nonNull(e)) {
			var op = e.toOperation(td, oc);
			if(isNull(oc)) {
				break;
			}
			else {
				oc = op; //preserve last non null column
			}
			e = e.next;
		}
		var cd = c == oc ? t.cd : DEFAUL_COLUMN; //no operation
		ComparisonExpression exp = null;
		if(isNull(e)) { // no expression
			exp = new RequestEntryChain(null).toComparison(td, cd, values);
		}
		else if(e.isLast()) {
			exp = e.toComparison(td, cd, values);
		}
		else {
			throw cannotEvaluateException(e); //more detail
		}
		return OVER_FN.equals(e.value) 
				? new WindowView(td.table(), oc.as(c.tagname())).filter(exp) 
				: oc.filter(exp);
	}
	
	public DBOrder asOrder(TableDecorator td) {
		var t = lookup(td, false);
		var c = t.buildColumn();
		var e = t.entry.next;
		DBColumn oc = c;
		while(nonNull(e)) {
			var op = e.toOperation(td, c);
			if(isNull(op)) {
				break;
			}
			else {
				oc = op; //preserve last non null column
			}
			e = e.next;
		}
		if(isNull(e)) { // no expression
			return c.order();
		}
		else if(e.isLast()) { //last entry
			var upVal = e.value.toUpperCase();
			var order = Stream.of(Order.values()).filter(o-> o.name().equals(upVal)).findAny();
			if(order.isPresent()) {
				return oc.order(order.get());
			}
		}
		throw cannotEvaluateException(e); //column expected
	}
	
	ComparisonExpression toComparison(TableDecorator td, ColumnDecorator cd, List<RequestEntryChain> values) {
		if(nonNull(value)) {
			var criteria = cd.criteria(value);
			if(nonNull(criteria)) {
				return criteria.build(toStringArray(values));
			}
		}
		var cmp = cd.comparator(value, values.size());
		if(nonNull(cmp)) {
	    	if(values.size() == 1) {
	    		try {
		    		return cmp.expression(values.get(0).asColumn(td)); // try parse column
	    		}
	    		catch (Exception e) {
	    	    	var prs = requireNonNull(cd.parser(td));
		    		return cmp.expression(prs.parse(values.get(0).toString()));
				}
	    	}
	    	var prs = requireNonNull(cd.parser(td));
	    	var arr = prs.parseAll(toStringArray(values));
			return cmp instanceof InCompartor 
					? cmp.expression(arr)
					: ofComparator(cmp).build(arr);
		}
		throw cannotEvaluateException(value);
	}
	
	OperationColumn requireOperation(TableDecorator td, DBColumn col) {
		var op = toOperation(td, col);
		if(nonNull(op)) {
			return op;
		}
		throw cannotEvaluateException(value); // as function
	}
	
	OperationColumn toOperation(TableDecorator td, DBColumn col) {
		var res = lookupOperator(value);
		if(res.isEmpty()) {
			return null;
		}
		var op = res.get();
		if("OVER".equals(res.get().id())) {
			var oc = args.stream().map(o-> o.requireOperation(td, col)).toArray(OperationColumn[]::new);
			return op.args(clauses(oc));
		}
		var min = op.requireArgCount();
		var np = args.size()+1; // col
		if(np < min || (!op.isVarags() && np > op.getParameters().length)) {
			throw new IllegalArgumentException("msg");
		}
		var params = new ArrayList<Object>(np);
		params.add(col);
		var i=1;
		for(; i<min; i++) {
			params.add(args.get(i-1).toArg(td, op.getParameters()[i].getTypes()));
		}
		for(; i<min(np, op.getParameters().length); i++) {
			params.add(args.get(i-1).toArg(td, op.getParameters()[i].getTypes()));
		}
		if(op.isVarags()) {
			var types = op.getParameters()[op.getParameters().length-1].getTypes(); 
			for(; i<np; i++) {
				params.add(args.get(i-1).toArg(td, types));
			}
		}
		return op.args(params.toArray());
	}
	
	Object toArg(TableDecorator td, JavaType... types) {
		if(isNull(value) || text) {
			return requireNoArgs().value;
		}
		if(value.matches(VAR_PATTERN)) {
			var c = lookup(td, true);
			if(nonNull(c)) {
				return c; // type will be checked later
			}
		}
		requireNoArgs();
		for(var t : types) {
			var o = javaTypeParser(t).tryParse(value);
			if(nonNull(o)) {
				return o;
			}
		}
		throw new ParseException("cannot parse value : " + value);
	}
	
	private Triple lookup(TableDecorator td, boolean noArgFn) { //columnName == viewName
		if(next() && context().isDeclaredTable(value) && context().isDeclaredColumn(next.value)) {
			return new Triple(
					context().getTable(requireNoArgs().value), 
					context().getColumn(next.requireNoArgs().value), 
					next, null);
		}
		if(context().isDeclaredColumn(value)) {
			return new Triple(td, context().getColumn(requireNoArgs().value), this, null);
		}
		if(noArgFn) {
			var res = lookupNoArgFunction(value); //window function 
			if(res.isPresent()) {
				var op = res.get(); 
				if(isNull(args) || args.isEmpty()) {
					return new Triple(null, null, this, op.args().as(op.id()));
				}
				throw new IllegalArgumentException(op.id() + " takes no arguments");
			}
		}
		throw cannotEvaluateException(this);
	}
	

	private Object lookupColumn(TableDecorator td, boolean noArgFn) {

		if(context().isDeclaredColumn(value)) {
			
		}
	}
	
	private RequestEntryChain requireNoArgs() {
		if(isNull(args)) {
			return this;
		}
		throw new IllegalArgumentException(value + " takes no args");
	}
	
	public boolean isLast() {
		return isNull(next);
	}

	public boolean next() {
		return nonNull(next);
	}
	
	@Override
	public String toString() {
		var s = "";
		if(nonNull(value)) {
			s += text ? doubleQuote(value) : value;
		}
		if(nonNull(args)){
			s += args.stream().map(RequestEntryChain::toString).collect(joining(",", "(", ")"));
		}
		if(nonNull(next)) {
			s += "." + next.toString();
		}
		return isNull(tag) ? s : s + ":" + tag;
	}

	static ParseException cannotEvaluateException(RequestEntryChain entry) {
		return cannotEvaluateException(entry.toString());
	}
	
	static ParseException cannotEvaluateException(String entry) {
		return new ParseException("cannot evaluate entry : " + quote(entry));
	}
	
	static String[] toStringArray(List<RequestEntryChain> entries) {
		return entries.stream().map(RequestEntryChain::toString).toArray(String[]::new);
	}
	
	@RequiredArgsConstructor
	static class Triple {
		
		private final TableDecorator td;
		private final ColumnDecorator cd;
		private final RequestEntryChain entry;
		private final TaggableColumn column;
		
		TaggableColumn buildColumn() {
			return isNull(column) ? td.column(cd) : column;
		}
	}
	
}