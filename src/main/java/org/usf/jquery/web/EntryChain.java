package org.usf.jquery.web;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Comparator.eq;
import static org.usf.jquery.core.Comparator.in;
import static org.usf.jquery.core.DBColumn.allColumns;
import static org.usf.jquery.core.JDBCType.BOOLEAN;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Utils.joinArray;
import static org.usf.jquery.core.Validation.requireNArgs;
import static org.usf.jquery.web.ArgumentParsers.jdbcArgParser;
import static org.usf.jquery.web.ArgumentParsers.parse;
import static org.usf.jquery.web.ArgumentParsers.parseAll;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.Parameters.COLUMN;
import static org.usf.jquery.web.Parameters.DISTINCT;
import static org.usf.jquery.web.Parameters.FILTER;
import static org.usf.jquery.web.Parameters.JOIN;
import static org.usf.jquery.web.Parameters.LIMIT;
import static org.usf.jquery.web.Parameters.OFFSET;
import static org.usf.jquery.web.Parameters.ORDER;
import static org.usf.jquery.web.Parameters.PARTITION;
import static org.usf.jquery.web.Parameters.QUERY;
import static org.usf.jquery.web.Parameters.SELECT;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBObject;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.JQueryType;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.OrderType;
import org.usf.jquery.core.ParameterSet;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.QueryComposer;
import org.usf.jquery.core.SingleQueryColumn;
import org.usf.jquery.core.TypedOperator;
import org.usf.jquery.core.ViewJoin;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author u$f
 *
 */
@Setter(value = AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class EntryChain {

	private static final String ORDER_PATTERN = enumPattern(OrderType.class); 
	private static final String PARTITION_PATTERN = join("|", PARTITION, ORDER);
	private static final Predicate<Object> ANY = o-> true;
	
	private final String value;
	private final boolean text; //"string"
	private final EntryChain[] args;
	private final EntryChain next;
	private final String tag;

	public EntryChain(String value) {
		this(value, false, null, null, null);
	}
	
	public EntryChain(String value, boolean text) {
		this(value, text, null, null, null);
	}
	
	public EntryChain(String value, EntryChain[] args, EntryChain next, String tag) {
		this(value, false, args, next, tag);
	}
	
	//[view.]column[.expression]*
	public DBColumn evalColumn(RequestContext ctx, boolean requireTag) {
		try {
			var rsc = chainResourceExpression(ctx);
			if(rsc.entry.isLast()) {
				if(nonNull(rsc.entry.tag)) {
					return rsc.col.as(rsc.entry.tag);
				}
				if(!requireTag || rsc.col instanceof NamedColumn) {
					return rsc.col;
				}
				throw expectedEntryTagException(rsc.entry);
			}
			throw badEntrySyntaxException(rsc.entry.next.value, "criteria|operator|comparator");
		} catch (Exception e) {
			throw cannotParseEntryException(this, COLUMN, e);
		}
	}
	
	//[view.]column[.operator]*[.order]
	public DBOrder evalOrder(RequestContext ctx) {
		try {
			var rsc = chainResourceExpression(ctx);
			if(rsc.entry.isLast()) { // default order
				return rsc.col.order();
			}
			var nxt = rsc.entry.next;
			if(nxt.value.matches(ORDER_PATTERN)) { //check args & next only if order exists
				var ord = nxt.requireNoArgs().requireNoNext().value.toUpperCase();
				return rsc.col.order(OrderType.valueOf(ord));
			}
			throw badEntrySyntaxException(nxt.value, ORDER_PATTERN);
		} catch (Exception e) {
			throw cannotParseEntryException(this, ORDER, e);
		}
	}

	public DBFilter evalFilter(RequestContext ctx) {
		return evalFilter(ctx, null); //null ==> inner filter
	}

	//[view.]criteria | [view.]column.criteria | [view.]column[.operator]*[.comparator][.and|or(comparator)]*
	public DBFilter evalFilter(RequestContext ctx, EntryChain[] outerArgs) {
		try {
			var rsc = chainResourceExpression(ctx, outerArgs);
			if(rsc.entry.isLast()) {
				if(rsc.col instanceof DBFilter f) {
					return f;
				}
				throw new IllegalArgumentException(this + " is not a filter");
			}
			throw badEntrySyntaxException(rsc.entry.next.value, "criteria|operator|comparator");
		} catch (Exception e) {
			throw cannotParseEntryException(this, FILTER, e);
		}
	}
	
	//[view.]join
	public ViewJoin[] evalJoin(RequestContext ctx) {
		return hasNext()
				? ctx.lookupRegisteredView(value)
						.map(vd-> requireNoArgs().next.evalJoin(vd))
						.orElseThrow(()-> noSuchResourceException(JOIN, value))
				: evalJoin(ctx.getDefaultView());
	}
	
	private ViewJoin[] evalJoin(ViewDecorator vd) { 
		var join = vd.join(value);
		if(nonNull(join)) {
			requireNoArgs().requireNoNext(); 
			return requireNonNull(join.build(), vd.identity() + "." + value);
		}
		throw noSuchResourceException(JOIN, value, vd.identity());
	}
	
	//[view.]partition | partition(*).order(*) | order(*).partition(*)
	public Partition evalPartition(RequestContext ctx) {
		try {
			 return hasNext()
				? ctx.lookupRegisteredView(value)
						.map(vd-> requireNoArgs().next.evalPartition(vd))
						.orElseThrow(()-> noSuchResourceException(PARTITION, value))
				: evalPartition(ctx.getDefaultView());
		} catch (Exception e) {
			return parsePartition(ctx);
		}
	}
	
	private Partition evalPartition(ViewDecorator vd) { 
		var par = vd.partition(value);
		if(nonNull(par)) {
			requireNoArgs().requireNoNext(); 
			return requireNonNull(par.build(), vd.identity() + "." + value);
		}
		throw noSuchResourceException(PARTITION, vd.identity(), value);
	}

	// [view|query]:tag
	public ViewDecorator evalView(RequestContext ctx) {
		return ctx.lookupRegisteredView(value) //check args & next only if view exists
				.<ViewDecorator>map(v-> new ViewDecoratorWrapper(v, requireNoArgs().requireNoNext().requireTag()))
				.orElseGet(()-> parseQuery(ctx, true));
	}
	
	public SingleQueryColumn evalQueryColumn(RequestContext ctx) {		
		return parseQuery(ctx, false).getQuery().asColumn();
	}

	public ViewDecorator parseQuery(RequestContext ctx) {
		return parseQuery(ctx, false);
	}
	
	public boolean parseDistinct(RequestContext ctx) {
		return (boolean) jdbcArgParser(BOOLEAN).parseEntry(requireNoArgs().requireNoNext(), ctx);
	}
	
	//select[.filter|order|offset|fetch]*
	QueryDecorator parseQuery(RequestContext ctx, boolean requireTag) { //sub context
		Exception cause = null;
		if(SELECT.equals(value)) {
			var e =	this;
			try {
				var q = new QueryComposer().columns(parseAll(args, ctx, JQueryType.NAMED_COLUMN));
				while(e.hasNext()) {
					e = e.next;
					switch(e.value) {//column not allowed 
					case FILTER: q.filters(parseAll(e.args, ctx, JQueryType.FILTER)); break;
					case ORDER: q.orders(parseAll(e.args, ctx, JQueryType.ORDER)); break;
					case JOIN: q.joins(parseAll(e.args, ctx, JQueryType.JOIN)); break;
					case LIMIT: q.limit(parseInt(requireNArgs(1, e.args, ()-> LIMIT)[0].value)); break;
					case OFFSET: q.offset(parseInt(requireNArgs(1, e.args, ()-> OFFSET)[0].value)); break;
					case DISTINCT: q.distinct(e.parseDistinct(ctx)); break;
					default: throw badEntrySyntaxException(e.value, join("|", FILTER, ORDER, JOIN, LIMIT, OFFSET));
					}
				}
				return new QueryDecorator(requireTag ? e.requireTag() : e.tag, q.asView());
			}
			catch (Exception ex) {
				cause = ex;
			}
		}
		throw cannotParseEntryException(this, QUERY, cause);
	}
	
	//[partition(*).order(*)]*
	public Partition parsePartition(RequestContext ctx) {
		Exception cause = null;
		if(value.matches(PARTITION_PATTERN)) {
			try {
				var cols = new ArrayList<DBColumn>();
				var ords = new ArrayList<DBOrder>();
				var e = this;
				do {
					switch (e.value) {
					case PARTITION: addAll(cols, parseAll(e.args, ctx, JQueryType.COLUMN)); break;
					case ORDER: addAll(ords, parseAll(e.args, ctx, JQueryType.ORDER)); break;
					default: throw badEntrySyntaxException(e.value, PARTITION_PATTERN);
					}
					e = e.next;
				} while(nonNull(e));
				return new Partition(
						cols.toArray(DBColumn[]::new), 
						ords.toArray(DBOrder[]::new));
			}
			catch (Exception ex) {
				cause = ex; //with cause
			}
		}
		throw cannotParseEntryException(this, PARTITION, cause);
	}

	private EntyChainCursor chainResourceExpression(RequestContext ctx) {
		return chainResourceExpression(ctx, null);
	}
	
	private EntyChainCursor chainResourceExpression(RequestContext ctx, EntryChain[] outerArgs) {
		var r = lookupResource(ctx);
		if(r.isCriteria()) {
			var crArgs = r.entry.args;
			if(isEmpty(crArgs) && r.entry.isLast()) {
				crArgs = outerArgs;
				outerArgs = null; //flag outerArgs as consumed
			}
			if(nonNull(r.viewCrt)) { //view criteria
				r.col = requireNonNull(r.viewCrt.build(toStringArray(crArgs)), 
						()-> format("%s[criteria=%s]", r.vd.identity(), r.entry.value));
			}
			else if(nonNull(r.colCrt) && nonNull(r.col)) { //column criteria
				r.col = r.col.filter(requireNonNull(r.colCrt.build(toStringArray(crArgs)), 
						()-> format("%s[criteria=%s]", r.vd.identity(), r.entry.value)));
			}
			else {
				throw new IllegalStateException("invalid criteria state");
			}
		}
		var e = r.entry.next;
		while(nonNull(e)) { //chain until [operator|comparator]
			var op = ctx.lookupOperator(e.value);
			if(op.isPresent()) {
				var fn = op.get();
				r.col = fn.operation(e.parseArgs(ctx, r.col, fn.getParameterSet())); 
			}
			else {
				var oc = ctx.lookupComparator(e.value);
				if(oc.isPresent()) {
					var cp = oc.get();
					if(e.isLast() && isEmpty(e.args)) {
						e = new EntryChain(e.value, outerArgs, null, null); //chain outerArgs
						outerArgs = null; //flag outerArgs as consumed
					}
					r.col = cp.filter(e.parseArgs(ctx, r.col, cp.getParameterSet()));
				}
				else {
					break;
				}
			}
			r.entry = e;
			e = e.next;
		}
		if(!isEmpty(outerArgs)) {
			var fn = outerArgs.length == 1 ? eq() : in();
			e = new EntryChain(fn.id(), false, outerArgs, null, null); 
			r.col = fn.filter(e.parseArgs(ctx, r.col, fn.getParameterSet())); //no chain
		}
		return r;
	}

	//operator|[query|view.]resource
	private EntyChainCursor lookupResource(RequestContext ctx) { //do not change priority
		if(hasNext()) {
			var res = ctx.lookupRegisteredView(value)
					.flatMap(vd-> next.lookupViewResource(ctx, vd, true));
			if(res.isPresent()) {
				requireNoArgs();
				return res.get();
			}
		} //!else => view.id == column.id
		return lookupViewResource(ctx, ctx.getDefaultView(), false)
				.orElseThrow(()-> noSuchResourceException(value));
	}

	private Optional<EntyChainCursor> lookupViewResource(RequestContext ctx, ViewDecorator vd, boolean prefixed) { //do not change priority
		return lookupViewOperation(ctx, vd, prefixed) //view.count only
				.or(()-> lookupDeclaredColumn(ctx, vd, prefixed))
				.or(()-> lookupViewCriteria(vd))
				.or(()-> lookupRegistredColumn(ctx, vd));
	}
	
	//operator|[view.]operator
	private Optional<EntyChainCursor> lookupViewOperation(RequestContext ctx, ViewDecorator vd, boolean prefixed) {
		return ctx.lookupOperator(value).filter(prefixed ? TypedOperator::isCountFunction : ANY).map(fn-> {
			var col = isEmpty(args) && fn.isCountFunction() ? allColumns(vd.view()) : null;
			return fn.operation(parseArgs(ctx, col, fn.getParameterSet()));
		}).map(oc-> new EntyChainCursor(this, vd, oc));
	}

	//query.column|column
	private Optional<EntyChainCursor> lookupDeclaredColumn(RequestContext ctx, ViewDecorator vd, boolean prefixed) {
		if(prefixed) {
			return vd instanceof QueryDecorator qd
					? qd.column(value).map(col-> new EntyChainCursor(requireNoArgs(), qd, col)) 
					: empty();
		}
		return ctx.lookupDeclaredColumn(value)
				.map(c-> new EntyChainCursor(requireNoArgs(), null, c));
	}
	
	//view.criteria
	private Optional<EntyChainCursor> lookupViewCriteria(ViewDecorator vd) {
		return ofNullable(vd.criteria(value))
				.map(cr-> new EntyChainCursor(requireNoArgs(), vd, cr));
	}
	
	//view.column[.criteria]
	private Optional<EntyChainCursor> lookupRegistredColumn(RequestContext ctx, ViewDecorator vd) {
		return ctx.lookupRegisteredColumn(value).map(cd->{
			if(hasNext()) {
				var cr = cd.criteria(next.value);
				if(nonNull(cr)) {
					return new EntyChainCursor(requireNoArgs().next, vd, vd.column(cd), cr);
				}
			}
			return new EntyChainCursor(requireNoArgs(), vd, vd.column(cd));
		});
	}

	Object[] parseArgs(RequestContext ctx, DBObject col, ParameterSet ps) {
		int inc = isNull(col) ? 0 : 1;
		var arr = new Object[isNull(args) ? inc : args.length + inc];
		if(nonNull(col)) {
			arr[0] = col;
		}
		try {
			ps.eachParameter(arr.length, (p,i)-> {
				if(i>=inc) { //arg0 already parsed
					var o = args[i-inc];
					arr[i] = isNull(o.value) || o.text
							? o.requireNoArgs().value 
							: parse(o, ctx, p.types(arr));
				}
			});
		}
		catch (Exception e) {
			throw cannotParseEntryException(this, "args", e);
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
		throw new EntrySyntaxException(format("unexpected entry args : %s[(%s)]", value, joinArray(", ", args)));
	}

	EntryChain requireNoNext() {
		if(isLast()) {
			return this;
		}
		throw new EntrySyntaxException(format("unexpected entry : %s.[%s]", value, next.value));
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
			s += stream(args).map(EntryChain::toString).collect(joining(",", "(", ")"));
		}
		if(nonNull(next)) {
			s += "." + next.toString();
		}
		return isNull(tag) ? s : s + ":" + tag;
	}

	private static String[] toStringArray(EntryChain[] entries) {
		if(!isEmpty(entries)) {
			return stream(entries)
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

	static EntrySyntaxException expectedEntryTagException(EntryChain e) {
		return new EntrySyntaxException(format("expected tag: %s[:tag]", e.toString()));
	}
	
	static EntrySyntaxException badEntrySyntaxException(String value, String expect) {
		return new EntrySyntaxException(format("incorrect syntax: [%s], expected: %s", value, expect));
	}
	
	static EntryParseException cannotParseEntryException(EntryChain entry, String type, Exception ex) {
		return new EntryParseException(format("cannot parse %s : '%s'", type, entry.toString()), ex);
	}
	
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	final class EntyChainCursor {
		
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