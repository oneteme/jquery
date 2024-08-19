package org.usf.jquery.web;

import static java.lang.String.format;
import static java.lang.reflect.Array.newInstance;
import static java.util.Collections.addAll;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
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
import static org.usf.jquery.core.Utils.join;
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
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.usf.jquery.core.AggregateFunction;
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
import org.usf.jquery.core.QueryColumn;
import org.usf.jquery.core.QueryView;
import org.usf.jquery.core.RequestQueryBuilder;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.TypedOperator;
import org.usf.jquery.core.Utils;
import org.usf.jquery.core.ViewColumn;
import org.usf.jquery.core.ViewJoin;
import org.usf.jquery.core.WindowFunction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@RequiredArgsConstructor
final class RequestEntryChain {

	private static final String ORDER_PATTERN = enumPattern(Order.class); 
	private static final String LOGIC_PATTERN = enumPattern(LogicalOperator.class); 
	
	private final String value;
	private final boolean text; //"string"
	private RequestEntryChain next;
	private List<RequestEntryChain> args;
	private String tag;

	public RequestEntryChain(String value) {
		this(value, false);
	}
	
	// [view|query]:tag
	public ViewDecorator evalView(ViewDecorator vd) {
		return currentContext().lookupRegisteredView(value) //check args & next only if view exists
				.<ViewDecorator>map(v-> new ViewDecoratorWrapper(v, requireNoArgs().requireNoNext().requireTag()))
				.or(()-> evalQuery(vd, true))
				.orElseThrow(()-> noSuchResourceException(VIEW, value));
	}
	
	public QueryColumn evalQueryColumn(ViewDecorator td) {
		return evalQuery(td, false)
				.map(QueryDecorator::getQuery)
				.map(QueryView::asColumn)
				.orElseThrow(()-> cannotParseEntryException(QUERY, this));
	}

	//TODO level isolation : window function
	public ViewDecorator evalQuery(ViewDecorator td) {
		return evalQuery(td, false).orElseThrow(()-> cannotParseEntryException(QUERY, this));
	}
	
	//select[.distinct|filter|order|offset|fetch]*
	Optional<QueryDecorator> evalQuery(ViewDecorator td, boolean requireTag) { //sub context
		if(SELECT.equals(value)) {
			var e =	this;
			try {
				var q = new RequestQueryBuilder().columns(taggableVarargs(td));
				while(e.hasNext()) {
					e = e.next;
					switch(e.value) {//column not allowed 
					case DISTINCT: e.requireNoArgs(); q.distinct(); break;
					case FILTER: q.filters(e.filterVarargs(td)); break;
					case ORDER: q.orders(e.oderVarargs(td)); break; //not sure
					case JOIN: q.joins(e.evalJoin(td)); break;
					case OFFSET: q.offset((int)e.toOneArg(td, INTEGER)); break;
					case FETCH: q.fetch((int)e.toOneArg(td, INTEGER)); break;
					default: throw badEntrySyntaxException(join("|", DISTINCT, FILTER, ORDER, JOIN, OFFSET, FETCH), e.value);
					}
				}
				return Optional.of(new QueryDecorator(requireTag ? e.requireTag() : e.tag, q.asView()));
			}
			catch (Exception ex) {
				throw new EntrySyntaxException("incorrect query syntax: " + e);
			}
		}
		return empty();
	}

	//[view.]joiner
	public ViewJoin[] evalJoin(ViewDecorator vd) { 
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
	
	//[partition.order]*
	public Partition evalPartition(ViewDecorator td) {
		List<DBColumn> cols = new ArrayList<>();
		List<DBOrder> ords = new ArrayList<>();
		var e = this;
		do {
			switch (e.value) {
			case PARTITION: addAll(cols, columnVarargs(td)); break;
			case ORDER: addAll(ords, e.oderVarargs(td)); break;
			default: throw e==this
				? cannotParseEntryException(PARTITION, e) //first entry
				: badEntrySyntaxException(join("|", PARTITION, ORDER), e.value);
			}
			e = e.next;
		} while(nonNull(e));
		return new Partition(
				cols.toArray(DBColumn[]::new), 
				ords.toArray(DBOrder[]::new));
	}
	
	//[view.]column[.operator]*
	public DBColumn evalColumn(ViewDecorator td, boolean requireTag, boolean declare) {
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
	}
	
	//[view.]column[.operator]*[.order]
	public DBOrder evalOrder(ViewDecorator td) {
		var r = chainColumnOperations(td, false)
				.orElseThrow(()-> noSuchViewColumnException(this));
		if(r.entry.isLast()) { // default order
			return r.col.order();
		}
		var ord = r.entry.next;
		if(ord.value.matches(ORDER_PATTERN)) { //check args & next only if order exists
			var s = ord.requireNoArgs().requireNoNext().value.toUpperCase();
			return r.col.order(Order.valueOf(s));
		}
		throw badEntrySyntaxException(ORDER_PATTERN, ord.value);
	}

	public DBFilter evalFilter(ViewDecorator td) {
		return evalFilter(td, emptyList());
	}

	//[view.]criteria | [view.]column.criteria |  [view.]column[.operator]*[.comparator]
	public DBFilter evalFilter(ViewDecorator vd, List<RequestEntryChain> values) { //supply values
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
			var cp = new RequestEntryChain(value, false, null, assertOuterParameters(values), null);
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
			if(e.value.matches(LOGIC_PATTERN)) {
				var op = LogicalOperator.valueOf(e.value.toUpperCase());
				f = f.append(op, (DBFilter) e.toOneArg(td, JQueryType.FILTER));
				e = e.next;
			}
			else {
				throw badEntrySyntaxException(LOGIC_PATTERN, e.value);
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

	private TaggableColumn[] taggableVarargs(ViewDecorator td) {
		return (TaggableColumn[]) typeVarargs(td, JQueryType.NAMED_COLUMN);
	}

	private DBColumn[] columnVarargs(ViewDecorator td) {
		return (DBColumn[]) typeVarargs(td, JQueryType.COLUMN);
	}
	
	private DBOrder[] oderVarargs(ViewDecorator td) {
		return (DBOrder[]) typeVarargs(td, JQueryType.ORDER);
	}
	
	private DBFilter[] filterVarargs(ViewDecorator td) {
		return (DBFilter[]) typeVarargs(td, JQueryType.FILTER);
	}

	private Object[] typeVarargs(ViewDecorator td, JavaType type) {
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
		}
		catch (Exception e) {
			throw new EntrySyntaxException(format("bad entry arguments : %s[(%s)]", value, isNull(args) ? "" : join(", ", args.toArray())), e);
		}
		return arr;
	}
	
	String requireTag() {
		if(nonNull(tag)) {
			return tag;
		}
		throw new EntrySyntaxException(format("expected tag : %s[:tag]", this));
	}

	RequestEntryChain requireNoArgs() {
		if(isNull(args)) {
			return this;
		}
		throw new EntrySyntaxException(format("unexpected entry args : %s[(%s)]", value, join(", ", args.toArray())));
	}

	RequestEntryChain requireNoNext() {
		if(isLast()) {
			return this;
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
		var fn = op.unwrap();
		return fn instanceof WindowFunction || fn instanceof AggregateFunction; //rank() | sum(col)
	}
	
	private static boolean isCountFunction(TypedOperator fn) {
		return "COUNT".equals(fn.unwrap().id());
	}
	
	private static String[] toStringArray(List<RequestEntryChain> entries) {
		if(!isEmpty(entries)) {
			return entries.stream()
			.map(e-> isNull(e.value) ? null : e.toString())
			.toArray(String[]::new);
		}
		return new String[0];
	}
	
	static NoSuchResourceException noSuchViewColumnException(RequestEntryChain e) {
		return noSuchResourceException(COLUMN, e.hasNext() 
				&& currentContext().lookupRegisteredColumn(e.value).isPresent() 
						? e.value + "." + e.next.value
						: e.value);
	}
	
	static String enumPattern(Class<? extends Enum<?>> c) {
		return Stream.of(c.getEnumConstants())
				.map(Enum::name)
				.map(String::toLowerCase)
				.collect(joining("|"));
	}
	
	static EntrySyntaxException badEntrySyntaxException(String type, String value) {
		return new EntrySyntaxException(format("incorrect syntax expected: %s, bat was: %s", type, value));
	}
	
	static EntrySyntaxException expectedEntryTagException(RequestEntryChain e) {
		throw new EntrySyntaxException(format("unexpected entry : %s[.%s]", e.value, e.next));
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