package org.usf.jquery.web;

import static java.lang.reflect.Array.newInstance;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.BadArgumentException.badArgumentCountException;
import static org.usf.jquery.core.Comparator.lookupComparator;
import static org.usf.jquery.core.Operator.lookupOperator;
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
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBObject;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.JqueryType;
import org.usf.jquery.core.LogicalOperator;
import org.usf.jquery.core.OperationColumn;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.ParameterSet;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.TypedComparator;
import org.usf.jquery.core.TypedOperator;
import org.usf.jquery.core.Utils;
import org.usf.jquery.core.WindowFunction;

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
	
	public TaggableColumn evalColumn(TableDecorator td) {
		var r = chainResourceOperations(td, false);
		if(r.entry.isLast()) {
			if(nonNull(r.entry.tag)) { //TD required if operation
				return r.col.as(r.entry.tag);
			}
			return r.col instanceof TaggableColumn 
					? (TaggableColumn) r.col 
					: r.col.as(r.cd.identity());
		}
		throw cannotEvaluateException("operation", r.entry.next);
	}
	
	public DBOrder evalOrder(TableDecorator td) {
		var r = chainResourceOperations(td, false);
		if(r.entry.isLast()) { // no order
			return r.col.order();
		}
		var e = r.entry.next;
		if(e.isLast() && e.value.matches("asc|desc")) { // next must be last
			var o = Order.valueOf(e.requireNoArgs().value.toUpperCase()); // noArgs on valid order
			return r.col.order(o);
		}
		throw cannotEvaluateException("order", e);
	}

	public DBFilter evalFilter(TableDecorator td) {
		return evalFilter(td, null);
	}

	public DBFilter evalFilter(TableDecorator td, List<RequestEntryChain> values) {
		var r = chainResourceOperations(td, true);
		if(r.entry.isLast()) { // no comparator
			return defaultComparatorEntry(values)
					.toComparison(td, r.col)
					.orElseThrow(); //cannot be empty
		}
		var e = r.entry.next;
		if(!isEmpty(values)) {
			if(!e.isLast()) {
				throw new RequestSyntaxException(e + "=" + Utils.toString(values.toArray()));
			}
			if(isNull(e.args)) {
				e.setArgs(values);
			}
		}
		return e.chainComparator(td, r.cd, r.col);
	}
	
	public Object[] evalFunction(TableDecorator td, String fnName, ParameterSet ps) {
		if(fnName.equals(value)) {
			return requireNoNext().toArgs(td, null, ps, Object[]::new);
		}
		throw cannotEvaluateException(fnName, this);
	}
	
	public Object[] evalArrayFunction(TableDecorator td, String fnName, JqueryType type) {
		if(fnName.equals(value)) {
			var c = type.typeClass();
			if(!c.isArray()) { //basic type
				return toArgs(td, null, ofParameters(required(type), varargs(type)), s-> (Object[]) newInstance(c, s));
			}
			throw new UnsupportedOperationException();
		}
		throw cannotEvaluateException(fnName, this);
	}
	
	ResourceCursor chainResourceOperations(TableDecorator td, boolean filter) {
		var r = lookupResource(td);
		var e = r.entry.next;
		while(nonNull(e)) {
			var c = e.toOperation(td, r.col, fn-> true);
			if(c.isEmpty()) {
				break;
			} // preserve last non null column
			r.entry = e;
			r.col = filter && "over".equals(e.value) 
					? windowColumn(r.td.table(), c.get().as(r.cd.identity())) 
					: c.get(); 
			e = e.next;
		}
		return r;
	}

	Optional<OperationColumn> toOperation(TableDecorator td, DBColumn col, Predicate<TypedOperator> pre) {
		return lookupOperator(value).filter(pre).map(fn-> {
			var c = col;
			if(isNull(c) && isEmpty(args) && "count".equals(value)) { // id is MAJ
				c = b-> {
					b.view(td.table()); // important! register view
					return "*";
				};
			}
			return fillArgs(td, c, fn);
		});
	}

	static RequestEntryChain defaultComparatorEntry(List<RequestEntryChain> values) {
		String cmp;
		if(isEmpty(values)) {
			cmp = "isNull";
		}
		else {
			cmp = values.size() > 1 ? "in" : "eq";
		}
		var e = new RequestEntryChain(cmp);
		e.setArgs(values);
		return e;
	}

	DBFilter chainComparator(TableDecorator td, ColumnDecorator cd, DBColumn col){
		var f = toComparison(td, col).orElse(null); //eval comparator first => avoid overriding
		if(isNull(f) && col instanceof TaggableColumn) { //no operation
			var c = cd.criteria(value); 
			if(nonNull(c)) {
				f = col.filter(c.build(toStringArray(args)));
			}
		}
		if(isNull(f)) {
			throw cannotEvaluateException("comparison|criteria", this);
		}
		var e = this;
		while(e.next()) {
			e = e.next;
			if(e.value.matches("and|or")) {
				var op = LogicalOperator.valueOf(e.value.toUpperCase());
				if(!isEmpty(e.args) && e.args.size() == 1) {
					f = f.append(op, e.args.get(0).evalFilter(td));
				}
				else {
					throw badArgumentCountException(1, isEmpty(e.args) ? 0 : e.args.size());				
				}
			}
			else {
				throw cannotEvaluateException("logical operator", e);
			}
		}
		return f;
	}
	
	
	Optional<DBFilter> toComparison(TableDecorator td, DBObject col) {
		return lookupComparator(value).map(c-> fillArgs(td, col, c));
	}
	
	private ResourceCursor lookupResource(TableDecorator td) {
		if(next() && context().isDeclaredTable(value)) {  //sometimes td.id == cd.id
			var rc = next.lookupViewResource(context().getTable(value), 
					fn-> fn.unwrap().getClass() == WindowFunction.class); // only window function
			if(nonNull(rc)) {
				return rc;
			}
		}
		var rc = lookupViewResource(td, fn-> true); // all operations
		if(nonNull(rc)) {
			return rc;
		}
		throw cannotEvaluateException("resource", this);
	}

	private ResourceCursor lookupViewResource(TableDecorator td, Predicate<TypedOperator> pre) {
		return context().isDeclaredColumn(value) 
				? new ResourceCursor(td, context().getColumn(requireNoArgs().value), this)
				: toOperation(td, null, pre)
				.map(op-> new ResourceCursor(td, ofColumn(value, b-> op), this))
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
			if(i>=inc) { //arg0 already parsed
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
	
	RequestEntryChain requireNoNext(){
		if(isLast()) {
			return this;
		}
		throw new IllegalArgumentException(value + " must be the last entry");
	}
	
	public boolean isLast() {
		return isNull(next);
	}

	public boolean next() {
		return nonNull(next);
	}
	
	@Override
	public String toString() {
		var s = ""; // null == EMPTY
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

	static EvalException cannotEvaluateException(String type, RequestEntryChain entry) {
		return EvalException.cannotEvaluateException(type, entry.toString());
	}
	
	@RequiredArgsConstructor
	static final class ResourceCursor {
		
		private final TableDecorator td;
		private final ColumnDecorator cd;
		private RequestEntryChain entry;
		private DBColumn col;
		
		public ResourceCursor(TableDecorator td, ColumnDecorator cd, RequestEntryChain entry) {
			this.td = td;
			this.cd = cd;
			this.entry = entry;
			this.col = td.column(cd);
		}
		
		@Override
		public String toString() {
			return td + "." + cd + " => " + entry.toString();
		}
	}
}