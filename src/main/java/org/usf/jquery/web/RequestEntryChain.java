package org.usf.jquery.web;

import static java.lang.reflect.Array.newInstance;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.BadArgumentException.badArgumentCountException;
import static org.usf.jquery.core.Comparator.lookupComparator;
import static org.usf.jquery.core.DBColumn.allColumns;
import static org.usf.jquery.core.Operator.lookupOperator;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;
import static org.usf.jquery.core.ParameterSet.ofParameters;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.ArgumentParsers.parse;
import static org.usf.jquery.web.ColumnDecorator.ofColumn;
import static org.usf.jquery.web.JQueryContext.context;
import static org.usf.jquery.web.RequestContext.requestContext;

import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBObject;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.JQueryType;
import org.usf.jquery.core.LogicalOperator;
import org.usf.jquery.core.OperationColumn;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.ParameterSet;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.TypedComparator;
import org.usf.jquery.core.TypedOperator;
import org.usf.jquery.core.Utils;
import org.usf.jquery.core.ViewColumn;
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
		var r = chainResourceOperations(td, false)
				.orElseThrow(()-> cannotEvaluateException("column", this));
		if(r.entry.isLast()) {
			if(nonNull(r.entry.tag)) { //TD: required tag if operation
				return r.col.as(r.entry.tag);
			}
			return r.col instanceof TaggableColumn 
					? (TaggableColumn) r.col 
					: r.col.as(r.cd.identity());
		}
		throw cannotEvaluateException("operation", r.entry.next);
	}
	
	public DBOrder evalOrder(TableDecorator td) {
		var r = chainResourceOperations(td, false)
				.orElseThrow(()-> cannotEvaluateException("order", this));
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
		var o = chainResourceOperations(td, true);
		if(o.isPresent()) {
			var r = o.get();
			if(r.entry.isLast()) { // no comparator
				r.entry.setNext(defaultComparatorEntry(values));
			}
			r.entry.next.updateArgs(values);
			return r.entry.next.chainComparator(td, r.cd, r.col);
		}
		if(next() && context().isDeclaredTable(value)) {
			var c = context().getTable(value).criteria(next.value);
			if(nonNull(c)) {
				next.updateArgs(values);
				return next.chainComparator(td, c.build(toStringArray(next.args)));
			}
		}
		var c = td.criteria(value);
		if(nonNull(c)) {
			updateArgs(values);
			return chainComparator(td, c.build(toStringArray(args)));
		}
		return null;
	}
	
	//column.eq=v1
	private void updateArgs(List<RequestEntryChain> values) {
		if(!isEmpty(values)) {
			if(isLast() && isNull(args)) {
				setArgs(values);
			}
			else {
				throw new RequestSyntaxException(this + "=" + Utils.toString(values.toArray()));
			}
		}
	}
	
	public Object[] evalFunction(TableDecorator td, String fnName, ParameterSet ps) {
		if(fnName.equals(value)) {
			return requireNoNext().toArgs(td, null, ps, Object[]::new);
		}
		throw cannotEvaluateException(fnName, this);
	}
	
	public Object[] evalArrayFunction(TableDecorator td, String fnName, JQueryType type) {
		if(fnName.equals(value)) {
			var c = type.typeClass();
			if(!c.isArray()) { //basic type
				return toArgs(td, null, ofParameters(required(type), varargs(type)), s-> (Object[]) newInstance(c, s));
			}
			throw new UnsupportedOperationException();
		}
		throw cannotEvaluateException(fnName, this);
	}

	static RequestEntryChain defaultComparatorEntry(List<RequestEntryChain> values) {
		String cmp;
		if(isEmpty(values)) {
			cmp = "isNull";
		}
		else {
			cmp = values.size() > 1 ? "in" : "eq";
		}
		return new RequestEntryChain(cmp); // do not set args
	}

	DBFilter chainComparator(TableDecorator td, ColumnDecorator cd, DBColumn col){
		var f = lookupComparator(value).map(c-> fillArgs(td, col, c)).orElse(null); //eval comparator first => avoid overriding
		if(isNull(f) && col instanceof TaggableColumn) { //no operation
			var c = cd.criteria(value); //criteria lookup
			if(nonNull(c)) {
				f = col.filter(c.build(toStringArray(args)));
			}
		}
		if(nonNull(f)) {
			return chainComparator(td, f);
		}
		throw cannotEvaluateException("comparison|criteria", this);
	}

	DBFilter chainComparator(TableDecorator td, DBFilter f){
		var e = next;
		while(nonNull(e)) {
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
			e = e.next;
		}
		return f;
	}
	
	private Optional<ResourceCursor> chainResourceOperations(TableDecorator td, boolean filter) {
		return lookupResource(td).map(r-> {
			var e = r.entry.next;
			while(nonNull(e)) { // chain until !operator
				var c = e.toOperation(td, r.col, fn-> true);
				if(c.isEmpty()) {
					break;
				} 
				r.entry = e;
				r.col = filter && "over".equals(e.value) 
						? windowColumn(r.td, c.get().as(r.cd.identity())) 
						: c.get(); 
				e = e.next;
			}
			return r;
		});
	}
	
	private static DBColumn windowColumn(TableDecorator td, TaggableColumn column) {
		var v = td.table();
		var vw = requestContext().getView(v); // TD use tag ?
		if(vw instanceof CompletableViewQuery) {  // already create
			((CompletableViewQuery)vw).columns(column);
		}
		else {
			vw = new CompletableViewQuery((isNull(vw) ? v : vw).window(td.identity(), column));
			requestContext().setViews(vw); // same name
		}
		return new ViewColumn(v, doubleQuote(column.tagname()), null, column.getType());
	}
	
	private Optional<ResourceCursor> lookupResource(TableDecorator td) {
		if(next() && context().isDeclaredTable(value)) {  //sometimes td.id == cd.id
			var rc = next.lookupViewResource(context().getTable(value), RequestEntryChain::isWindowFunction);
			if(rc.isPresent()) {
				requireNoArgs(); // noArgs on valid resource
				return rc;
			}
		}
		return lookupViewResource(td, fn-> true); // all operations
	}
	
	private Optional<ResourceCursor> lookupViewResource(TableDecorator td, Predicate<TypedOperator> pre) {
		return context().isDeclaredColumn(value) 
				? Optional.of(new ResourceCursor(td, context().getColumn(requireNoArgs().value), this))
				: toOperation(td, null, pre).map(op-> new ResourceCursor(td, ofColumn(value, b-> op), this));
	}

	private Optional<OperationColumn> toOperation(TableDecorator td, DBColumn col, Predicate<TypedOperator> pre) {
		return lookupOperator(value).filter(pre).map(fn-> {
			var c = col;
			if(isNull(c) && isEmpty(args) && "count".equals(value)) { // id is MAJ
				c = allColumns(td.table());
			}
			return fillArgs(td, c, fn);
		});
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
	
	private static boolean isWindowFunction(TypedOperator op) {
		return op.unwrap().getClass() == WindowFunction.class;  // !instanceOf : only window function
	}
	
	private static String[] toStringArray(List<RequestEntryChain> entries) {
		return entries.stream()
				.map(e-> isNull(e.value) ? null : e.toString())
				.toArray(String[]::new);
	}

	private static EvalException cannotEvaluateException(String type, RequestEntryChain entry) {
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