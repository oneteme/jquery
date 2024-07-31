package org.usf.jquery.web;

import static java.lang.reflect.Array.newInstance;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.BadArgumentException.badArgumentCountException;
import static org.usf.jquery.core.BadArgumentException.badArgumentsException;
import static org.usf.jquery.core.Comparator.lookupComparator;
import static org.usf.jquery.core.DBColumn.allColumns;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.Operator.lookupOperator;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;
import static org.usf.jquery.core.ParameterSet.ofParameters;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.ViewJoin.join;
import static org.usf.jquery.web.ArgumentParsers.parse;
import static org.usf.jquery.web.ColumnDecorator.ofColumn;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.DISTINCT;
import static org.usf.jquery.web.Constants.FETCH;
import static org.usf.jquery.web.Constants.FILTER;
import static org.usf.jquery.web.Constants.JOIN;
import static org.usf.jquery.web.Constants.OFFSET;
import static org.usf.jquery.web.Constants.ORDER;
import static org.usf.jquery.web.Constants.PARTITION;
import static org.usf.jquery.web.Constants.SELECT;
import static org.usf.jquery.web.Constants.TAG;
import static org.usf.jquery.web.Constants.VIEW;
import static org.usf.jquery.web.ContextManager.currentContext;
import static org.usf.jquery.web.EntryParseException.cannotParseEntryException;
import static org.usf.jquery.web.EntryParseException.entryTackesNoArgException;
import static org.usf.jquery.web.EntryParseException.requireEntryException;
import static org.usf.jquery.web.EntryParseException.unexpectedEntryException;
import static org.usf.jquery.web.RequestContext.currentContext_;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import org.usf.jquery.core.BadArgumentException;
import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBObject;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.DBQuery;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.JQueryType;
import org.usf.jquery.core.JavaType;
import org.usf.jquery.core.LogicalOperator;
import org.usf.jquery.core.OperationColumn;
import org.usf.jquery.core.Operator;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.ParameterSet;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.QueryView;
import org.usf.jquery.core.RequestQueryBuilder;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.TypedOperator;
import org.usf.jquery.core.Utils;
import org.usf.jquery.core.ViewColumn;
import org.usf.jquery.core.ViewJoin;
import org.usf.jquery.core.ViewJoin.JoinType;
import org.usf.jquery.core.WindowFunction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
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

	public ViewDecorator evalQuery(ViewDecorator td) {
		return evalQuery(td, false);
	}
	

	public ViewJoin evalJoin(ViewDecorator td) {
		if(value.matches(JoinType.pattern())) {
			var jt = JoinType.valueOf(value);
			var args = toArgs( td, null, null);
			return join(jt, (DBView)args[0], (DBFilter[])args[0]);
		}
		throw cannotParseEntryException(JOIN, this.toString()); //TD
	}
	
	
	//evalView query|view:alias
	
	public ViewDecorator evalQuery(ViewDecorator td, boolean requireTag) { //sub context
		if(SELECT.equals(value)) {
			var q = new RequestQueryBuilder().columns(toColumnArgs(td, false));
			var e =	this;
			while(e.hasNext()) { //preserve last entry
				e = e.next;
				switch(e.value) {//column not allowed 
				case DISTINCT: e.requireNoArgs(DISTINCT); q.distinct(); break;
				case FILTER: q.filters(e.toFilterArgs(td)); break;
				case ORDER: q.orders(e.toOderArgs(td)); break; //not sure
				case OFFSET: q.offset((int)e.toOneArg(td, INTEGER)); break;
				case FETCH: q.fetch((int)e.toOneArg(td, INTEGER)); break;
				default: throw unexpectedEntryException(e.toString());
				}
			}
			if(requireTag && isNull(e.tag)) {
				throw requireEntryException(TAG);
			}
			return new QueryDecorator(q.asView(e.tag));
		}
		throw cannotParseEntryException(SELECT, toString());
	}
	
	public Partition evalPartition(ViewDecorator td) {
		if(PARTITION.equals(value)) {
			var p = new Partition(toColumnArgs(td, true));
			if(hasNext()) { //TD loop
				var e =	next;
				if(ORDER.equals(e.value)) {//column not allowed
					p.orders(e.toOderArgs(td)); //not sure
				}
				else {
					throw unexpectedEntryException(e.toString());
				}
			}//require no tag
			return p;
		}
		throw cannotParseEntryException(PARTITION, this.toString());
	}
	
	public TaggableColumn evalColumn(ViewDecorator td) {
		var r = chainColumnOperations(td, false); //throws ParseException
		if(r.entry.isLast()) {
			if(nonNull(r.entry.tag)) { //TD: required tag if operation
				return r.col.as(r.entry.tag);
			}
			if(r.col instanceof TaggableColumn col) {
				return col;
			}
			throw requireEntryException(TAG);
		}
		throw unexpectedEntryException(r.entry.next.toString());
	}
	
	public DBOrder evalOrder(ViewDecorator td) {
		var r = chainColumnOperations(td, false);  //throws ParseException
		if(r.entry.isLast()) { // no order
			return r.col.order();
		}
		var e = r.entry.next;
		if(e.isLast()) { // next must be last
			if(e.value.matches("asc|desc")) {
				var o = Order.valueOf(e.requireNoArgs(ORDER).value.toUpperCase()); // order takes no args
				return r.col.order(o);
			}
			cannotParseEntryException(ORDER, e.toString());
		}
		throw unexpectedEntryException(e.toString());
	}

	public DBFilter evalFilter(ViewDecorator td) {
		return evalFilter(td, null);
	}

	public DBFilter evalFilter(ViewDecorator td, List<RequestEntryChain> values) {
		try {
			var r = chainColumnOperations(td, true);
			var e = r.entry;
			if(e.isLast()) { // no comparator, no criteria
				String cmp;
				if(isEmpty(values)) {
					cmp = "isNull";
				}
				else {
					cmp = values.size() > 1 ? "in" : "eq"; //query ??
				}
				e = e.copy();
				e.setNext(new RequestEntryChain(cmp)); // do not set args;
			}
			return e.next.updateArgs(values)
					.chainComparator(td, r.cd, r.col);
		}
		catch(EntryParseException e) {
			return tableCriteria(td, values)
					.orElseThrow(()-> cannotParseEntryException(FILTER, this.toString(), e));
		}
	}
	
	Optional<DBFilter> tableCriteria(ViewDecorator td, List<RequestEntryChain> values) {
		RequestEntryChain e = null;
		CriteriaBuilder<DBFilter> c = null;
		var res = currentContext_().lookupViewDecorator(value);
		if(res.isPresent() && hasNext()) {
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
		var e = this;
		if(!isEmpty(values)) {
			if(isLast() && isNull(args)) {
				e = copy();
				e.setArgs(values);
			}
			else {
				throw new UnexpectedEntryException(this + "=" + Utils.toString(values.toArray()));
			}
		}
		return e;
	}
	
	DBFilter chainComparator(ViewDecorator td, ColumnDecorator cd, DBColumn col){
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
		throw cannotParseEntryException("comparison|criteria", this.toString());
	}

	DBFilter chainComparator(ViewDecorator td, DBFilter f){
		var e = next;
		while(nonNull(e)) {
			if(e.value.matches("and|or")) {
				var op = LogicalOperator.valueOf(e.value.toUpperCase());
				f = f.append(op, (DBFilter) e.toOneArg(td, JQueryType.FILTER));
			}
			else {
				throw unexpectedEntryException(e.toString(), "and|or");
			}
			e = e.next;
		}
		return f;
	}
	
	private ResourceCursor chainColumnOperations(ViewDecorator td, boolean filter) {
		var r = lookupResource(td).orElseThrow(()-> cannotParseEntryException(COLUMN, this.toString()));
		var e = r.entry.next;
		while(nonNull(e)) { // chain until !operator
			var o = e.lookupOperation(td, r.col, fn-> true);
			if(o.isEmpty()) {
				break;
			} 
			r.entry = e;
			r.col = filter && "over".equals(e.value)
					? windowColumn(r.td, o.get().as(r.cd.identity())) //require tag | random tag
					: o.get(); 
			e = e.next;
		}
		return r;
	}
	
	private static DBColumn windowColumn(ViewDecorator vd, TaggableColumn col) {
		var v = vd.view();
		currentContext().overView(v, ()-> new RequestQueryBuilder()
				.columns(allColumns(v))
				.asView(vd.identity())).getBuilder().columns(col); //append over column
		return new ViewColumn(v, doubleQuote(col.tagname()), null, col.getType());
	}
	
	private Optional<ResourceCursor> lookupResource(ViewDecorator td) { //do not change priority
		if(hasNext()) { //check td.cd first
			var rc = currentContext().lookupRegistredView(value)
					.flatMap(v-> next.lookupViewResource(v, RequestEntryChain::isWindowFunction));
			if(rc.isPresent()) {
				requireNoArgs(VIEW); //view takes no args
				return rc;
			}
		}
		return currentContext().lookupDeclaredColumn(value)  //declared column
				.map(c-> new ResourceCursor(null, null, requireNoArgs(COLUMN), c)) 
				.or(()-> lookupViewResource(td, fn-> true)); //registered column
	}
	
	private Optional<ResourceCursor> lookupViewResource(ViewDecorator td, Predicate<TypedOperator> pre) {
		if(td instanceof QueryDecorator qd) { //query column
			var res = ofNullable(qd.column(value)).map(c-> new ResourceCursor(td, null, requireNoArgs(COLUMN), c));
			if(res.isPresent()) {
				return res;
			}
		}
		return currentContext().lookupRegistredColumn(value)
				.map(cd-> new ResourceCursor(td, cd, requireNoArgs(COLUMN), td.column(cd)))
				.or(()-> lookupOperation(td, null, pre).map(col-> new ResourceCursor(td, null, this, col)));
	}
	
	private Optional<OperationColumn> lookupOperation(ViewDecorator td, DBColumn col, Predicate<TypedOperator> opr) {
		return lookupOperator(value).filter(opr).map(fn-> {
			var c = col;
			if(isNull(c) && isEmpty(args) && "COUNT".equals(fn.id())) {
				c = b-> {
					b.view(td.view()); // declare view
					return "*"; 
				};
			}
			return fn.args(toArgs(td, c, fn.getParameterSet()));
		});
	}

	private TaggableColumn[] toColumnArgs(ViewDecorator td, boolean allowEmpty) {
		return (TaggableColumn[]) toArgs(td, JQueryType.COLUMN, allowEmpty);
	}
	
	private DBFilter[] toFilterArgs(ViewDecorator td) {
		return (DBFilter[]) toArgs(td, JQueryType.FILTER, false);
	}
	
	private DBOrder[] toOderArgs(ViewDecorator td) {
		return (DBOrder[]) toArgs(td, JQueryType.ORDER, false);
	}

	private Object[] toArgs(ViewDecorator td, JavaType type, boolean allowEmpty) {
		var c = type.typeClass();
		if(DBObject.class.isAssignableFrom(c)) { // JQuery types & !array
			var ps = allowEmpty 
					? ofParameters(varargs(type)) 
					: ofParameters(required(type), varargs(type));
			return toArgs(td, null, ps, s-> (Object[]) newInstance(c, s));
		}
		throw new UnsupportedOperationException("cannot instanitate type " + c);
	}
	
	private Object toOneArg(ViewDecorator td, JavaType type) {
		return toArgs(td, null, ofParameters(required(type)))[0];
	}
	
	private Object[] toArgs(ViewDecorator td, DBObject col, ParameterSet ps) {
		return toArgs(td, col, ps, Object[]::new);
	}
	
	private Object[] toArgs(ViewDecorator td, DBObject col, ParameterSet ps, IntFunction<Object[]> arrFn) {
		int inc = isNull(col) ? 0 : 1;
		var arr = arrFn.apply(isNull(args) ? inc : args.size() + inc);
		if(nonNull(col)) {
			arr[0] = col;
		}
		try {
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
		catch (EntryParseException | BadArgumentException e) {
			throw badArgumentsException(ps.toString(), this.toString(), e);
		}
	}

	RequestEntryChain requireNoArgs(String name) {
		if(isNull(args)) {
			return this;
		}
		throw entryTackesNoArgException(name, toString());
	}

	RequestEntryChain requireNoArgs() {
		if(isNull(args)) {
			return this;
		}
		throw badArgumentCountException(0, args.size()); //TODO 
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

	public boolean hasNext() {
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
	
	protected RequestEntryChain copy() {
		return new RequestEntryChain(value, text, next, args, tag);
	}
	
	private static boolean isWindowFunction(TypedOperator op) {
		return op.unwrap() instanceof WindowFunction;  // WindowFunction + COUNT
	}
	
	private static String[] toStringArray(List<RequestEntryChain> entries) {
		return entries.stream()
				.map(e-> isNull(e.value) ? null : e.toString())
				.toArray(String[]::new);
	}

	@AllArgsConstructor
	static final class ResourceCursor {
		
		private final ViewDecorator td; //optional
		private ColumnDecorator cd; //optional
		private RequestEntryChain entry;
		private DBColumn col;
		//chained !?

		@Override
		public String toString() {
			return td + "." + cd + " => " + entry.toString();
		}
	}
	
}