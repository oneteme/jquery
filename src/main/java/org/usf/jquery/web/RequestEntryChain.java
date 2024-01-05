package org.usf.jquery.web;

import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Operator.lookupOperator;
import static org.usf.jquery.core.Operator.lookupWindowFunction;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Utils.isEmpty;
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

	private final String value;
	private final boolean text; //"string"
	private RequestEntryChain next;
	private List<RequestEntryChain> args;
	private String tag;

	public RequestEntryChain(String value) {
		this(value, false);
	}

	public TaggableColumn asColumn(TableDecorator td) {
		var t = lookupResource(td);
		var c = t.buildColumn();
		var e = t.entry;
		DBColumn oc = c;
		if(e.next()) {
			do {
				e = e.next;
				oc = e.toOperation(td, oc);
				if(isNull(oc)) {
					throw cannotEvaluateException("column", e);
				}
			} while(e.next());
		}
		if(nonNull(e.tag)) {
			return oc.as(e.tag);
		}
		return oc == c ? c : oc.as(c.tagname());
	}

	public DBFilter asFilter(TableDecorator td, List<RequestEntryChain> values) {
		var t = lookupResource(td);
		var c = t.buildColumn();
		var e = t.entry.next;
		var p = t.entry;
		DBColumn oc = c;
		while(nonNull(e)) {
			var op = e.toOperation(td, oc);
			if(isNull(op)) {
				break;
			}
			oc = op; //preserve last non null column
			p = e; //preserve last operator entry
			e = e.next;
		}
		var exp = (isNull(e) ? new RequestEntryChain(null) : e)
				.toComparison(td, c == oc ? t.cd : DEFAUL_COLUMN, values);
		return "over".equals(p.value) 
				? new WindowView(td.table(), oc.as(c.tagname())).filter(exp) 
				: oc.filter(exp);
	}
	
	public DBOrder asOrder(TableDecorator td) {
		var t = lookupResource(td);
		var c = t.buildColumn();
		var e = t.entry.next;
		DBColumn oc = c;
		while(nonNull(e)) {
			var op = e.toOperation(td, c);
			if(isNull(op)) {
				break;
			}
			oc = op; //preserve last non null column
			e = e.next;
		}
		if(isNull(e)) { // no expression
			return oc.order();
		}
		else if(e.isLast()) { //last entry
			var upVal = e.value.toUpperCase();
			var order = Stream.of(Order.values()).filter(o-> o.name().equals(upVal)).findAny();
			if(order.isPresent()) {
				return oc.order(order.get());
			}
		}
		throw cannotEvaluateException("order", e); //column expected
	}
	
	public OperationColumn asOperation(TableDecorator td) { //predicate
		var op = toOperation(td, null);
		if(nonNull(op)) {
			return op;
		}
		throw cannotEvaluateException("operation", this); // as function
	}

	OperationColumn toOperation(TableDecorator td, DBColumn col) {
		var res = lookupOperator(value);
		if(isNull(col) && "count".equals(value) && isEmpty(args)) {
			col = b-> { //no column & no args
				b.view(td.table());
				return "*";
			};
		}
		return res.isEmpty() ? null : fillArgs(td, col, res.get());
	}
	
	ComparisonExpression toComparison(TableDecorator td, ColumnDecorator cd, List<RequestEntryChain> values) {
		if(next()) {
			throw cannotEvaluateException("expression", next); // as function
		}
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
		throw cannotEvaluateException("expression", this);
	}
	
	private Triple lookupResource(TableDecorator td) {
		if(next() && context().isDeclaredTable(value)) {  //columnName == viewName
			var tr = next.lookupViewResource(context().getTable(value));
			if(nonNull(tr)) {
				return tr;
			}
		}
		var tp = lookupViewResource(td);
		if(nonNull(tp)) {
			return tp;
		}
		var op = toOperation(td, null);
		if(nonNull(op)) {
			return new Triple(td, ofColumn(value, b-> op), this);
		}
		throw cannotEvaluateException("resource", this);
	}

	private Triple lookupViewResource(TableDecorator td) {
		return context().isDeclaredColumn(value) 
				? new Triple(td, context().getColumn(requireNoArgs().value), this)
				: lookupWindowFunction(value)  //rank, rowNumber, ..
				.map(fn-> new Triple(td, ofColumn(value, b-> fillArgs(td, null, fn)), this))
				.orElse(null);
	}
	
	private OperationColumn fillArgs(TableDecorator td, DBColumn col, TypedOperator op) {
		var np = isNull(args) ? 0 : args.size();
		if(nonNull(col)) {
			np++;
		}		
		var min = op.requireArgCount();
		var max = op.getParameters().length;
		if(np >= min && (op.isVarags() || np <= max)) {
			var params = new ArrayList<Object>(np);
			if(nonNull(col)) {
				params.add(col);
			}
			var s = nonNull(col) ? 1 : 0;
			var i = s; 
			for(; i<min(np, max); i++) {
				params.add(args.get(i-s).toArg(td, op.getParameters()[i]));
			}
			if(op.isVarags()) {
				var types = op.getParameters()[max-1]; 
				for(; i<np; i++) {
					params.add(args.get(i-s).toArg(td, types));
				}
			}
			return op.args(params.toArray());
		}
		throw new IllegalArgumentException("msg");
	}

	private Object toArg(TableDecorator td, Parameter parameter) {
		return isNull(value) || text
				? requireNoArgs().value
				: parse(this, td, parameter.getTypes());
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

	static ParseException cannotEvaluateException(String type, RequestEntryChain entry) {
		return ParseException.cannotEvaluateException(type, entry.toString());
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