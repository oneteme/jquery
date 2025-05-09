package org.usf.jquery.web;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.lang.reflect.Array.newInstance;
import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.BadArgumentException.badArgumentsFormat;
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
import static org.usf.jquery.web.EntryParseException.cannotParseEntryException;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.Parameters.FILTER;
import static org.usf.jquery.web.Parameters.JOIN;
import static org.usf.jquery.web.Parameters.LIMIT;
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
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBObject;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.JQueryType;
import org.usf.jquery.core.JavaType;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.OrderType;
import org.usf.jquery.core.ParameterSet;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.QueryComposer;
import org.usf.jquery.core.QueryView;
import org.usf.jquery.core.SingleQueryColumn;
import org.usf.jquery.core.TypedOperator;
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
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class EntryChain {

	private static final String ORDER_PATTERN = enumPattern(OrderType.class); 
	private static final String PARTITION_PATTERN = join("|", PARTITION, ORDER);
	private static final Predicate<Object> ANY = o-> true;
	
	private final String value;
	private final boolean text; //"string"
	private final List<EntryChain> args; //modifiable
	private final EntryChain next;
	private final String tag;

	public EntryChain(String value) {
		this(value, false, null, null, null);
	}
	
	public EntryChain(String value, boolean text) {
		this(value, text, null, null, null);
	}
	
	public EntryChain(String value, List<EntryChain> args, EntryChain next, String tag) {
		this(value, false, args, next, tag);
	}
	
	// [view|query]:tag
	public ViewDecorator evalView(RequestContext vd) {
		return vd.lookupRegisteredView(value) //check args & next only if view exists
				.<ViewDecorator>map(v-> new ViewDecoratorWrapper(v, requireNoArgs().requireNoNext().requireTag()))
				.or(()-> evalQuery(vd, true))
				.orElseThrow(()-> noSuchResourceException(VIEW, value));
	}
	
	public SingleQueryColumn evalQueryColumn(RequestContext td) {
		return evalQuery(td, false)
				.map(QueryDecorator::getQuery)
				.map(QueryView::asColumn)
				.orElseThrow(()-> cannotParseEntryException(QUERY, this));
	}

	public ViewDecorator evalQuery(RequestContext td) {
		return evalQuery(td, false).orElseThrow(()-> cannotParseEntryException(QUERY, this));
	}
	
	//select[.distinct|filter|order|offset|fetch]*
	Optional<QueryDecorator> evalQuery(RequestContext context, boolean requireTag) { //sub context
		if(SELECT.equals(value)) {
			var e =	this;
			try {
				var q = new QueryComposer().columns(taggableVarargs(context));
				while(e.hasNext()) {
					e = e.next;
					switch(e.value) {//column not allowed 
					case FILTER: q.filters(e.filterVarargs(context)); break;
					case ORDER: q.orders(e.orderVarargs(context)); break;
					case JOIN: q.joins(e.evalJoin(context)); break;
					case LIMIT: q.limit((int)e.toOneArg(context, INTEGER)); break;
					case OFFSET: q.offset((int)e.toOneArg(context, INTEGER)); break;
					default: throw badEntrySyntaxException(e.value, join("|", FILTER, ORDER, JOIN, LIMIT, OFFSET));
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

	//[view.]join
	public ViewJoin[] evalJoin(RequestContext context) { 
		var e = this;
		var vd = context.getDefaultView();
		if(hasNext()) { 
			var res = context.lookupRegisteredView(value);
			if(res.isPresent()) {
				vd = res.get();
				e = requireNoArgs().next; //check args only if view exists
			}
		}
		var join = vd.join(e.value);
		if(nonNull(join)) {
			e.requireNoArgs().requireNoNext(); //check args & next only if joiner exists
			return requireNonNull(join.build(), vd.identity() + "." + e.value);
		}
		throw noSuchResourceException("join", vd.identity(), e.value);
	}
	
	//[view.]partition | [partition.order]*
	public Partition evalPartition(RequestContext context) {
		var e = this;
		var vd = context.getDefaultView();
		if(hasNext()) {
			var res = context.lookupRegisteredView(value);
			if(res.isPresent()) {
				vd = res.get();
				e = requireNoArgs().next; //check args only if view exists
			}
		}
		var par = vd.partition(e.value);
		if(nonNull(par)) {
			e.requireNoArgs().requireNoNext();
			return requireNonNull(par.build(), vd.identity() + "." + e.value);
		}
		var res = evalPartition2(context);
		if(res.isPresent()) {
			return res.get();
		}
		throw noSuchResourceException(vd.identity() + ".partition", e.value);
	}
	
	private Optional<Partition> evalPartition2(RequestContext context) {
		if(value.matches(PARTITION_PATTERN)) {
			List<DBColumn> cols = new ArrayList<>();
			List<DBOrder> ords = new ArrayList<>();
			var e = this;
			do {
				switch (e.value) {
				case PARTITION: addAll(cols, columnVarargs(context)); break;
				case ORDER: addAll(ords, e.orderVarargs(context)); break;
				default: throw badEntrySyntaxException(e.value, PARTITION_PATTERN);
				}
				e = e.next;
			} while(nonNull(e));
			return Optional.of(new Partition(
					cols.toArray(DBColumn[]::new), 
					ords.toArray(DBOrder[]::new)));
		}
		return empty();
	}
	
	//[view.]column[.operator(args)]*
	public DBColumn evalColumn(RequestContext td, boolean requireTag) {
		var r = chainResourceExpression(td);
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
	public DBOrder evalOrder(RequestContext td) {
		var r = chainResourceExpression(td);
		if(r.entry.isLast()) { // default order
			return r.col.order();
		}
		var ord = r.entry.next;
		if(ord.value.matches(ORDER_PATTERN)) { //check args & next only if order exists
			var s = ord.requireNoArgs().requireNoNext().value.toUpperCase();
			return r.col.order(OrderType.valueOf(s));
		}
		throw badEntrySyntaxException(ord.value, ORDER_PATTERN);
	}

	public DBFilter evalFilter(RequestContext td) {
		return evalFilter(td, null); //null ==> inner filter
	}

	//[view.]criteria | [view.]column.criteria |  [view.]column[.operator]*[.comparator][.and|or(comparator)]*
	public DBFilter evalFilter(RequestContext context, List<EntryChain> outerArgs) { //use CD.parser
		var res = chainResourceExpression(context, outerArgs);
		if(res.entry.isLast()) {
			if(res.col instanceof DBFilter f) {
				return f;
			}
			throw new IllegalArgumentException(this + " is not a filter");
		}
		throw cannotParseEntryException("", this);
	}

	private EntyChainCursor chainResourceExpression(RequestContext context) {
		return chainResourceExpression(context, null);
	}
	
	private EntyChainCursor chainResourceExpression(RequestContext context, List<EntryChain> outerArgs) {
		var r = lookupResource(context);
		if(r.isCriteria()) {
			var crArgs = r.entry.args;
			if(isEmpty(crArgs) && r.entry.isLast()) {
				crArgs = outerArgs;
				outerArgs = null; //flag outerArgs as consumed
			}
			if(nonNull(r.viewCrt)) { //view criteria
				r.col = requireResource(r.viewCrt.build(toStringArray(crArgs)), "criteria", r.vd.identity(), r.entry.value);
			}
			else if(nonNull(r.colCrt) && nonNull(r.col)) { //column criteria
				r.col = r.col.filter(requireResource(r.colCrt.build(toStringArray(crArgs)), "criteria", r.vd.identity(), r.entry.value));
			}
			else {
				throw new IllegalStateException("invalid criteria state");
			}
		}
		var e = r.entry.next;
		while(nonNull(e)) { //chain until [operator|comparator]
			var op = lookupOperator(e.value);
			if(op.isPresent()) {
				var fn = op.get();
				r.col = fn.operation(e.toArgs(context, r.col, fn.getParameterSet())); 
			}
			else {
				var oc = lookupComparator(e.value);
				if(oc.isPresent()) {
					var cp = oc.get();
					if(e.isLast() && isEmpty(e.args)) {
						e = new EntryChain(e.value, outerArgs, null, null); //chain outerArgs
						outerArgs = null; //flag outerArgs as consumed
					}
					r.col = cp.filter(e.toArgs(context, r.col, cp.getParameterSet()));
				}
				else {
					break;
				}
			}
			r.entry = e;
			e = e.next;
		}
		if(!isEmpty(outerArgs)) {
			var fn = outerArgs.size() == 1 ? eq() : in();
			e = new EntryChain(fn.id(), false, outerArgs, null, null); 
			r.col = fn.filter(e.toArgs(context, r.col, fn.getParameterSet())); //no chain
		}
		return r;
	}

	//operator|[query|view.]resource
	private EntyChainCursor lookupResource(RequestContext context) { //do not change priority
		if(hasNext()) {
			var res = context.lookupRegisteredView(value)
					.flatMap(vd-> next.lookupViewResource(context, vd, true));
			if(res.isPresent()) {
				requireNoArgs();
				return res.get();
			}
		} //!else => view.id == column.id
		return lookupViewResource(context, context.getDefaultView(), false)
				.orElseThrow(()-> cannotParseEntryException("resource", this));
	}

	private Optional<EntyChainCursor> lookupViewResource(RequestContext context, ViewDecorator vd, boolean prefixed) { //do not change priority
		return lookupViewOperation(context, vd, prefixed) //view.count only
				.or(()-> lookupDeclaredColumn(context, vd, prefixed))
				.or(()-> lookupViewCriteria(vd))
				.or(()-> lookupRegistredColumn(context, vd)); //check order
	}
	
	//operator|[view.]operator
	private Optional<EntyChainCursor> lookupViewOperation(RequestContext context, ViewDecorator vd, boolean prefixed) {
		return lookupOperator(value).filter(prefixed ? TypedOperator::isCountFunction : ANY).map(fn-> {
			var col = isEmpty(args) && fn.isCountFunction() ? allColumns(vd.view()) : null;
			return fn.operation(toArgs(context, col, fn.getParameterSet()));
		}).map(oc-> new EntyChainCursor(this, vd, oc));
	}

	//query.column|column
	private Optional<EntyChainCursor> lookupDeclaredColumn(RequestContext context, ViewDecorator vd, boolean prefixed) {
		if(prefixed) {
			return vd instanceof QueryDecorator qd
					? qd.column(value).map(col-> new EntyChainCursor(requireNoArgs(), qd, col)) 
					: empty();
		}
		return context.lookupDeclaredColumn(value)
				.map(c-> new EntyChainCursor(requireNoArgs(), null, c));
	}
	
	//view.criteria
	private Optional<EntyChainCursor> lookupViewCriteria(ViewDecorator vd) {
		return ofNullable(vd.criteria(value))
				.map(cr-> new EntyChainCursor(requireNoArgs(), vd, cr));
	}
	
	//view.column[.criteria]
	private Optional<EntyChainCursor> lookupRegistredColumn(RequestContext context, ViewDecorator vd) {
		var opt = context.lookupRegisteredColumn(value);
		if(opt.isPresent() && hasNext()) {
			var cd = opt.get();
			var cr = cd.criteria(next.value);
			if(nonNull(cr)) {
				return Optional.of(new EntyChainCursor(requireNoArgs(), vd, vd.column(cd), cr));
			}
		}
		return opt.map(cd-> new EntyChainCursor(requireNoArgs(), vd, vd.column(cd)));
	}
	
	
	private NamedColumn[] taggableVarargs(RequestContext context) {
		return (NamedColumn[]) typeVarargs(context, JQueryType.NAMED_COLUMN);
	}

	private DBColumn[] columnVarargs(RequestContext context) {
		return (DBColumn[]) typeVarargs(context, JQueryType.COLUMN);
	}
	
	private DBOrder[] orderVarargs(RequestContext context) {
		return (DBOrder[]) typeVarargs(context, JQueryType.ORDER);
	}
	
	private DBFilter[] filterVarargs(RequestContext context) {
		return (DBFilter[]) typeVarargs(context, JQueryType.FILTER);
	}

	private Object[] typeVarargs(RequestContext context, JavaType type) {
		var c = type.getCorrespondingClass();
		if(DBObject.class.isAssignableFrom(c)) { // JQuery types & !array
			var ps = ofParameters(required(type), varargs(type));
			return toArgs(context, null, ps, s-> (Object[]) newInstance(c, s));
		}
		throw new UnsupportedOperationException("cannot instantiate type " + c);
	}
	
	private Object toOneArg(RequestContext context, JavaType type) {
		return toArgs(context, null, ofParameters(required(type)))[0];
	}
	
	private Object[] toArgs(RequestContext context, DBObject col, ParameterSet ps) {
		return toArgs(context, col, ps, Object[]::new);
	}
	
	private Object[] toArgs(RequestContext context, DBObject col, ParameterSet ps, IntFunction<Object[]> arrFn) {
		int inc = isNull(col) ? 0 : 1;
		var arr = arrFn.apply(isNull(args) ? inc : args.size() + inc);
		if(nonNull(col)) {
			arr[0] = col;
		}
		try {
			ps.eachParameter(arr.length, (p,i)-> {
				if(i>=inc) { //arg0 already parsed
					var e = args.get(i-inc);
					arr[i] = isNull(e.value) || e.text
							? e.requireNoArgs().value 
							: parse(e, context, p.types(arr));
				}
			});
		}
		catch (Exception e) {
			throw new EntrySyntaxException("bad entry arguments: " +
					badArgumentsFormat(value, nonNull(args) ? args.toArray() : null), e);
		}
		return arr;
	}
	
	String requireTag() {
		if(nonNull(tag)) {
			return tag;
		}
		throw expectedEntryTagException(this);
	}

	EntryChain requireNoArgs() {
		if(isNull(args)) {
			return this;
		}
		throw new EntrySyntaxException(format("unexpected entry args : %s[(%s)]", value, joinArray(", ", args.toArray())));
	}

	EntryChain requireNoNext() {
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
			s += args.stream().map(EntryChain::toString).collect(joining(",", "(", ")"));
		}
		if(nonNull(next)) {
			s += "." + next.toString();
		}
		return isNull(tag) ? s : s + ":" + tag;
	}

	private static String[] toStringArray(List<EntryChain> entries) {
		if(!isEmpty(entries)) {
			return entries.stream()
			.map(e-> isNull(e.value) ? null : e.toString())
			.toArray(String[]::new);
		}
		return new String[0];
	}
	
	static String enumPattern(Class<? extends Enum<?>> c) {
		return Stream.of(c.getEnumConstants())
				.map(Enum::name)
				.map(String::toLowerCase)
				.collect(joining("|"));
	}
	
	static EntrySyntaxException badEntrySyntaxException(String value, String expect) {
		return new EntrySyntaxException(format("incorrect syntax: [%s], expected: %s", value, expect));
	}
	
	static EntrySyntaxException expectedEntryTagException(EntryChain e) {
		throw new EntrySyntaxException(format("expected tag: %s[:tag]", e));
	}
	
	static final <T> T requireResource(T obj, String name, String parent, String resource) {
		if(nonNull(obj)) {
			return obj;
		}
		throw noSuchResourceException(name, parent, resource);
	}
	
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	static final class EntyChainCursor {
		
		private final ViewDecorator vd;
		private final CriteriaBuilder<DBFilter> viewCrt;
		private final CriteriaBuilder<ComparisonExpression> colCrt;
		private EntryChain entry;
		private DBColumn col;

		public EntyChainCursor(EntryChain entry, ViewDecorator vd, DBColumn col) {
			this(vd, null, null, entry, col); //[view.]column
		}

		public EntyChainCursor(EntryChain entry, ViewDecorator vd, DBColumn col, CriteriaBuilder<ComparisonExpression> colCrt) {
			this(vd, null, colCrt, entry, col); //[view.]colum.criteria
		}

		public EntyChainCursor(EntryChain entry, ViewDecorator vd, CriteriaBuilder<DBFilter> viewCrt) {
			this(vd, viewCrt, null, entry, null); //[view.]criteria
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