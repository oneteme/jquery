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
import static org.usf.jquery.core.Column.allColumns;
import static org.usf.jquery.core.Dialect.getDialect;
import static org.usf.jquery.core.JDBCType.BOOLEAN;
import static org.usf.jquery.core.JQueryType.COLUMN;
import static org.usf.jquery.core.JQueryType.FILTER;
import static org.usf.jquery.core.JQueryType.JOIN;
import static org.usf.jquery.core.JQueryType.NAMED_COLUMN;
import static org.usf.jquery.core.JQueryType.ORDER;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNArgs;
import static org.usf.jquery.web.ArgumentParsers.jdbcArgParser;
import static org.usf.jquery.web.ArgumentParsers.parse;
import static org.usf.jquery.web.ArgumentParsers.parseAll;
import static org.usf.jquery.web.JQuery.currentEnvironment;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.Parameters.COLUMN_PARAM;
import static org.usf.jquery.web.Parameters.DISTINCT_PARAM;
import static org.usf.jquery.web.Parameters.FILTER_OPR;
import static org.usf.jquery.web.Parameters.JOIN_PARAM;
import static org.usf.jquery.web.Parameters.LIMIT_PARAM;
import static org.usf.jquery.web.Parameters.OFFSET_PARAM;
import static org.usf.jquery.web.Parameters.ORDER_PARAM;
import static org.usf.jquery.web.Parameters.PARTITION_OPR;
import static org.usf.jquery.web.Parameters.SELECT_OPR;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import org.usf.jquery.core.Column;
import org.usf.jquery.core.Criteria;
import org.usf.jquery.core.DBObject;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.OperatorDefinition;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.OrderType;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.Predicate;
import org.usf.jquery.core.Signature;
import org.usf.jquery.core.SingleQueryColumn;
import org.usf.jquery.core.ViewJoin;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author u$f
 *
 */
@Deprecated
@EqualsAndHashCode(exclude = "prev")
@Setter(value = AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class EntryChain {

	private static final String ORDER_PATTERN = enumPattern(OrderType.class); 
	private static final String PARTITION_PATTERN = join("|", PARTITION_OPR, ORDER_PARAM);
	
	private final String value;
	private final boolean text; //"string"
	private final EntryChain[] args;
	private final EntryChain next;
	private final String tag;

	private EntryChain prev;
	
	public EntryChain(String value) {
		this(value, false, null, null, null);
	}
	
	public EntryChain(String value, boolean text) {
		this(value, text, null, null, null);
	}
	
	public EntryChain(String value, EntryChain[] args, EntryChain next, String tag) {
		this(value, false, args, next, tag);
		if(nonNull(next)) {
			next.prev = this; //two way chain
		}
	}
	
	//[view.]column[.expression]*
	public Column evalColumn(QueryContext ctx, boolean requireTag) {
		try {
			var cur = chainResourceExpression(ctx);
			if(cur.entry.isLast()) {
				if(nonNull(cur.entry.tag)) {
					return cur.col.as(cur.entry.tag);
				}
				if(!requireTag || cur.col instanceof NamedColumn) {
					return cur.col;
				}
				throw badEntryTagException(cur.entry);
			}
			throw badEntryChainException(cur.entry.next, "criteria|operator|comparator");
		} catch (Exception e) {
			throw cannotParseEntryException(this, COLUMN_PARAM, e);
		}
	}
	
	//[view.]column[.operator]*[.order]
	public Order evalOrder(QueryContext ctx) {
		try {
			var rsc = chainResourceExpression(ctx);
			if(rsc.entry.isLast()) { // default order
				return rsc.col.order();
			}
			var nxt = rsc.entry.next;
			if(nxt.value.matches(ORDER_PATTERN)) {
				var ord = nxt.requireNoArgs().requireNoNext().value.toUpperCase();
				return rsc.col.order(OrderType.valueOf(ord));
			}
			throw badEntryChainException(nxt, ORDER_PATTERN);
		} catch (Exception e) {
			throw cannotParseEntryException(this, ORDER_PARAM, e);
		}
	}

	public Criteria evalFilter(QueryContext ctx) {
		return evalFilter(ctx, null); //null ==> inner filter
	}

	//[view.]criteria | [view.]column.criteria | [view.]column[.operator]*[.comparator][.and|or(comparator)]*
	public Criteria evalFilter(QueryContext ctx, EntryChain[] outerArgs) {
		try {
			var rsc = chainResourceExpression(ctx, outerArgs);
			if(rsc.entry.isLast()) {
				if(rsc.col instanceof Criteria f) {
					return f;
				}
				throw new EntrySyntaxException(this + " is not a filter");
			}
			throw badEntryChainException(rsc.entry.next, "criteria|operator|comparator");
		} catch (Exception e) {
			throw cannotParseEntryException(this, FILTER_OPR, outerArgs, e);
		}
	}
	
	//[view.]join
	public ViewJoin[] evalJoin(QueryContext ctx) {
		try {
			return hasNext()
					? ctx.lookupRegisteredView(value)
							.map(vd-> requireNoArgs().next.evalJoin(vd))
							.orElseThrow(()-> noSuchResourceException(value))
					: evalJoin(ctx.getDefaultView());
		} catch (Exception e) {
			throw cannotParseEntryException(this, JOIN_PARAM, e);
		}
	}
	
	private ViewJoin[] evalJoin(ViewDecorator vd) { 
		var join = vd.joinBuilder(value);
		if(nonNull(join)) {
			return build(JOIN_PARAM, join, vd, requireNoNext()); //takes args but not next
		}
		throw noSuchResourceException(JOIN_PARAM, value, vd.identity());
	}
	
	//[view.]partition | partition(*).order(*) | order(*).partition(*)
	public Partition evalPartition(QueryContext ctx) {
		var opt = parsePartition(ctx);
		if(opt.isPresent()) {
			return opt.get();
		}
		try {
			 return hasNext()
				? ctx.lookupRegisteredView(value)
						.map(vd-> requireNoArgs().next.evalPartition(vd))
						.orElseThrow(()-> noSuchResourceException(value))
				: evalPartition(ctx.getDefaultView());
		} catch (Exception e) {
			throw cannotParseEntryException(this, PARTITION_OPR, e);
		}
	}
	
	private Partition evalPartition(ViewDecorator vd) { 
		var par = vd.partitionBuilder(value);
		if(nonNull(par)) {
			return build(PARTITION_OPR, par, vd, requireNoNext()); //takes args but not next
		}
		throw noSuchResourceException(PARTITION_OPR, vd.identity(), value);
	}

	// [view|query]:tag
	public ViewDecorator evalView(QueryContext ctx) {
		return ctx.lookupRegisteredView(value) //check args & next only if view exists
				.<ViewDecorator>map(v-> new ViewDecoratorWrapper(v, requireNoArgs().requireNoNext().requireTag()))
				.orElseGet(()-> parseQuery(ctx, true));
	}
	
	public SingleQueryColumn evalQueryColumn(QueryContext ctx) {		
		return parseQuery(ctx, false).view().asColumn();
	}

	public ViewDecorator parseQuery(QueryContext ctx) {
		return parseQuery(ctx, false);
	}
	
	public boolean parseDistinct(QueryContext ctx) {
		return (boolean) jdbcArgParser(BOOLEAN).parseEntry(requireNoArgs().requireNoNext(), ctx);
	}
	
	//select[.filter|order|offset|fetch]*
	QueryDecorator parseQuery(QueryContext ctx, boolean requireTag) {
		Exception cause = null;
		if(SELECT_OPR.equals(value)) {
			try {
				var e = new EntryChain[] {this}; //mutable reference
				var qry = currentEnvironment().query(q->{
					var subCtx = new QueryContext(ctx.getDefaultView());
					q.columns(parseAll(args, subCtx, NAMED_COLUMN)); //context isolation
					while(e[0].hasNext()) {
						e[0] = e[0].next;
						switch(e[0].value) {//column not allowed 
						case FILTER_OPR: q.filters(parseAll(e[0].args, subCtx, FILTER)); break;
						case ORDER_PARAM: q.orders(parseAll(e[0].args, subCtx, ORDER)); break;
						case JOIN_PARAM: q.joins(parseAll(e[0].args, subCtx, JOIN)); break;
						case LIMIT_PARAM: q.limit(parseInt(requireNArgs(1, e[0].args, ()-> LIMIT_PARAM)[0].value)); break;
						case OFFSET_PARAM: q.offset(parseInt(requireNArgs(1, e[0].args, ()-> OFFSET_PARAM)[0].value)); break;
						case DISTINCT_PARAM: q.distinct(e[0].parseDistinct(subCtx)); break;
						default: throw badEntryChainException(e[0], join("|", FILTER_OPR, ORDER_PARAM, JOIN_PARAM, LIMIT_PARAM, OFFSET_PARAM, DISTINCT_PARAM));
						}
					}
				});
				return new QueryDecorator(requireTag ? e[0].requireTag() : e[0].tag, qry.compose());
			}
			catch (Exception ex) {
				cause = ex;
			}
		}
		throw cannotParseEntryException(this, "query", cause);
	}
	
	//[partition(*).order(*)]*
	public Optional<Partition> parsePartition(QueryContext ctx) {
		if(value.matches(PARTITION_PATTERN)) {
			try {
				var cols = new ArrayList<Column>();
				var ords = new ArrayList<Order>();
				var e = this;
				do {
					switch (e.value) {
					case PARTITION_OPR: addAll(cols, parseAll(e.args, ctx, COLUMN)); break;
					case ORDER_PARAM: addAll(ords, parseAll(e.args, ctx, ORDER)); break;
					default: throw badEntryChainException(e, PARTITION_PATTERN);
					}
					e = e.next;
				} while(nonNull(e));
				return Optional.of(new Partition(
						cols.toArray(Column[]::new), 
						ords.toArray(Order[]::new)));
			}
			catch (Exception ex) {
				throw cannotParseEntryException(this, PARTITION_OPR, ex);
			}
		}
		return empty();
	}

	private EntryChainCursor chainResourceExpression(QueryContext ctx) {
		return chainResourceExpression(ctx, null);
	}
	
	private EntryChainCursor chainResourceExpression(QueryContext ctx, EntryChain[] outerArgs) {
		var r = lookupResource(ctx);
		if(r.isCriteria()) {
			var crArgs = r.entry.args;
			if(isEmpty(crArgs) && r.entry.isLast()) {
				crArgs = outerArgs;
				outerArgs = null; //flag outerArgs as consumed
			}
			r.col = nonNull(r.viewCrt)
					? buildCriteria(r.viewCrt, r, crArgs) //view criteria
					: r.col.filter(buildCriteria(r.colCrt, r, crArgs)); //column criteria
		}
		var e = r.entry.next;
		while(nonNull(e)) { //chain until [operator|comparator]
			var op = ctx.lookupOperator(e.value);
			if(op.isPresent()) {
				var fn = op.get();
				r.col = fn.invoke(e.parseArgs(ctx, r.col, fn.getSignature())); 
			}
			else {
				var oc = ctx.lookupComparator(e.value);
				if(oc.isPresent()) {
					var cp = oc.get();
					if(e.isLast() && isEmpty(e.args)) {
						e = new EntryChain(e.value, outerArgs, null, null); //chain outerArgs
						outerArgs = null; //flag outerArgs as consumed
					}
					r.col = cp.invoke(e.parseArgs(ctx, r.col, cp.getSignature()));
				}
				else {
					break;
				}
			}
			r.entry = e;
			e = e.next;
		}
		if(!isEmpty(outerArgs)) {
			var fn = outerArgs.length == 1 ? getDialect().eq() : getDialect().in();
			e = new EntryChain(fn.getComparator().toString(), false, outerArgs, null, null); 
			r.col = fn.invoke(e.parseArgs(ctx, r.col, fn.getSignature())); //no chain
		}
		return r;
	}
	
	private static <V> V buildCriteria(Builder<ViewDecorator, V> builder, EntryChainCursor cur, EntryChain... args) {
		return requireNonNull(builder.build(cur.vd, toStringArray(args)),
				()-> format("%s[criteria=%s]", nonNull(cur.col) ? "" : cur.vd.identity(), cur.entry.value));
	}
	
	private static <T> T build(String type, Builder<ViewDecorator, T> builder, ViewDecorator vd, EntryChain e) {
		return requireNonNull(builder.build(vd, toStringArray(e.args)), 
				()->  format("%s[%s=%s]", vd.identity(), type, e.value));
	}

	//operator|[query|view.]resource
	private EntryChainCursor lookupResource(QueryContext ctx) { //do not change priority
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

	private Optional<EntryChainCursor> lookupViewResource(QueryContext ctx, ViewDecorator vd, boolean prefixed) { //do not change priority
		return lookupDeclaredColumn(ctx, vd, prefixed)//view.count only
				.or(()-> lookupViewCriteria(vd))
				.or(()-> lookupRegistredColumn(ctx, vd))
				.or(()-> lookupViewOperation(ctx, vd, prefixed));
	}
	
	//operator|[view.]operator
	private Optional<EntryChainCursor> lookupViewOperation(QueryContext ctx, ViewDecorator vd, boolean prefixed) {
		return ctx.lookupOperator(value).filter(prefixed ? op -> op.getName().equals("count") : o-> true).map(fn-> {
			var col = isEmpty(args) && fn.getName().equals("count") ? allColumns(vd.view()) : null;
			return fn.invoke(parseArgs(ctx, col, fn.getSignature()));
		}).map(oc-> new EntryChainCursor(this, vd, oc));
	}

	//query.column|column
	private Optional<EntryChainCursor> lookupDeclaredColumn(QueryContext ctx, ViewDecorator vd, boolean prefixed) {
		if(prefixed) {
			return vd instanceof QueryDecorator qd
					? qd.column(value).map(col-> new EntryChainCursor(requireNoArgs(), qd, col)) 
					: empty();
		}
		return ctx.lookupDeclaredColumn(value)
				.map(c-> new EntryChainCursor(requireNoArgs(), null, c));
	}
	
	//view.criteria
	private Optional<EntryChainCursor> lookupViewCriteria(ViewDecorator vd) {
		return ofNullable(vd.criteriaBuilder(value))
				.map(cr-> new EntryChainCursor(requireNoArgs(), vd, cr)); //view criteria takes no args
	}
	
	//view.column[.criteria]
	private Optional<EntryChainCursor> lookupRegistredColumn(QueryContext ctx, ViewDecorator vd) {
		return ctx.lookupRegisteredColumn(value).map(cd->{
			if(hasNext()) {
				var cr = cd.criteriaBuilder(next.value);
				if(nonNull(cr)) {
					return new EntryChainCursor(next, vd, vd.column(cd), cr); //column criteria takes args
				}
			}
			return new EntryChainCursor(this, vd, vd.column(cd, toStringArray(args)));
		});
	}

	Object[] parseArgs(QueryContext ctx, DBObject col, Signature ps) {
		int inc = isNull(col) ? 0 : 1;
		var arr = new Object[isNull(args) ? inc : args.length + inc];
		if(nonNull(col)) {
			arr[0] = col;
		}
		try {
			return ps.buildArgs(isNull(args) ? inc : args.length + inc, (i,types)-> {
				if(i<inc) {
					arr[0] = col;
				}
				var o = args[i-inc];
				return isNull(o.value) || o.text
						? o.requireNoArgs().value 
						: parse(o, ctx, types);
			});
		}
		catch (Exception e) {
			throw badEntryArgsException(this, ps, e);
		}
	}
	
	String requireTag() {
		if(nonNull(tag)) {
			return tag;
		}
		throw badEntryTagException(this);
	}

	EntryChain requireNoArgs() {
		if(isNull(args)) {
			return this;
		}
		throw badEntryArgsException(this);
	}

	EntryChain requireNoNext() {
		if(isLast()) {
			return this;
		}
		throw badEntryChainException(this);
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
		if(hasNext()) {
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
		return null;
	}
	
	static String enumPattern(Class<? extends Enum<?>> c) {
		return Stream.of(c.getEnumConstants())
				.map(Enum::name)
				.map(String::toLowerCase)
				.collect(joining("|"));
	}

	static EntrySyntaxException badEntryTagException(EntryChain e) {
		return new EntrySyntaxException(format("incorrect syntax: expected %s[:tag]", e.toString()));
	}

	static EntrySyntaxException badEntryChainException(EntryChain e) {
		return new EntrySyntaxException(format("incorrect syntax: unexpected entry chain %s[.%s]", e.value, e.next.value));
	}
	
	static EntrySyntaxException badEntryChainException(EntryChain e, String expect) {
		var prev = nonNull(e.prev) ? e.prev.value+"." : "";
		return new EntrySyntaxException(format("incorrect syntax: expected %s[%s], but was %s", prev, expect, e.value));
	}

	static EntrySyntaxException badEntryArgsException(EntryChain e) {
		return new EntrySyntaxException(format("incorrect syntax: unexpected entry args %s[(%s)]", e.value, toStringArray(e.args)));
	}
	
	static EntrySyntaxException badEntryArgsException(EntryChain e, Signature param, Exception ex) {
		var prv = nonNull(e.prev) ? e.prev.value+"." : "";
		return new EntrySyntaxException(format("incorrect syntax: expected %s%s, but was %s%s[(%s)]", e.value, param, prv, e.value, toStringArray(e.args)), ex);
	}
	
	static EntryParseException cannotParseEntryException(EntryChain entry, String type, Exception ex) {
		return cannotParseEntryException(entry, type, null, ex);
	}
	
	static EntryParseException cannotParseEntryException(EntryChain entry, String type, EntryChain[] outerArgs, Exception ex) {
		var eq = isNull(outerArgs) ? "" : "=" + stream(outerArgs).map(Object::toString).collect(joining(","));
		return new EntryParseException(format("cannot parse '%s' : %s", type, entry.toString()+eq), ex);
	}
	
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	final class EntryChainCursor {
		
		private final ViewDecorator vd;
		private final Builder<ViewDecorator, Criteria> viewCrt;
		private final Builder<ViewDecorator, Predicate> colCrt;
		private EntryChain entry;
		private Column col;

		public EntryChainCursor(EntryChain entry, ViewDecorator vd, Column col) {
			this(vd, null, null, entry, col); //[view.]column
		}

		public EntryChainCursor(EntryChain entry, ViewDecorator vd, Column col, Builder<ViewDecorator, Predicate> colCrt) {
			this(vd, null, colCrt, entry, col); //[view.]colum.criteria
		}

		public EntryChainCursor(EntryChain entry, ViewDecorator vd, Builder<ViewDecorator, Criteria> viewCrt) {
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