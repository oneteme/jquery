package org.usf.jquery.web;

import static java.lang.String.format;
import static java.lang.reflect.Array.newInstance;
import static java.util.Collections.addAll;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
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
import static org.usf.jquery.core.Utils.joinArray;
import static org.usf.jquery.web.ArgumentParsers.parse;
import static org.usf.jquery.web.ContextManager.currentContext;
import static org.usf.jquery.web.EntryParseException.cannotParseEntryException;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.Parameters.DISTINCT;
import static org.usf.jquery.web.Parameters.FETCH;
import static org.usf.jquery.web.Parameters.FILTER;
import static org.usf.jquery.web.Parameters.JOIN;
import static org.usf.jquery.web.Parameters.OFFSET;
import static org.usf.jquery.web.Parameters.ORDER;
import static org.usf.jquery.web.Parameters.PARTITION;
import static org.usf.jquery.web.Parameters.QUERY;
import static org.usf.jquery.web.Parameters.SELECT;
import static org.usf.jquery.web.Parameters.VIEW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import org.usf.jquery.core.ComparisonExpression;
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
import org.usf.jquery.core.QueryContext;
import org.usf.jquery.core.QueryView;
import org.usf.jquery.core.RequestQueryBuilder;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.TypedComparator;
import org.usf.jquery.core.ViewColumn;
import org.usf.jquery.core.ViewJoin;

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
	public ViewDecorator evalView(ViewDecorator vd, QueryContext ctx) {
		return currentContext().lookupRegisteredView(value) //check args & next only if view exists
				.<ViewDecorator>map(v-> new ViewDecoratorWrapper(v, requireNoArgs().requireNoNext().requireTag()))
				.or(()-> evalQuery(vd, ctx, true))
				.orElseThrow(()-> noSuchResourceException(VIEW, value));
	}
	
	public QueryColumn evalQueryColumn(ViewDecorator td, QueryContext ctx) {
		return evalQuery(td, ctx, false)
				.map(QueryDecorator::getQuery)
				.map(QueryView::asColumn)
				.orElseThrow(()-> cannotParseEntryException(QUERY, this));
	}

	public ViewDecorator evalQuery(ViewDecorator td, QueryContext ctx) {
		return evalQuery(td, ctx, false).orElseThrow(()-> cannotParseEntryException(QUERY, this));
	}
	
	//select[.distinct|filter|order|offset|fetch]*
	Optional<QueryDecorator> evalQuery(ViewDecorator td, QueryContext ctx, boolean requireTag) { //sub context
		if(SELECT.equals(value)) {
			var e =	this;
			try {
				var q = new RequestQueryBuilder().columns(taggableVarargs(td, ctx));
				while(e.hasNext()) {
					e = e.next;
					switch(e.value) {//column not allowed 
					case DISTINCT: e.requireNoArgs(); q.distinct(); break;
					case FILTER: q.filters(e.filterVarargs(td, ctx)); break;
					case ORDER: q.orders(e.oderVarargs(td, ctx)); break;
					case JOIN: q.joins(e.evalJoin(td, ctx)); break;
					case OFFSET: q.offset((int)e.toOneArg(td, ctx, INTEGER)); break;
					case FETCH: q.fetch((int)e.toOneArg(td, ctx, INTEGER)); break;
					default: throw badEntrySyntaxException(joinArray("|", DISTINCT, FILTER, ORDER, JOIN, OFFSET, FETCH), e.value);
					}
				}
				return Optional.of(new QueryDecorator(requireTag ? e.requireTag() : e.tag, q.asView()));
			}
			catch (EntryParseException | NoSuchResourceException ex) {
				throw new EntrySyntaxException("incorrect query syntax: " + e, ex);
			}
		}
		return empty();
	}

	//[view.]joiner
	public ViewJoin[] evalJoin(ViewDecorator vd, QueryContext ctx) { 
		var e = this;
		if(hasNext()) { 
			vd = currentContext().lookupRegisteredView(value)
					.orElseThrow(()-> noSuchResourceException(VIEW, value));
			e = requireNoArgs().next; //check args only if view exists
		}
		var join = vd.join(e.value);
		if(nonNull(join)) {
			e.requireNoArgs().requireNoNext(); //check args & next only if joiner exists
			return requireNonNull(join.build(ctx), vd.identity() + ".join: " + e);
		}
		throw noSuchResourceException(vd.identity() + ".join", e.value);
	}
	
	//[partition.order]*
	public Partition evalPartition(ViewDecorator vd, QueryContext ctx) {
		var e = this;
		if(hasNext()) {
			var res = currentContext().lookupRegisteredView(value);
			if(res.isPresent()) {
				vd = res.get();
				e = requireNoArgs().next; //check args only if view exists
			}
		}
		var par = vd.partition(e.value);
		if(nonNull(par)) {
			e.requireNoArgs().requireNoNext();
			return requireNonNull(par.build(ctx), vd.identity() + ".partition: " + e);
		}
		if(e == this) { // not view
			var res = evalPartition2(vd, ctx);
			if(res.isPresent()) {
				return res.get();
			}
		}
		throw noSuchResourceException(vd.identity() + ".partition", e.value);
	}
	
	private Optional<Partition> evalPartition2(ViewDecorator vd, QueryContext ctx) {
		List<DBColumn> cols = new ArrayList<>();
		List<DBOrder> ords = new ArrayList<>();
		var e = this;
		do {
			switch (e.value) {
			case PARTITION: addAll(cols, columnVarargs(vd, ctx)); break;
			case ORDER: addAll(ords, e.oderVarargs(vd, ctx)); break;
			default: 
				if(e == this) {
					return empty(); //cannotParseEntryException(PARTITION, e) //first entry
				}
				throw badEntrySyntaxException(joinArray("|", PARTITION, ORDER), e.value);
			}
			e = e.next;
		} while(nonNull(e));
		return Optional.of(new Partition(
				cols.toArray(DBColumn[]::new), 
				ords.toArray(DBOrder[]::new)));
	}
	
	//[view.]column[.operator]*
	public DBColumn evalColumn(ViewDecorator td, QueryContext ctx, boolean requireTag) {
		var r = chainColumnOperations(td, ctx, false);
		r.entry.requireNoNext(); //check next only if column exists
		if(nonNull(r.entry.tag)) {
			return r.col.as(r.entry.tag);
		}
		if(!requireTag || r.col instanceof NamedColumn) {
			return r.col;
		}
		throw expectedEntryTagException(r.entry);
	}
	
	//[view.]column[.operator]*[.order]
	public DBOrder evalOrder(ViewDecorator td, QueryContext ctx) {
		var r = chainColumnOperations(td, ctx, false);
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

	public DBFilter evalFilter(ViewDecorator td, QueryContext ctx) {
		return evalFilter(td, ctx, emptyList());
	}

	//[view.]criteria | [view.]column.criteria |  [view.]column[.operator]*[.comparator][.and|or(comparator)]*
	public DBFilter evalFilter(ViewDecorator vd, QueryContext ctx, List<RequestEntryChain> values) { //use CD.parser
		var rc = chainColumnOperations(vd, ctx, true);
		if(rc.isCriteria()) {
			var strArgs = toStringArray(rc.entry.assertOuterParameters(values));
			if(nonNull(rc.viewCrt)) { //view criteria
				var f = requireNonNull(rc.viewCrt.build(ctx, strArgs), "view.criteria: " + rc.entry);
				return rc.entry.chainComparator(vd, ctx, f);
			}
			if(nonNull(rc.colCrt) && nonNull(rc.col)) { //column criteria
				var ex = requireNonNull(rc.colCrt.build(ctx, strArgs), "column.criteria: " + rc.entry);
				return rc.entry.chainComparator(vd, ctx, rc.col.filter(ex));
			}
		}
		else if(nonNull(rc.col)) {
			if(nonNull(rc.cmp)) { //column comparator
				var cp = new RequestEntryChain(rc.entry.value, false, null, rc.entry.assertOuterParameters(values), null);
				return rc.entry.chainComparator(vd, ctx, rc.cmp.filter(cp.toArgs(vd, ctx, rc.col, rc.cmp.getParameterSet())));
			}
			if(rc.entry.isLast()) { // no criteria, no comparator
				var fn = requireNonNull(values).size() == 1 ? eq() : in(); //non empty
				var e = new RequestEntryChain(null, false, null, values, null); 
				return fn.filter(e.toArgs(vd, ctx, rc.col, fn.getParameterSet())); //no chain
			}
			throw noSuchResourceException("comparator|criteria", rc.entry.next.value);
		}
		throw new IllegalStateException("illegal ViewResource: " + rc);
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

	DBFilter chainComparator(ViewDecorator td, QueryContext ctx, DBFilter f) {
		var e = next;
		while(nonNull(e)) {
			if(e.value.matches(LOGIC_PATTERN)) {
				var op = LogicalOperator.valueOf(e.value.toUpperCase());
				f = f.append(op, (DBFilter) e.toOneArg(td, ctx, JQueryType.FILTER));
				e = e.next;
			}
			else {
				throw badEntrySyntaxException(LOGIC_PATTERN, e.value);
			}
		}
		return f;
	}
	
	private ViewResource chainColumnOperations(ViewDecorator vd, QueryContext ctx, boolean filter) {
		var r = lookupResource(vd, ctx, filter);
		if(!r.isFilter()) { // !criteria & !comparator
			var e = r.entry.next;
			while(nonNull(e)) { //chain until !operator
				var res = lookupOperator(e.value);
				if(res.isPresent()) {
					var fn = res.get();
					var o = fn.operation(e.toArgs(vd, ctx, r.col, fn.getParameterSet()));
					r.cd = null;
					r.entry = e;
					r.col = filter && "over".equals(e.value) ? windowColumn(r.vd, ctx, o) : o; 
					e = e.next;
				}
				else {
					break;
				}
			}
		} //else filter
		return r;
	}
	
	private static DBColumn windowColumn(ViewDecorator vd, QueryContext ctx, DBColumn col) {
		var v = vd.view();
		var tag = "over_" + vd.identity() + "_" + col.hashCode();  //over_view_hash
		ctx.overView(v).getBuilder().columns(col.as(tag)); //append over colum
		return new ViewColumn(doubleQuote(tag), v, col.getType(), null);
	}
	
	//view.resource | resource
	private ViewResource lookupResource(ViewDecorator vd, QueryContext ctx, boolean filter) { //do not change priority
		if(hasNext()) {
			var rc = currentContext().lookupRegisteredView(value);
			if(rc.isPresent()) {
				var res = next.lookupQueryResource(rc.get()) //declared query first
						.or(()-> next.lookupViewResource(vd, ctx, rc.get(), filter));
				if(res.isPresent()) {
					requireNoArgs();
					return res.get();
				}
			}
		} //view.id == column.id
		return ctx.lookupDeclaredColumn(value)
				.map(c-> new ViewResource(requireNoArgs(), null, null, c)) //declared column first
				.or(()-> lookupViewResource(vd, ctx, null, filter)) //registered column
				.orElseThrow(()-> noSuchViewColumnException(this)); //no such resource
	}

	//query.column
	private Optional<ViewResource> lookupQueryResource(ViewDecorator vd) {
		if(vd instanceof QueryDecorator qd) {
			try {
				var col = qd.column(value);
				return Optional.of(new ViewResource(requireNoArgs(), qd, null, col));
			} catch (Exception e) {/*do not throw exception*/}
		}
		return empty();
	}
	
	//view[.operator|column[.criteria]|criteria]
	private Optional<ViewResource> lookupViewResource(ViewDecorator vd, QueryContext ctx, ViewDecorator current, boolean filter) {
		var cur = requireNonNullElse(current, vd);
		var res = lookupViewOperation(vd, ctx, current) 
				.map(col-> new ViewResource(this, cur, null, col)) 
				.or(()-> lookupColumnResource(cur, filter));
		return filter ? res.or(()-> ofNullable(cur.criteria(value)).map(crt-> new ViewResource(this, cur, crt))) : res;
	}
	
	private Optional<OperationColumn> lookupViewOperation(ViewDecorator vd, QueryContext ctx, ViewDecorator current) {
		 //view.[count|rank|rowNumber|danseRank] only
		return lookupOperator(value, isNull(current) ? null : op-> op.isCountFunction() || op.isWindowFunction()).map(fn-> {
			var col = isEmpty(args) && fn.isCountFunction() 
					? allColumns(requireNonNullElse(current, vd).view()) 
					: null;
			return fn.operation(toArgs(vd, ctx, col, fn.getParameterSet()));
		});
	}
	
	private Optional<ViewResource> lookupColumnResource(ViewDecorator td, boolean filter) {
		var res = currentContext().lookupRegisteredColumn(value);
		if(res.isPresent()) {
			var cd = res.get();
			try {
				var col = td.column(cd); //throw exception
				if(filter && hasNext()) {
					var cmp = lookupComparator(next.value);
					if(cmp.isPresent()) {
						return Optional.of(new ViewResource(requireNoArgs().next, td, cd, col, cmp.get()));
					}
					var crt = cd.criteria(next.value); //TD !operator
					if(nonNull(crt)) {
						return Optional.of(new ViewResource(requireNoArgs().next, td, cd, col, crt));
					}
				}
				return Optional.of(new ViewResource(requireNoArgs(), td, cd, col));
			}
			catch(Exception e) {/*do not throw exception*/} //TD specific exception
		}
		return empty();
	}

	private NamedColumn[] taggableVarargs(ViewDecorator td, QueryContext ctx) {
		return (NamedColumn[]) typeVarargs(td, ctx, JQueryType.NAMED_COLUMN);
	}

	private DBColumn[] columnVarargs(ViewDecorator td, QueryContext ctx) {
		return (DBColumn[]) typeVarargs(td, ctx, JQueryType.COLUMN);
	}
	
	private DBOrder[] oderVarargs(ViewDecorator td, QueryContext ctx) {
		return (DBOrder[]) typeVarargs(td, ctx, JQueryType.ORDER);
	}
	
	private DBFilter[] filterVarargs(ViewDecorator td, QueryContext ctx) {
		return (DBFilter[]) typeVarargs(td, ctx, JQueryType.FILTER);
	}

	private Object[] typeVarargs(ViewDecorator td, QueryContext ctx, JavaType type) {
		var c = type.getCorrespondingClass();
		if(DBObject.class.isAssignableFrom(c)) { // JQuery types & !array
			var ps = ofParameters(required(type), varargs(type));
			return toArgs(td, ctx, null, ps, s-> (Object[]) newInstance(c, s));
		}
		throw new UnsupportedOperationException("cannot instantiate type " + c);
	}
	
	private Object toOneArg(ViewDecorator td, QueryContext ctx, JavaType type) {
		return toArgs(td, ctx, null, ofParameters(required(type)))[0];
	}
	
	private Object[] toArgs(ViewDecorator td, QueryContext ctx, DBObject col, ParameterSet ps) {
		return toArgs(td, ctx, col, ps, Object[]::new);
	}
	
	private Object[] toArgs(ViewDecorator td, QueryContext ctx, DBObject col, ParameterSet ps, IntFunction<Object[]> arrFn) {
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
							: parse(e, td, ctx, p.types(arr));
				}
			});
		}
		catch (Exception e) {
			throw new EntrySyntaxException(format("bad entry arguments : %s[(%s)]", 
					requireNonNullElse(value, ""), isNull(args) ? "" : joinArray(", ", args.toArray())), e);
		}
		return arr;
	}
	
	String requireTag() {
		if(nonNull(tag)) {
			return tag;
		}
		throw expectedEntryTagException(this);
	}

	RequestEntryChain requireNoArgs() {
		if(isNull(args)) {
			return this;
		}
		throw new EntrySyntaxException(format("unexpected entry args : %s[(%s)]", value, joinArray(", ", args.toArray())));
	}

	RequestEntryChain requireNoNext() {
		if(isLast()) {
			return this;
		}
		throw new EntrySyntaxException(format("unexpected entry : %s[.%s]", value, next));
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

	private static String[] toStringArray(List<RequestEntryChain> entries) {
		if(!isEmpty(entries)) {
			return entries.stream()
			.map(e-> isNull(e.value) ? null : e.toString())
			.toArray(String[]::new);
		}
		return new String[0];
	}
	
	static NoSuchResourceException noSuchViewColumnException(RequestEntryChain e) {
		return noSuchResourceException("resource", e.hasNext() 
				&& currentContext().lookupRegisteredView(e.value).isPresent() 
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
		throw new EntrySyntaxException(format("expected tag : %s[:tag]", e));
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	static final class ViewResource {
		
		private RequestEntryChain entry;
		private ViewDecorator vd;
		private ColumnDecorator cd;
		private DBColumn col;
		private CriteriaBuilder<DBFilter> viewCrt;
		private CriteriaBuilder<ComparisonExpression> colCrt;
		private TypedComparator cmp;

		public ViewResource(RequestEntryChain entry, ViewDecorator vd, ColumnDecorator cd, DBColumn col) {
			this(entry, vd, cd, col, null, null, null); //[view.]column
		}

		public ViewResource(RequestEntryChain entry, ViewDecorator vd, ColumnDecorator cd, DBColumn col, CriteriaBuilder<ComparisonExpression> colCrt) {
			this(entry, vd, cd, col, null, colCrt, null); //[view.]colum.criteria
		}

		public ViewResource(RequestEntryChain entry, ViewDecorator vd, ColumnDecorator cd, DBColumn col, TypedComparator cmp) {
			this(entry, vd, cd, col, null, null, cmp); //[view.]colum.comparator
		}

		public ViewResource(RequestEntryChain entry, ViewDecorator vd, CriteriaBuilder<DBFilter> viewCrt) {
			this(entry, vd, null, null, viewCrt, null, null); //[view.]criteria
		}
		
		boolean isFilter() {
			return nonNull(cmp) || isCriteria();
		}
		
		boolean isCriteria() {
			return nonNull(viewCrt) || nonNull(colCrt);
 		}
		
		@Override
		public String toString() {
			return entry.toString();
		}
	}
}