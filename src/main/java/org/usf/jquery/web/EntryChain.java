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
import static org.usf.jquery.core.BadArgumentException.badArgumentsFormat;
import static org.usf.jquery.core.Comparator.eq;
import static org.usf.jquery.core.Comparator.in;
import static org.usf.jquery.core.Comparator.lookupComparator;
import static org.usf.jquery.core.DBColumn.allColumns;
import static org.usf.jquery.core.Operator.lookupOperator;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Utils.joinArray;
import static org.usf.jquery.core.Validation.requireNArgs;
import static org.usf.jquery.web.ArgumentParsers.parse;
import static org.usf.jquery.web.ArgumentParsers.parseAll;
import static org.usf.jquery.web.NoSuchResourceException.noSuchResourceException;
import static org.usf.jquery.web.Parameters.COLUMN;
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
	private final EntryChain[] args; //modifiable
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
	public DBColumn evalColumn(RequestContext td, boolean requireTag) {
		try {
			var r = chainResourceExpression(td);
			if(r.entry.isLast()) {
				if(nonNull(r.entry.tag)) {
					return r.col.as(r.entry.tag);
				}
				if(!requireTag || r.col instanceof NamedColumn) {
					return r.col;
				}
				throw expectedEntryTagException(r.entry);
			}
			throw badEntrySyntaxException(r.entry.next.value, "expression");
		} catch (Exception e) {
			throw cannotParseEntryException(this, COLUMN, e);
		}
	}
	
	//[view.]column[.operator]*[.order]
	public DBOrder evalOrder(RequestContext td) {
		try {
			var r = chainResourceExpression(td);
			if(r.entry.isLast()) { // default order
				return r.col.order();
			}
			var nxt = r.entry.next;
			if(nxt.value.matches(ORDER_PATTERN)) { //check args & next only if order exists
				var ord = nxt.requireNoArgs().requireNoNext().value.toUpperCase();
				return r.col.order(OrderType.valueOf(ord));
			}
			throw badEntrySyntaxException(nxt.value, ORDER_PATTERN);
		} catch (Exception e) {
			throw cannotParseEntryException(this, ORDER, e);
		}
	}

	public DBFilter evalFilter(RequestContext td) {
		return evalFilter(td, null); //null ==> inner filter
	}

	//[view.]criteria | [view.]column.criteria | [view.]column[.operator]*[.comparator][.and|or(comparator)]*
	public DBFilter evalFilter(RequestContext context, EntryChain[] outerArgs) {
		try {
			var rsc = chainResourceExpression(context, outerArgs);
			if(rsc.entry.isLast()) {
				if(rsc.col instanceof DBFilter f) {
					return f;
				}
				throw new IllegalArgumentException(this + " is not a filter");
			}
			throw badEntrySyntaxException(rsc.entry.next.value, "expression");
		} catch (Exception e) {
			throw cannotParseEntryException(this, FILTER, e);
		}
	}
	
	//[view.]partition | partition(*).order(*) | order(*).partition(*)
	public Partition evalPartition(RequestContext context) {
		if(hasNext()) {
			var res = context.lookupRegisteredView(value)
					.map(vd-> requireNoArgs().next.evalPartition(vd));
			if(res.isPresent()) {
				return res.get();
			}
		}
		try {
			return evalPartition(context.getDefaultView());
		} catch (NoSuchResourceException e) {
			return parsePartition(context);
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
	public ViewDecorator evalView(RequestContext vd) {
		return vd.lookupRegisteredView(value) //check args & next only if view exists
				.<ViewDecorator>map(v-> new ViewDecoratorWrapper(v, requireNoArgs().requireNoNext().requireTag()))
				.orElseGet(()-> parseQuery(vd, true));
	}
	
	public SingleQueryColumn evalQueryColumn(RequestContext td) {		
		return parseQuery(td, false).getQuery().asColumn();
	}
	
	//[view.]join
	public ViewJoin[] evalJoin(RequestContext context) {
		return hasNext()
				? context.lookupRegisteredView(value)
						.map(vd-> requireNoArgs().next.evalJoin(vd))
						.orElseThrow(()-> noSuchResourceException(JOIN, value))
				: evalJoin(context.getDefaultView());
	}
	
	private ViewJoin[] evalJoin(ViewDecorator vd) { 
		var join = vd.join(value);
		if(nonNull(join)) {
			requireNoArgs().requireNoNext(); 
			return requireNonNull(join.build(), vd.identity() + "." + value);
		}
		throw noSuchResourceException(JOIN, value, vd.identity());
	}

	public ViewDecorator parseQuery(RequestContext td) {
		return parseQuery(td, false);
	}
	
	//select[.filter|order|offset|fetch]*
	QueryDecorator parseQuery(RequestContext context, boolean requireTag) { //sub context
		Exception cause = null;
		if(SELECT.equals(value)) {
			var e =	this;
			try {
				var q = new QueryComposer().columns(parseAll(args, context, JQueryType.NAMED_COLUMN));
				while(e.hasNext()) {
					e = e.next;
					switch(e.value) {//column not allowed 
					case FILTER: q.filters(parseAll(e.args, context, JQueryType.FILTER)); break;
					case ORDER: q.orders(parseAll(e.args, context, JQueryType.ORDER)); break;
					case JOIN: q.joins(parseAll(e.args, context, JQueryType.JOIN)); break;
					case LIMIT: q.limit(parseInt(requireNArgs(1, e.args, ()-> LIMIT)[0].value)); break;
					case OFFSET: q.offset(parseInt(requireNArgs(1, e.args, ()-> OFFSET)[0].value)); break;
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
	public Partition parsePartition(RequestContext context) {
		Exception cause = null;
		if(value.matches(PARTITION_PATTERN)) {
			try {
				var cols = new ArrayList<DBColumn>();
				var ords = new ArrayList<DBOrder>();
				var e = this;
				do {
					switch (e.value) {
					case PARTITION: addAll(cols, parseAll(e.args, context, JQueryType.COLUMN)); break;
					case ORDER: addAll(ords, parseAll(e.args, context, JQueryType.ORDER)); break;
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
	

	private EntyChainCursor chainResourceExpression(RequestContext context) {
		return chainResourceExpression(context, null);
	}
	
	private EntyChainCursor chainResourceExpression(RequestContext context, EntryChain[] outerArgs) {
		var r = lookupResource(context);
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
			var fn = outerArgs.length == 1 ? eq() : in();
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
				.orElseThrow(()-> noSuchResourceException(value));
	}

	private Optional<EntyChainCursor> lookupViewResource(RequestContext context, ViewDecorator vd, boolean prefixed) { //do not change priority
		return lookupViewOperation(context, vd, prefixed) //view.count only
				.or(()-> lookupDeclaredColumn(context, vd, prefixed))
				.or(()-> lookupViewCriteria(vd))
				.or(()-> lookupRegistredColumn(context, vd));
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
		return context.lookupRegisteredColumn(value).map(cd->{
			if(hasNext()) {
				var cr = cd.criteria(next.value);
				if(nonNull(cr)) {
					return new EntyChainCursor(requireNoArgs().next, vd, vd.column(cd), cr);
				}
			}
			return new EntyChainCursor(requireNoArgs(), vd, vd.column(cd));
		});
	}

	
	
	private Object[] toArgs(RequestContext context, DBObject col, ParameterSet ps) {
		int inc = isNull(col) ? 0 : 1;
		var arr = new Object[isNull(args) ? inc : args.length + inc];
		if(nonNull(col)) {
			arr[0] = col;
		}
		try {
			ps.eachParameter(arr.length, (p,i)-> {
				if(i>=inc) { //arg0 already parsed
					var e = args[i-inc];
					arr[i] = isNull(e.value) || e.text
							? e.requireNoArgs().value 
							: parse(e, context, p.types(arr));
				}
			});
		}
		catch (Exception e) {
			throw new EntrySyntaxException("bad entry arguments: " +
					badArgumentsFormat(value, nonNull(args) ? args : null), e);
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
	
	private static EntryParseException cannotParseEntryException(EntryChain entry, String type, Exception ex) {
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