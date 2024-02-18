package org.usf.jquery.web;

import static java.lang.reflect.Array.newInstance;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.BadArgumentException.badArgumentCountException;
import static org.usf.jquery.core.BadArgumentException.badArgumentTypeException;
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
import static org.usf.jquery.web.ParseException.cannotParseException;
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
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
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
				switch(e.value) {//column not allowed 
				case DISTINCT: e.requireNoArgs(); q.distinct(); break;
				case FILTER: q.filters(e.toFilterArgs(td)); break;
				case ORDER: q.orders(e.toOderArgs(td)); break; //not sure
				case OFFSET: q.offset(e.toIntArg(td)); break;
				case FETCH: q.fetch(e.toIntArg(td)); break;
				default: throw unexpectedEntryException(e, DISTINCT, FILTER, ORDER, OFFSET, FETCH);
				}
			}
			return q.as(e.tag); //TD require tag to register query
		}
		throw cannotParseException(SELECT, this.toString());
	}
	
	public Partition evalPartition(TableDecorator td) {
		if(PARTITION.equals(value)) {
			var p = new Partition(toColumnArgs(td, true));
			if(next()) {
				var e =	next;
				if(ORDER.equals(e.value)) {//column not allowed
					p.orders(e.toOderArgs(td)); //not sure
				}
				else {
					throw unexpectedEntryException(e, ORDER);
				}
			}
			return p;
		}
		throw cannotParseException(PARTITION, this.toString());
	}
	
	public TaggableColumn evalColumn(TableDecorator td) {
		var r = chainResourceOperations(td, false); //throws ParseException
		if(r.entry.isLast()) {
			if(nonNull(r.entry.tag)) { //TD: required tag if operation
				return r.col.as(r.entry.tag);
			}
			if(r.col instanceof TaggableColumn) {
				return (TaggableColumn) r.col ;
			}
			log.warn("tag missing : {}", this);
			return r.col.as(r.cd.identity());
		}
		throw unexpectedEntryException(r.entry.next);
	}
	
	public DBOrder evalOrder(TableDecorator td) {
		var r = chainResourceOperations(td, false);  //throws ParseException
		if(r.entry.isLast()) { // no order
			return r.col.order();
		}
		var e = r.entry.next;
		if(e.isLast() && e.value.matches("asc|desc")) { // next must be last
			var o = Order.valueOf(e.requireNoArgs().value.toUpperCase()); // noArgs on valid order
			return r.col.order(o);
		}
		throw unexpectedEntryException(e, "asc|desc");
	}

	public DBFilter evalFilter(TableDecorator td) {
		return evalFilter(td, null);
	}

	public DBFilter evalFilter(TableDecorator td, List<RequestEntryChain> values) {
		try {
			var r = chainResourceOperations(td, true);
			var e = r.entry;
			if(e.isLast()) { // no comparator
				e.setNext(defaultComparatorEntry(values));
			}
			return e.next.updateArgs(values)
					.chainComparator(td, r.cd, r.col);
		}
		catch(ParseException e) {
			return tableCriteria(td, values)
					.orElseThrow(()-> cannotParseException(FILTER, this.toString(), e));
		}
	}
	
	Optional<DBFilter> tableCriteria(TableDecorator td, List<RequestEntryChain> values) {
		RequestEntryChain e = null;
		CriteriaBuilder<DBFilter> c = null;
		var res = requestContext().lookupViewDecorator(value);
		if(res.isPresent() && next()) {
			c = res.get().criteria(next.value);
			e = next; // only if nonNull
		}
		if(isNull(c)) {
			c = td.criteria(value);
			e = this;
		}
		if(nonNull(c)) {
			e.updateArgs(values);
			var f = e.chainComparator(td, c.build(toStringArray(args)));
			return Optional.of(f);
		}
		return empty();
	}
	
	//column.eq=v1
	private RequestEntryChain updateArgs(List<RequestEntryChain> values) {
		if(!isEmpty(values)) {
			if(isLast() && isNull(args)) {
				setArgs(values);
			}
			else {
				throw new UnexpectedEntryException(this + "=" + Utils.toString(values.toArray()));
			}
		}
		return this;
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
		throw cannotParseException("comparison|criteria", this.toString());
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
				throw unexpectedEntryException(e, "and|or");
			}
			e = e.next;
		}
		return f;
	}
	
	private ResourceCursor chainResourceOperations(TableDecorator td, boolean filter) {
		var r = lookupResource(td).orElseThrow(()-> cannotParseException(COLUMN, this.toString()));
		var e = r.entry.next;
		while(nonNull(e)) { // chain until !operator
			var c = e.toOperation(td, r.col, fn-> true);
			if(c.isEmpty()) {
				break;
			} 
			r.entry = e;
			r.col =  filter && "over".equals(e.value)
					? windowColumn(r.td, c.get().as(r.cd.identity())) 
					: c.get(); 
			e = e.next;
		}
		return r;
	}
	
	private static DBColumn windowColumn(TableDecorator td, TaggableColumn column) {
		var vw = requestContext().getView(td.identity());
		if(vw instanceof CompletableViewQuery) {  // already create
			((CompletableViewQuery)vw).getQuery().columns(column);
		}
		else {
			vw = new CompletableViewQuery((isNull(vw) ? td.table() : vw).window(td.identity(), column));
			requestContext().putView(vw); // same name
		}
		return new ViewColumn(vw, doubleQuote(column.tagname()), null, column.getType());
	}
	
	private Optional<ResourceCursor> lookupResource(TableDecorator td) {
		if(next()) {  //check td.cd first
			var rc = requestContext().lookupViewDecorator(value)
					.flatMap(v-> next.lookupViewResource(v, RequestEntryChain::isWindowFunction));
			if(rc.isPresent()) {
				requireNoArgs(); // noArgs on valid resource
				return rc;
			}
		}
		return lookupViewResource(td, fn-> true); // all operations
	}
	
	private Optional<ResourceCursor> lookupViewResource(TableDecorator td, Predicate<TypedOperator> pre) {
		var res = requestContext().lookupColumnDecorator(value);
		if(res.isPresent()) {
			requireNoArgs();
		}
		else {
			res = toOperation(td, null, pre).map(op-> ofColumn(value, b-> op));
		}
		return res.map(cd-> new ResourceCursor(td, cd, this));
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
		return (TaggableColumn[]) toArgs(td, JQueryType.COLUMN, allowEmpty);
	}
	
	private DBFilter[] toFilterArgs(TableDecorator td) {
		return (DBFilter[]) toArgs(td, JQueryType.FILTER, false);
	}
	
	private DBOrder[] toOderArgs(TableDecorator td) {
		return (DBOrder[]) toArgs(td, JQueryType.ORDER, false);
	}

	private Object[] toArgs(TableDecorator td, JavaType type, boolean allowEmpty) {
		var c = type.typeClass();
		if(DBObject.class.isAssignableFrom(c)) { // JQuery types & !array
			var ps = allowEmpty 
					? ofParameters(varargs(type)) 
					: ofParameters(required(type), varargs(type));
			return toArgs(td, null, ps, s-> (Object[]) newInstance(c, s));
		}
		throw new UnsupportedOperationException("cannot create array of " + c);
	}
	
	private int toIntArg(TableDecorator td) {
		return (Integer) toArgs(td, null, ofParameters(required(INTEGER)))[0];
	}
	
	private Object[] toArgs(TableDecorator td, DBObject col, ParameterSet ps) {
		return toArgs(td, col, ps, Object[]::new);
	}
	
	private Object[] toArgs(TableDecorator td, DBObject col, ParameterSet ps, IntFunction<Object[]> arrFn) {
		int inc = isNull(col) ? 0 : 1;
		var arr = arrFn.apply(isNull(args) ? inc : args.size() + inc);
		if(nonNull(col)) {
			arr[0] = col;
		}
		ps.forEach(arr.length, (p,i)-> {
			if(i>=inc) { //arg0 already parsed
				var e = args.get(i-inc);
				if(isNull(e.value) || e.text) {
					arr[i] = e.requireNoArgs().value; //checked later
				}
				else {
					try {
						arr[i] = parse(e, td, p.types(arr));
					} catch (ParseException ex) { // do not throw parse exception
						throw badArgumentTypeException(p.types(arr), e, ex);
					}
				}
			}
		});
		return arr;
	}

	RequestEntryChain requireNoArgs() {
		if(isNull(args)) {
			return this;
		}
		throw badArgumentCountException(0, args.size());
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

	public static UnexpectedEntryException unexpectedEntryException(RequestEntryChain entry, String... expected) {
		return UnexpectedEntryException.unexpectedEntryException(entry.toString(), expected);
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