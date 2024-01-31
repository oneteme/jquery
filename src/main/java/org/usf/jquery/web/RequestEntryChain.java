package org.usf.jquery.web;

import static java.lang.reflect.Array.newInstance;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Comparator.in;
import static org.usf.jquery.core.Comparator.lookupComparator;
import static org.usf.jquery.core.Operator.lookupOperator;
import static org.usf.jquery.core.Operator.lookupWindowFunction;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;
import static org.usf.jquery.core.ParameterSet.ofParameters;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.WindowView.windowColumn;
import static org.usf.jquery.web.ArgumentParsers.parse;
import static org.usf.jquery.web.ColumnDecorator.ofColumn;
import static org.usf.jquery.web.JQueryContext.context;

import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBObject;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.JqueryType;
import org.usf.jquery.core.OperationColumn;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.ParameterSet;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.TypedComparator;
import org.usf.jquery.core.TypedOperator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author u$f
 *
 */
@Getter
@Setter(value = AccessLevel.PACKAGE)
@RequiredArgsConstructor
final class RequestEntryChain {
	
	private final String value;
	private final boolean text; //"string"
	private RequestEntryChain next;
	private List<RequestEntryChain> args;
	private String tag;

	public RequestEntryChain(String value) {
		this(value, false);
	}
	
	public Object[] evalFunction(TableDecorator td, String fnName, ParameterSet ps) {
		if(fnName.equals(value)) {
			return toArgs(td, null, ps, Object[]::new);
		}
		throw new IllegalArgumentException();
	}
	
	public Object[] evalArray(TableDecorator td, String fnName, JqueryType type) {
		if(fnName.equals(value)) {
			var c = type.typeClass();
			if(c.isArray()) {
				throw new UnsupportedOperationException();
			}
			return toArgs(td, null, ofParameters(required(type), varargs(type)), s-> (Object[]) newInstance(c, s));
		}
		throw new IllegalArgumentException();
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

	public DBFilter asFilter(TableDecorator td) {
		return asFilter(td, null);
	}

	public DBFilter asFilter(TableDecorator td, List<RequestEntryChain> values) {
		var t = lookupResource(td);
		var c = t.buildColumn();
		var e = t.entry.next;
		DBColumn oc = c;
		while(nonNull(e)) {
			var op = e.toOperation(td, oc);
			if(isNull(op)) {
				break;
			}
			oc = "over".equals(e.value)
					? windowColumn(td.table(), op.as(c.tagname())) 
					: op; //preserve last non null column
			e = e.next;
		}
		if(isNull(e)) {
			return isEmpty(values) 
					? oc.isNull() 
					: defaultFilter(t.td, oc, requireNonNull(t.cd.parser(t.td)), values);
		}
		if(!isEmpty(values) && (e.next() || !isEmpty(e.args))) {
			throw new IllegalArgumentException("illegal");
		}
		if(isNull(e.args) && !isEmpty(values)) {
			e.args = values;
		}
		var ftr = e.toComparison(td, oc);
		if(nonNull(ftr)) {
			if(e.next()) {
				//values isEmpty
				e = e.next;
				while(nonNull(e)) {
					ftr = e.toComparison(td, ftr);
					e = e.next;
				}
				return ftr; //throw if null
			}
			if(isNull(e.args)) {
		    	var prs = requireNonNull(t.cd.parser(td));
		    	var arr = isNull(values) ? null : prs.parseAll(toStringArray(values));
				return oc.filter(in().expression(arr));
			}
			//values isEmpty
			return ftr;
		}
		else if(oc == c) {
			return c.filter(e.toComparison(td, t.cd, values));
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	static DBFilter defaultFilter(TableDecorator td, DBColumn oc, JDBCArgumentParser parser, List<RequestEntryChain> values) {
		if(values.size() > 1) {
			return oc.in(parser.parseAll(toStringArray(values)));
		}
		Object o;
		try {
			o = values.get(0).asColumn(td);
		}
		catch(Exception e) {
			o = parser.parse(values.get(0).toString());
		}
		return oc.equal(o);
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
	
	DBFilter toComparison(TableDecorator td, DBObject col) {
		var res = lookupComparator(value);
		return res.isEmpty() ? null : fillArgs(td, col, res.get());
	}
	
	ComparisonExpression toComparison(TableDecorator td, ColumnDecorator cd, List<RequestEntryChain> values) {
		if(next()) {
			throw cannotEvaluateException("expression", this);
		}
		if(nonNull(value)) {
			var criteria = cd.criteria(value);
			if(nonNull(criteria)) {
				return criteria.build(toStringArray(values));
			}
		}
//		var cmp = cd.comparator(value, values.size());
//		if(nonNull(cmp)) {
//	    	if(values.size() == 1) {
//	    		try {
//		    		return cmp.expression(values.get(0).asColumn(td)); // try parse column
//	    		}
//	    		catch (Exception e) {
//	    	    	var prs = requireNonNull(cd.parser(td));
//		    		return cmp.expression(prs.parse(values.get(0).toString()));
//				}
//	    	}
//	    	var prs = requireNonNull(cd.parser(td));
//	    	var arr = prs.parseAll(toStringArray(values));
//			return cmp.isVarargs() 
//					? cmp.expression(arr)
//					: ofComparator(cmp).build(arr);
//		}
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

	private DBFilter fillArgs(TableDecorator td, DBObject col, TypedComparator cmp) {
		try {
			return cmp.args(toArgs(td, col, cmp.getParameterSet(), Object[]::new));
		}
		catch(Exception e) {
			for(var ps : cmp.getOverloads()) {
				try {
					return cmp.args(toArgs(td, col, ps, Object[]::new));
				}
				catch(Exception e1) {
					System.out.println(e1);
				}
			}
			throw e;
		}
	}
	
	private OperationColumn fillArgs(TableDecorator td, DBColumn col, TypedOperator opr) {
		return opr.args(toArgs(td, col, opr.getParameterSet(), Object[]::new));
	}
	
	private Object[] toArgs(TableDecorator td, DBObject col, ParameterSet ps, IntFunction<Object[]> arrFn) {
		int inc = nonNull(col) ? 1 : 0;
		var arr = arrFn.apply(isNull(args) ? inc : args.size() + inc);
		if(nonNull(col)) {
			arr[0] = col;
		}
		ps.forEach(arr.length, (p,i)-> {
			if(i>0 || inc==0) { //arg0 already parsed
				var e = args.get(i-inc);
				arr[i] = isNull(e.value) || e.text
						? e.requireNoArgs().value
						: parse(e, td, p.types(arr));
			}
		});
		return arr;
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
	
	static String[] toStringArray(List<RequestEntryChain> entries) {
		return entries.stream()
				.map(e-> isNull(e.value) ? null : e.toString())
				.toArray(String[]::new);
	}

	static ParseException cannotEvaluateException(String type, RequestEntryChain entry) {
		return ParseException.cannotEvaluateException(type, entry.toString());
	}
	
	@RequiredArgsConstructor
	static class Triple {
		
		private final TableDecorator td;
		private final ColumnDecorator cd;
		private final RequestEntryChain entry;
		
		TaggableColumn buildColumn() {
			return td.column(cd);
		}
		
		@Override
		public String toString() {
			return td + "." + cd + " => " + entry.toString();
		}
	}
}