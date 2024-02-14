package org.usf.jquery.web;

import static java.lang.reflect.Array.newInstance;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Comparator.lookupComparator;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.Operator.lookupOperator;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;
import static org.usf.jquery.core.ParameterSet.ofParameters;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.ArgumentParsers.parse;
import static org.usf.jquery.web.ColumnDecorator.ofColumn;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.DISTINCT;
import static org.usf.jquery.web.Constants.FETCH;
import static org.usf.jquery.web.Constants.FILTER;
import static org.usf.jquery.web.Constants.OFFSET;
import static org.usf.jquery.web.Constants.ORDER;
import static org.usf.jquery.web.Constants.PARTITION;
import static org.usf.jquery.web.Constants.SELECT;
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
import org.usf.jquery.core.JavaType;
import org.usf.jquery.core.LogicalOperator;
import org.usf.jquery.core.OperationColumn;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.ParameterSet;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.RequestQueryBuilder;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.TypedOperator;
import org.usf.jquery.core.Utils;
import org.usf.jquery.core.ViewColumn;
import org.usf.jquery.core.ViewQuery;
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
	
	public ViewQuery evalQuery(TableDecorator td) {
		if(SELECT.equals(value)) {
			var q = new RequestQueryBuilder().columns(toColumnArgs(td, false));
			var e =	this;
			while(e.next()) { //preserve last entry
				e = e.next;
				switch(e.value) {//column not permitted 
				case DISTINCT: e.requireNoArgs(); q.distinct(); break;
				case FILTER: q.filters(e.toFilterArgs(td)); break;
				case ORDER: q.orders(e.toOderArgs(td)); break; //not sure
				case OFFSET: q.offset(e.toIntArg(td)); break;
				case FETCH: q.fetch(e.toIntArg(td)); break;
				default: throw unexpectedEntryException(e);
				}
			}
			return q.as(e.tag); //TD require tag to register query
		}
		throw cannotEvaluateException(SELECT, this);
	}
	
	public Partition evalPartition(TableDecorator td) {
		if(PARTITION.equals(value)) {
			var p = new Partition(toColumnArgs(td, true));
			if(next()) {
				var e =	next;
				if(ORDER.equals(e.value)) {//column not permitted
					p.orders(e.toOderArgs(td)); //not sure
				}
				else {
					throw unexpectedEntryException(e);
				}
			}
			return p;
		}
		throw cannotEvaluateException(PARTITION, this);
	}
	
	public TaggableColumn evalColumn(TableDecorator td) {
		var r = chainResourceOperations(td, false)
				.orElseThrow(()-> cannotEvaluateException(COLUMN, this));
		if(r.entry.isLast()) {
			if(nonNull(r.entry.tag)) { //TD: required tag if operation
				return r.col.as(r.entry.tag);
			}
			return r.col instanceof TaggableColumn 
					? (TaggableColumn) r.col 
					: r.col.as(r.cd.identity());
		}
		throw unexpectedEntryException(r.entry.next);
	}
	
	public DBOrder evalOrder(TableDecorator td) {
		var r = chainResourceOperations(td, false)
				.orElseThrow(()-> cannotEvaluateException(ORDER, this));
		if(r.entry.isLast()) { // no order
			return r.col.order();
		}
		var e = r.entry.next;
		if(e.isLast() && e.value.matches("asc|desc")) { // next must be last
			var o = Order.valueOf(e.requireNoArgs().value.toUpperCase()); // noArgs on valid order
			return r.col.order(o);
		}
		throw unexpectedEntryException(e);
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
				throw new UnexpectedEntryException(this + "=" + Utils.toString(values.toArray()));
			}
		}
	}
	
	static RequestEntryChain defaultComparatorEntry(List<RequestEntryChain> values) {
		String cmp;
		if(isEmpty(values)) {
			cmp = "isNull";
		}
		else {
			cmp = values.size() > 1 ? "in" : "eq"; //query ??
		}
		return new RequestEntryChain(cmp); // do not set args
	}

	DBFilter chainComparator(TableDecorator td, ColumnDecorator cd, DBColumn col){
		var f = lookupComparator(value).map(c-> c.args(toArgs(td, col, c.getParameterSet()))).orElse(null); //eval comparator first => avoid overriding
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
				f = f.append(op, (DBFilter) e.toArgs(td, null, 
						ofParameters(required(JQueryType.FILTER)), DBFilter[]::new)[0]);
			}
			else {
				throw unexpectedEntryException(e);
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
		var vw = requestContext().getView(v.id()); // TD use tag ?
		if(vw instanceof CompletableViewQuery) {  // already create
			((CompletableViewQuery)vw).getQuery().columns(column);
		}
		else {
			vw = new CompletableViewQuery((isNull(vw) ? v : vw).window(td.identity(), column));
			requestContext().putView(vw); // same name
		}
		return new ViewColumn(v, doubleQuote(column.tagname()), null, column.getType());
	}
	
	private Optional<ResourceCursor> lookupResource(TableDecorator td) {
		if(next() && requestContext().isDeclaredView(value)) {  //sometimes td.id == cd.id
			var rc = next.lookupViewResource(requestContext().getViewDecorator(value), RequestEntryChain::isWindowFunction);
			if(rc.isPresent()) {
				requireNoArgs(); // noArgs on valid resource
				return rc;
			}
		}
		return lookupViewResource(td, fn-> true); // all operations
	}
	
	private Optional<ResourceCursor> lookupViewResource(TableDecorator td, Predicate<TypedOperator> pre) {
		return requestContext().isDeclaredColumn(value) 
				? Optional.of(new ResourceCursor(td, context().getColumn(requireNoArgs().value), this))
				: toOperation(td, null, pre).map(op-> new ResourceCursor(td, ofColumn(value, b-> op), this));
	}

	private Optional<OperationColumn> toOperation(TableDecorator td, DBColumn col, Predicate<TypedOperator> pre) {
		return lookupOperator(value).filter(pre).map(fn-> {
			var c = col;
			if(isNull(c) && isEmpty(args) && "count".equals(value)) { // id is MAJ
				c = b-> {
					b.view(td.table()); // important! register view
					return "*"; 
				};
			}
			return fn.args(toArgs(td, c, fn.getParameterSet()));
		});
	}

	private TaggableColumn[] toColumnArgs(TableDecorator td, boolean allowEmpty) {
		return (TaggableColumn[]) toArgs(td, null, JQueryType.COLUMN, allowEmpty);
	}
	
	private DBOrder[] toOderArgs(TableDecorator td) {
		return (DBOrder[]) toArgs(td, null, JQueryType.ORDER, false);
	}

	private DBFilter[] toFilterArgs(TableDecorator td) {
		return (DBFilter[]) toArgs(td, null, JQueryType.FILTER, false);
	}

	private Object[] toArgs(TableDecorator td, DBObject col, JavaType type, boolean allowEmpty) {
		var c = type.typeClass();
		if(c.isArray()) {
			throw new UnsupportedOperationException("arrayOf " + c);
		}
		var ps = allowEmpty 
				? ofParameters(varargs(type)) 
				: ofParameters(required(type), varargs(type));
		return toArgs(td, col, ps, s-> (Object[]) newInstance(c, s));
	}
	
	private int toIntArg(TableDecorator td) {
		return (Integer) toArgs(td, null, ofParameters(required(INTEGER)))[0];
	}
	
	private Object[] toArgs(TableDecorator td, DBObject col, ParameterSet ps) {
		return toArgs(td, col, ps, Object[]::new);
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
		throw new UnexpectedEntryException(value + " takes no args : " + this);
	}
	
	RequestEntryChain requireNoNext(){
		if(isLast()) {
			return this;
		}
		throw new UnexpectedEntryException(value + " must be the last entry : " + this);
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
	public static UnexpectedEntryException unexpectedEntryException(RequestEntryChain entry) {
		return UnexpectedEntryException.unexpectedEntryException(entry.toString());
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