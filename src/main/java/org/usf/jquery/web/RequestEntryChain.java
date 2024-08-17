package org.usf.jquery.web;

import static java.lang.reflect.Array.newInstance;
import static java.util.Collections.addAll;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.BadArgumentException.badArgumentsException;
import static org.usf.jquery.core.Comparator.eq;
import static org.usf.jquery.core.Comparator.in;
import static org.usf.jquery.core.Comparator.lookupComparator;
import static org.usf.jquery.core.DBColumn.allColumns;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.Operator.lookupOperator;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;
import static org.usf.jquery.core.ParameterSet.ofParameters;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.ArgumentParsers.parse;
import static org.usf.jquery.web.Constants.COLUMN;
import static org.usf.jquery.web.Constants.DISTINCT;
import static org.usf.jquery.web.Constants.FETCH;
import static org.usf.jquery.web.Constants.FILTER;
import static org.usf.jquery.web.Constants.JOIN;
import static org.usf.jquery.web.Constants.OFFSET;
import static org.usf.jquery.web.Constants.ORDER;
import static org.usf.jquery.web.Constants.PARTITION;
import static org.usf.jquery.web.Constants.QUERY;
import static org.usf.jquery.web.Constants.SELECT;
import static org.usf.jquery.web.Constants.VIEW;
import static org.usf.jquery.web.ContextManager.currentContext;
import static org.usf.jquery.web.EntryParseException.cannotParseEntryException;
import static org.usf.jquery.web.EntrySyntaxException.expectedEntryTagException;
import static org.usf.jquery.web.EntrySyntaxException.unexpectedEntryArgsException;
import static org.usf.jquery.web.EntrySyntaxException.unexpectedEntryException;
import static org.usf.jquery.web.EntrySyntaxException.unexpectedEntryValueException;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import org.usf.jquery.core.BadArgumentException;
import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBObject;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.JQueryType;
import org.usf.jquery.core.JavaType;
import org.usf.jquery.core.LogicalOperator;
import org.usf.jquery.core.OperationColumn;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.Parameter;
import org.usf.jquery.core.ParameterSet;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.QueryColumn;
import org.usf.jquery.core.RequestQueryBuilder;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.TypedOperator;
import org.usf.jquery.core.ViewColumn;
import org.usf.jquery.core.ViewJoin;
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
	
	// [view|query]:tag
	public ViewDecorator evalView(ViewDecorator vd) { //TODO level isolation
		try {
			return currentContext().lookupRegisteredView(value) //check args & next only if view exists
					.<ViewDecorator>map(v-> new ViewDecoratorWrapper(v, requireNoArgs().requireNoNext().requireTag()))
					.or(()-> evalQuery(vd, true))
					.orElseThrow(()-> noSuchResourceException(VIEW, value));
		}
		catch (Exception e) {
			throw cannotParseEntryException(VIEW, this, e);
		}
	}
	
	public QueryColumn evalQueryColumn(ViewDecorator td) {
		return null;
	}
	
	public ViewDecorator evalQuery(ViewDecorator td) {
		try {
			return evalQuery(td, false)
					.orElseThrow(()-> unexpectedEntryValueException(this));
		}
		catch (Exception e) {
			throw cannotParseEntryException(QUERY, this, e);
		}
	}
	
	//select[.distinct|filter|order|offset|fetch]*
	Optional<ViewDecorator> evalQuery(ViewDecorator td, boolean requireTag) { //sub context
		if(SELECT.equals(value)) {
			var q = new RequestQueryBuilder().columns(toTaggableArgs(td));
			var e =	this;
			while(e.hasNext()) {
				e = e.next;
				switch(e.value) {//column not allowed 
				case DISTINCT: e.requireNoArgs(); q.distinct(); break;
				case FILTER: q.filters(e.toFilterArgs(td)); break;
				case ORDER: q.orders(e.toOderArgs(td)); break; //not sure
				case JOIN: q.joins(e.evalJoin(td)); break;
				case OFFSET: q.offset((int)e.toOneArg(td, INTEGER)); break;
				case FETCH: q.fetch((int)e.toOneArg(td, INTEGER)); break;
				default: throw unexpectedEntryValueException(e);
				}
			}
			return Optional.of(new QueryDecorator(requireTag ? e.requireTag() : e.tag, q.asView()));
		}
		return empty();
	}

	//[view.]joiner
	public ViewJoin[] evalJoin(ViewDecorator vd) { 
		try {
			var e = this;
			if(hasNext()) { 
				vd = currentContext().lookupRegisteredView(value)
						.orElseThrow(()-> noSuchResourceException(VIEW, value));
				e = requireNoArgs().next; //check args only if view exists
			}
			var join = vd.joiner(e.value);
			if(nonNull(join)) {
				e.requireNoArgs().requireNoNext(); //check args & next only if joiner exists
				return requireNonNull(join.build(), "view.joiner: " + e);
			}
			throw noSuchResourceException(vd.identity() + ".joiner", e.value);
		}
		catch (Exception e) {
			throw cannotParseEntryException(JOIN, this, e);
		}
	}
	
	//[partition.order]*
	public Partition evalPartition(ViewDecorator td) {
		List<DBColumn> cols = new ArrayList<>();
		List<DBOrder> ords = new ArrayList<>();
		try {
			var e = this;
			do {
				switch (e.value) {
				case PARTITION: addAll(cols, toColumnArgs(td)); break;
				case ORDER: addAll(ords, e.toOderArgs(td)); break;
				default: throw unexpectedEntryValueException(e);
				}
				e = e.next;
			} while(nonNull(e));
			return new Partition(
					cols.toArray(DBColumn[]::new), 
					ords.toArray(DBOrder[]::new));
		}
		catch (Exception e) {
			throw cannotParseEntryException(PARTITION, this, e);
		}
	}
	
	//[view.]column[.operator]*
	public DBColumn evalColumn(ViewDecorator td, boolean requireTag, boolean declare) {
		try {
			var r = chainColumnOperations(td, false)
					.orElseThrow(()-> noSuchViewColumnException(this));
			r.entry.requireNoNext(); //check next only if column exists
			if(nonNull(r.entry.tag)) {
				var c = r.col.as(r.entry.tag);
				return declare ? currentContext().declareColumn(c) : c;
			}
			if(!requireTag || r.col instanceof TaggableColumn) {
				return r.col;
			}
			throw expectedEntryTagException(r.entry);
		} catch (Exception e) {
			throw cannotParseEntryException(COLUMN, this, e);
		}
	}
	
	//[view.]column[.operator]*[.order]
	public DBOrder evalOrder(ViewDecorator td) {
		try {
			var r = chainColumnOperations(td, false)
					.orElseThrow(()-> noSuchViewColumnException(this));
			if(r.entry.isLast()) { // default order
				return r.col.order();
			}
			var ord = r.entry.next;
			if(ord.value.matches("asc|desc")) { //check args & next only if order exists
				var s = ord.requireNoArgs().requireNoNext().value.toUpperCase();
				return r.col.order(Order.valueOf(s));
			}
			throw unexpectedEntryValueException(ord);
		} catch (Exception e) {
			throw cannotParseEntryException(ORDER, this, e);
		}
	}

	public DBFilter evalFilter(ViewDecorator td) {
		return evalFilter(td, emptyList());
	}

	//[view.]criteria | [view.]column.criteria |  [view.]column[.operator]*[.comparator]
	public DBFilter evalFilter(ViewDecorator vd, List<RequestEntryChain> values) { //supply values
		try {
			var res = chainColumnOperations(vd, true);
			if(res.isEmpty()) { //not a column
				return viewCriteria(vd, values)
						.orElseThrow(()-> noSuchViewColumnException(this)); 
			}
			var rc = res.get();
			if(rc.entry.isLast()) { //no comparator, no criteria
				var fn = requireNonNull(values).size() == 1 ? eq() : in(); //non empty
				var e = new RequestEntryChain(null, false, null, values, null); 
				return fn.filter(e.toArgs(vd, rc.col, fn.getParameterSet())); //no chain
			}
			return rc.entry.next.columnCriteria(vd, rc.cd, rc.col, values);
		}
		catch(Exception e) {
			throw cannotParseEntryException(FILTER, this, e);
		}
	}

	//[view.]criteria
	Optional<DBFilter> viewCriteria(ViewDecorator td, List<RequestEntryChain> values) {
		CriteriaBuilder<DBFilter> b = null;
		RequestEntryChain e = null;
		if(hasNext()) { //view.id == column.id
			var res = currentContext().lookupRegisteredView(value).map(v-> v.criteria(next.value));
			if(res.isPresent()){
				b = res.get();
				e = next;
			}
		}
		if(isNull(b)) {
			b = td.criteria(value);
			e = this;
		}
		if(nonNull(b)) {
			var strArgs = toStringArray(e.assertOuterParameters(values));
			var f = requireNonNull(b.build(strArgs), "view.criteria: " + e);
			return Optional.of(e.chainComparator(td, f));
		}
		return Optional.empty();
	}
	
	DBFilter columnCriteria(ViewDecorator vc, ColumnDecorator cd, DBColumn col, List<RequestEntryChain> values) {
		var cmp = lookupComparator(value);
		if(cmp.isPresent()) {
			var fn = cmp.get();
			var cp = new RequestEntryChain(null, false, null, assertOuterParameters(values), null);
			return chainComparator(vc, fn.filter(cp.toArgs(vc, col, fn.getParameterSet())));
		}
		if(nonNull(cd)) { // no operation
			var c = cd.criteria(value);
			if(nonNull(c)) {
				var strArgs = toStringArray(assertOuterParameters(values));
				var ex = requireNonNull(c.build(strArgs), "column.criteria: " + this);
				return chainComparator(vc, col.filter(ex));
			}
			throw noSuchResourceException("comparator|criteria", value);
		}
		throw noSuchResourceException("comparator", value);
	}
	
	private List<RequestEntryChain> assertOuterParameters(List<RequestEntryChain> values) {
		if(isEmpty(values)) {
			return args;
		}
		if(isNull(args) && isLast()) {  //do not set args=values
			 return values;
		}
		throw new IllegalStateException(this + "=" + values); //denied
	}

	DBFilter chainComparator(ViewDecorator td, DBFilter f) {
		var e = next;
		while(nonNull(e)) {
			if(e.value.matches("and|or")) {
				var op = LogicalOperator.valueOf(e.value.toUpperCase());
				f = f.append(op, (DBFilter) e.toOneArg(td, JQueryType.FILTER));
				e = e.next;
			}
			else {
				throw unexpectedEntryValueException(e);
			}
		}
		return f;
	}
	
	private Optional<ViewResource> chainColumnOperations(ViewDecorator td, boolean filter) {
		return lookupResource(td).map(r-> {
			var e = r.entry.next;
			while(nonNull(e)) { //chain until !operator
				var o = e.lookupOperation(td, r.col, fn-> true); //accept all
				if(o.isEmpty()) {
					break;
				}
				r.cd = null;
				r.entry = e;
				r.col = filter && "over".equals(e.value)
						? windowColumn(r.vd, o.get())
						: o.get(); 
				e = e.next;
			}
			return r;
		});
	}
	
	private static DBColumn windowColumn(ViewDecorator vd, DBColumn col) {
		var v = requireNonNull(vd, "column view").view(); //Declared column
		var tag = "over_" + vd.identity() + "_" + col.hashCode();  //over_view_hash
		currentContext().overView(v, ()-> new RequestQueryBuilder()
				.columns(allColumns(v)).asView())
		.getBuilder().columns(col.as(tag)); //append over column
		return new ViewColumn(v, doubleQuote(tag), null, col.getType());
	}
	
	private Optional<ViewResource> lookupResource(ViewDecorator td) { //do not change priority
		if(hasNext()) { //view.id == column.id
			var rc = currentContext().lookupRegisteredView(value)
					.flatMap(v-> next.lookupViewResource(v, RequestEntryChain::isWindowFunction));
			if(rc.isPresent()) {
				requireNoArgs(); //view takes no args
				return rc;
			}
		}
		return currentContext().lookupDeclaredColumn(value)  //declared column first
				.map(c-> new ViewResource(null, null, requireNoArgs(), c)) 
				.or(()-> lookupViewResource(td, fn-> true)); //registered column
	}
	
	private Optional<ViewResource> lookupViewResource(ViewDecorator td, Predicate<TypedOperator> pre) {
		if(td instanceof QueryDecorator qd) { //query column
			return ofNullable(qd.column(value))
					.map(c-> new ViewResource(td, null, requireNoArgs(), c)); //no column decorator
		}
		return currentContext().lookupRegisteredColumn(value)
				.map(cd-> new ViewResource(td, cd, requireNoArgs(), td.column(cd)))
				.or(()-> lookupOperation(td, null, pre).map(col-> new ViewResource(td, null, this, col)));  //no column decorator
	}
	
	private Optional<OperationColumn> lookupOperation(ViewDecorator vd, DBColumn col, Predicate<TypedOperator> opr) {
		return lookupOperator(value).filter(opr).map(fn-> {
			var c = col;
			if(isNull(c) && isEmpty(args) && isCountFunction(fn)) {
				c = allColumns(vd.view());
			}
			return fn.operation(toArgs(vd, c, fn.getParameterSet()));
		});
	}

	private TaggableColumn[] toTaggableArgs(ViewDecorator td) {
		return (TaggableColumn[]) toArgs(td, JQueryType.NAMED_COLUMN);
	}

	private DBColumn[] toColumnArgs(ViewDecorator td) {
		return (DBColumn[]) toArgs(td, JQueryType.COLUMN);
	}
	
	private DBFilter[] toFilterArgs(ViewDecorator td) {
		return (DBFilter[]) toArgs(td, JQueryType.FILTER);
	}
	
	private DBOrder[] toOderArgs(ViewDecorator td) {
		return (DBOrder[]) toArgs(td, JQueryType.ORDER);
	}

	private Object[] toArgs(ViewDecorator td, JavaType type) {
		var c = type.typeClass();
		if(DBObject.class.isAssignableFrom(c)) { // JQuery types & !array
			var ps = ofParameters(required(type), varargs(type));
			return toArgs(td, null, ps, s-> (Object[]) newInstance(c, s));
		}
		throw new UnsupportedOperationException("cannot instantiate type " + c);
	}
	
	private Object toOneArg(ViewDecorator td, JavaType type) {
		return toArgs(td, null, ofParameters(required(type)))[0];
	}

	private Object[] toFilterArgs(ViewDecorator td, DBObject col, ParameterSet ps) {
		if(ps.getNReqArgs() == 2) {
			try {
				return toArgs(td, col, ofParameters(required(JQueryType.COLUMN)));
			}
			catch (Exception e) {} //TODO explicit exception
			try {
				return toArgs(td, col, ofParameters(required(JQueryType.QUERY)));
			}
			catch (Exception e) {}  //TODO explicit exception
		}
		return toArgs(td, col, ps);
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

	RequestEntryChain requireNoNext() {
		if(isLast()) {
			return this;
		}
		throw unexpectedEntryException(next);
	}

	RequestEntryChain requireNoArgs() {
		if(isNull(args)) {
			return this;
		}
		throw unexpectedEntryArgsException(this);
	}
	
	String requireTag() {
		if(nonNull(tag)) {
			return tag;
		}
		throw expectedEntryTagException(this);
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
	
	private static boolean isWindowFunction(TypedOperator op) {
		return isCountFunction(op) 
				|| op.unwrap() instanceof WindowFunction;
	}
	
	private static boolean isCountFunction(TypedOperator fn) {
		return "COUNT".equals(fn.unwrap().id());
	}
	
	private static String[] toStringArray(List<RequestEntryChain> entries) {
		return entries.stream()
				.map(e-> isNull(e.value) ? null : e.toString())
				.toArray(String[]::new);
	}
	
	static NoSuchResourceException noSuchViewColumnException(RequestEntryChain e) {
		return noSuchResourceException(COLUMN, e.hasNext() 
				&& currentContext().lookupRegisteredColumn(e.value).isPresent() 
						? e.value + "." + e.next.value
						: e.value);
	}

	@AllArgsConstructor
	static final class ViewResource {
		
		private ViewDecorator vd; //optional
		private ColumnDecorator cd; //optional
		private RequestEntryChain entry;
		private DBColumn col;
		//chained !?

		@Override
		public String toString() {
			return vd + "." + cd + " => " + entry.toString();
		}
	}
}