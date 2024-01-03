package org.usf.jquery.web;

import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.DBColumn.column;
import static org.usf.jquery.core.Operator.count;
import static org.usf.jquery.core.Operator.lookupOperator;
import static org.usf.jquery.core.Operator.lookupStandaloneFunction;
import static org.usf.jquery.core.Operator.lookupWindowFunction;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.web.ArgumentParsers.parse;
import static org.usf.jquery.web.ColumnDecorator.ofColumn;
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
import org.usf.jquery.core.OperationColumn;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.Parameter;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.TypedOperator;
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
	private static final String OVER_FN = "OVER";

	private final String value;
	private final boolean text; //"string"
	private RequestEntryChain next;
	private List<RequestEntryChain> args;
	private String tag;

	public RequestEntryChain(String value) {
		this(value, false);
	}

	public TaggableColumn asColumn(TableDecorator td) {
		var t = lookup(td);
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
		if(oc == c) {
			return c;
		}
		return oc.as(isNull(e.tag) ? c.tagname() : e.tag);
	}

	public DBFilter asFilter(TableDecorator td, List<RequestEntryChain> values) {
		var t = lookup(td);
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
		var t = lookup(td);
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
	
	OperationColumn asOperation(TableDecorator td) {
		var op = toOperation(td, null);
		if(nonNull(op)) {
			return op;
		}
		throw cannotEvaluateException(value); // as function
	}
	
	OperationColumn toOperation(TableDecorator td, DBColumn col) {
		var res = lookupOperator(value);
		return res.isEmpty() ? null : fillArgs(td, col, res.get());
	}

	OperationColumn fillArgs(TableDecorator td, DBColumn col, TypedOperator op) {
		var min = op.requireArgCount();
		var np = isNull(args) ? 0 : args.size();
		if(nonNull(col)) {
			np++;
		}
		if(np < min || (!op.isVarags() && np > op.getParameters().length)) {
			throw new IllegalArgumentException("msg");
		}
		var params = new ArrayList<Object>(np);
		if(nonNull(col)) {
			params.add(col);
		}
		var s = nonNull(col) ? 1 : 0;
		var i = s; 
		for(; i<min(np, op.getParameters().length); i++) {
			params.add(args.get(i-s).toArg(td, op.getParameters()[i]));
		}
		if(op.isVarags()) {
			var types = op.getParameters()[op.getParameters().length-1]; 
			for(; i<np; i++) {
				params.add(args.get(i-s).toArg(td, types));
			}
		}
		return op.args(params.toArray());
	}
	
	Object toArg(TableDecorator td, Parameter parameter) {
		return isNull(value) || text 
				? requireNoArgs().value
				: parse(this, td, parameter.getTypes());
	}
	
	private Triple lookup(TableDecorator td) {
		if(next() && context().isDeclaredTable(value)) {  //columnName == viewName
			var tr = next.lookupColumn(context().getTable(value));
			if(nonNull(tr)) {
				return tr;
			}
		}
		var tp = lookupColumn(td);
		if(nonNull(tp)) {
			return tp;
		}
		if("count".equals(value)) { //table !?
			return new Triple(td, ofColumn("count", b-> fillArgs(td, column("*"), count())), this);
		}
		var res = lookupStandaloneFunction(value); //rank, rowNumber, ..
		if(res.isPresent()) {
			var fn = res.get();
			return new Triple(td, ofColumn(value, b-> fn.args()), this); //args //??
		}
		throw cannotEvaluateException(this);
	}

	private Triple lookupColumn(TableDecorator td) {
		if(context().isDeclaredColumn(value)) {
			return new Triple(td, context().getColumn(requireNoArgs().value), this);
		}
		var res = lookupWindowFunction(value); //rank, rowNumber, ..
		if(res.isPresent()) {
			var fn = res.get();
			return new Triple(td, ofColumn(value, b-> fn.args()), this); //args //??
		}
		return null;
	}
	
	RequestEntryChain requireNoArgs() {
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
		
		TaggableColumn buildColumn() {
			return td.column(cd);
		}
	}
	
}