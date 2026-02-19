package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static org.usf.jquery.core.DBColumn.allColumns;
import static org.usf.jquery.core.ViewJoin.join;
import static org.usf.jquery.web.Parameters.COLUMN_PARAM;
import static org.usf.jquery.web.Parameters.DISTINCT_PARAM;
import static org.usf.jquery.web.Parameters.FILTER_OPR;
import static org.usf.jquery.web.Parameters.JOIN_PARAM;
import static org.usf.jquery.web.Parameters.LIMIT_PARAM;
import static org.usf.jquery.web.Parameters.OFFSET_PARAM;
import static org.usf.jquery.web.Parameters.ORDER_PARAM;
import static org.usf.jquery.web.Parameters.PARTITION_OPR;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.usf.jquery.core.Comparator;
import org.usf.jquery.core.ComparisonExpression;
import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.JoinType;
import org.usf.jquery.core.JoinsClause;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.Operator;
import org.usf.jquery.core.OrderType;
import org.usf.jquery.core.ParameterSet;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.QueryComposer;
import org.usf.jquery.core.SingleQueryColumn;
import org.usf.jquery.core.TypedComparator;
import org.usf.jquery.core.TypedOperator;
import org.usf.jquery.web.EntryParseException;
import org.usf.jquery.web.EntrySyntaxException;
import org.usf.jquery.web.NoSuchResourceException;

import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class EntryEvaluators {
	
	public static DBView evaluateView(Entry entry, RequestContext ctx) {
		try {
			var itr = entry.iterator();
			var view = evalView(itr, ctx);
			assertLastEntry(itr, true);
			ctx.declareView(requireTag(itr.get()), view);
			return view.getView();
		}
		catch (Exception e) {
			throw new EntryParseException("invalid view expression : " + entry.getValue(), e);
		}
	}

	public static DBColumn evaluateColumn(Entry entry, RequestContext ctx) {
		try {
			var itr = entry.iterator();
			var col = evaluateColumn(itr, ctx);
			assertLastEntry(itr, false);
			return col;
		}
		catch (Exception e) {
			throw new EntryParseException("invalid column expression : " + entry.getValue(), e);
		}
	}

	public static NamedColumn evaluateNamedColumn(Entry entry, RequestContext ctx) {
		try {
			var itr = entry.iterator();
			var col = evaluateColumn(itr, ctx);
			assertLastEntry(itr, true);
			return col instanceof NamedColumn nc ? nc : col.as(requireTag(itr.get()));
		}
		catch (Exception e) {
			throw new EntryParseException("invalid named column expression : " + entry.getValue(), e);
		}
	}
	
	public static DBFilter evaluateFilter(Entry entry, RequestContext ctx, Entry... outerArgs) {
		try {
			var itr = entry.iterator();
			var col = evaluateColumn(itr, ctx, outerArgs);
			assertLastEntry(itr, false);
			if(col instanceof DBFilter filter) { 
				return filter; 
			} 
			throw new EntryParseException(entry.getValue() + " does not resolve to a filter");
		}
		catch (Exception e) {
			throw new EntryParseException("invalid filter expression : " + entry.getValue(), e);
		}
	}
	
	public static DBOrder evaluateOrder(Entry entry, RequestContext ctx) {
		try {
			var itr = entry.iterator();
			var ord = lookupDeclaredResource(itr, DBOrder.class, ctx, null).orElseGet(()-> evalOrder(itr.reset(), ctx));
			assertLastEntry(itr, false);
			return ord;
		}
		catch (Exception e) {
			throw new EntryParseException("invalid order expression : " + entry.getValue(), e);
		}
	}

	public static JoinsClause evaluateJoin(Entry entry, RequestContext ctx) {
		try {
			var itr = entry.iterator();
			var join = lookupDeclaredResource(itr, JoinsClause.class, ctx, null).orElseGet(()-> evalJoin(itr.reset(), ctx));
			assertLastEntry(itr, false);
			return join;
		}
		catch (Exception e) {
			throw new EntryParseException("invalid join expression : " + entry.getValue(), e);
		}
	}
	
	public static Partition evaluatePartition(Entry entry, RequestContext ctx) {
		try {
			var itr = entry.iterator();
			var prt = lookupDeclaredResource(itr, Partition.class, ctx, null).orElseGet(()-> evaluatePartition(itr.reset(), ctx));
			assertLastEntry(itr, false);
			return prt;
		}
		catch (Exception e) {
			throw new EntryParseException("invalid partition expression : " + entry.getValue(), e);
		}
	}

	public static SingleQueryColumn evaluateQueryColumn(Entry entry, RequestContext ctx) {
		try {
			var itr = entry.iterator();
			var view = evalView(itr, ctx);
			if(view instanceof QueryResource query) {
				assertLastEntry(itr, false);
				return query.getQuery().asColumn();
			}
			throw new EntryParseException("");
		}
		catch (Exception e) {
			throw new EntryParseException("invalid single query column expression : " + entry.getValue(), e);
		}
	}
	
	static ViewResource evalView(EntryIterator itr, RequestContext ctx) {
		var entry = itr.get();
		var view = ctx.lookupView(true, entry.getValue(), entry.getArgs());
		if(view.isPresent()) {
			return itr.hasNext()  //view.column().filter()..
					? evalQuery(itr.advance(), view.get(), ctx.subContext(view.get()))
					: view.get();
		}
		throw new NoSuchElementException("view resource not found : " + entry.getValue());
	}
	
	static QueryResource evalQuery(EntryIterator itr, ViewResource view, RequestContext ctx) {
		if(itr.get().getValue().equals(COLUMN_PARAM)) {
			var query = new QueryComposer();
			var subCtx = ctx.subContext(view);
			do {
				var entry = itr.get();
				switch (entry.getValue()) {
				case COLUMN_PARAM: query.columns(subCtx.resolveAll(entry.getArgs(), NamedColumn.class)); break;
				case FILTER_OPR: query.filters(subCtx.resolveAll(entry.getArgs(), DBFilter.class)); break;
				case ORDER_PARAM: query.orders(subCtx.resolveAll(entry.getArgs(), DBOrder.class)); break;
				case JOIN_PARAM: query.joins(subCtx.resolveAll(entry.getArgs(), JoinsClause.class)); break;
				case LIMIT_PARAM: query.limit(evalSingleArgsValue(entry, Integer.class, subCtx)); break;
				case OFFSET_PARAM: query.offset(evalSingleArgsValue(entry, Integer.class, subCtx)); break;
				case DISTINCT_PARAM: query.distinct(evalSingleArgsValue(entry, Boolean.class, subCtx)); break;
				default: throw new EntryParseException("invalid query operator : " + entry.getValue());
				}
			} while(itr.tryAdvance());
			return new QueryResource(query.compose());	
		}
		throw new EntryParseException("invalid query operator : " + itr.get().getValue());
	}
	
	static <T> T evalSingleArgsValue(Entry entry, Class<T> type, RequestContext ctx) {
		if(entry.hasArgs() && entry.getArgs().length == 1) {
			var arg = entry.getArgs()[0];
			assertLastEntry(arg.iterator(), false);
			return ctx.evalValue(arg.getValue(), type);
		}
		throw new EntryParseException("entry " + entry.getValue() + " must have exactly 1 argument");
	}
	
	static DBOrder evalOrder(EntryIterator itr, RequestContext ctx) {
		var col = evaluateColumn(itr, ctx);
		if(itr.hasNext()) {
			var entry = itr.next();
			if(entry.getValue().matches("asc|desc")) {
				if(entry.hasArgs()) {
					throw new EntryParseException("order operator cannot have arguments");
				}
				return col.order(OrderType.valueOf(entry.getValue().toUpperCase()));
			}
			else {
				throw new EntryParseException("invalid order type : " + entry.getValue());
			}
		}
		return col.order();
	}
	
	static JoinsClause evalJoin(EntryIterator itr, RequestContext ctx) {
		Predicate<String> isJoin = s-> s.matches("(inner|left|right|full|cross)Join");
		var entry = itr.get();
		if(!isJoin.test(entry.getValue())) {
			var view = evalView(itr, ctx);
			if(entry.hasNext()) {
				entry = itr.next();
				if(!isJoin.test(entry.getValue())) {
					throw new EntryParseException("invalid join operator : " + entry.getValue());
				}
				ctx = ctx.withView(view);
			}
			else {
				throw new EntryParseException("join operator is missing");
			}
		}
		if(entry.hasArgs() && entry.getArgs().length == 2) {
			assertLastEntry(itr, false);
			try {
				var type = JoinType.valueOf(entry.getValue().substring(0, entry.getValue().length()-4).toUpperCase());
				itr = entry.getArgs()[0].iterator();
				var view = evalView(itr, ctx).getView();
				assertLastEntry(itr, false);
				var filter = evaluateFilter(entry.getArgs()[1], ctx);
				return JoinsClause.of(join(type, view, filter));
			}
			catch (Exception e) {
				throw new EntryParseException("cannot parse join arguments ", e);
			}
		}
		throw new EntryParseException(entry.getValue() + " operator must have exactly 2 arguments");
	}
	
	static Partition evaluatePartition(EntryIterator itr, RequestContext ctx) {
		var cols = new ArrayList<DBColumn>();
		var ords = new ArrayList<DBOrder>();
		do {
			var entry = itr.get();
			switch (entry.getValue()) {
			case PARTITION_OPR: addAll(cols, ctx.resolveAll(entry.getArgs(), DBColumn.class)); break;
			case ORDER_PARAM: addAll(ords, ctx.resolveAll(entry.getArgs(), DBOrder.class)); break;
			default: throw new IllegalArgumentException("invalid partition operator : " + entry.getValue());
			}
		} while(itr.tryAdvance());
		return new Partition(cols.toArray(DBColumn[]::new), ords.toArray(DBOrder[]::new));
	}
	
	static DBColumn evaluateColumn(EntryIterator itr, RequestContext ctx, Entry... outArgs) {
		var res = lookupDeclaredResource(itr, DBColumn.class, ctx, (v, e)->{ 
			if("count".equals(e.getValue())) {
				return Optional.of(invokeOperator(e.hasArgs() ? null : allColumns(v.getView()), e.getValue(), e.getArgs(), ctx));
			}
			return empty();
		}); //column or criteria resource
		var col = res.isEmpty() 
				? chainResource(itr, null, ctx, outArgs)
				: chainResource(itr.advance(), res.get(), ctx, outArgs);
		if(nonNull(col)) {
			return col;
		}
		throw new NoSuchResourceException("no such column : " + itr.get().getValue());
	}

	static <T> Optional<T> lookupDeclaredResource(EntryIterator itr, Class<T> type, RequestContext ctx, BiFunction<ViewResource, Entry, Optional<T>> orElseGet) {
		if(itr.hasNext()) { //may be prefixed resource
			var vRes = ctx.lookupView(false, itr.get().getValue()); //parameterized view resource is not supported, must be declared as view resource in context
			if(vRes.isPresent()) {
				var view = vRes.get();
				var res = lookupViewResource(view, type, itr.next(), ctx);
				if(res.isEmpty() && nonNull(orElseGet)) {
					res = orElseGet.apply(view, itr.get());
				}
				if(res.isPresent()) {
					return res;
				}
				itr.reset(); // TODO check this
			}
		}
		var res = lookupViewResource(ctx.getDefaultView(), type, itr.get(), ctx);
		return res.isPresent() || isNull(orElseGet) ? res : orElseGet.apply(ctx.getDefaultView(), itr.get());
	}
	
	//res=3 or res.fun1.eq=3 or res.in=1,2,3 or res.express=33 or res.express(33).and(..)
	
	static DBColumn chainResource(EntryIterator itr, DBColumn res, RequestContext ctx, Entry... outArgs) {
		var col = res;
		do {
			var entry = itr.get();
			var tmp = invokeOperator(col, entry.getValue(), entry.getArgs(), ctx);
			if(isNull(tmp)) {
				var args = itr.hasNext() || nonNull(entry.getArgs()) ? entry.getArgs() : outArgs; //do not use entry.getArgs()
				tmp = invokeComparator(col, entry.getValue(), args, ctx);
				if(isNull(tmp) && nonNull(col)) {
					tmp = ctx.lookupSchemaResource(entry.getValue(), ComparisonExpression.class, args)
							.map(res::filter).orElse(null);
				}
			}
			if(nonNull(tmp)) {
				itr.advance();
				col = tmp;
			}
			else {
				break;
			}
		} while(itr.tryAdvance());
		return col;
	}

	static DBColumn invokeOperator(Object col, String name, Entry[] args, RequestContext ctx) {
		var opr = ctx.lookupSchemaResource(name, TypedOperator.class, args) //check declared operator first, then static resource
				.orElseGet(()-> lookup(Operator.class, TypedOperator.class, name));
		return nonNull(opr) 
				? opr.operation(resolveArgs(opr.getParameterSet(), col, args, ctx))
				: null;
	}
	
	static DBFilter invokeComparator(Object col, String name, Entry[] args, RequestContext ctx) {
		var cmp = ctx.lookupSchemaResource(name, TypedComparator.class, args) //check declared comparator first, then static resource
				.orElseGet(()-> lookup(Comparator.class, TypedComparator.class, name));
		return nonNull(cmp) 
			? cmp.filter(resolveArgs(cmp.getParameterSet(), col, args, ctx))
			: null;
	}
	
	static <T> T lookup(Class<?> clazz, Class<T> type, String name) {
		try {
			var mth = clazz.getMethod(name); //no parameter
			if(nonNull(mth)) {
				var mod = mth.getModifiers();
				if(mth.getReturnType() == type && mth.getParameterCount()==0 && isPublic(mod) && isStatic(mod)) {
					return type.cast(mth.invoke(null));
				}
			}
		} catch (Exception e) {/* do not throw exception */}
		return null;
	}
	
	static Object[] resolveArgs(ParameterSet ps, Object res, Entry[] args, RequestContext ctx){
		var arr = new Object[args.length + (nonNull(res) ? 1 : 0)];
		ps.eachParameter(args.length, (p,i)-> {
			if(i==0 && nonNull(res)) {
				if(p.accept(i, arr)) {
					arr[i] = res;
				}
				else {
					throw new IllegalArgumentException(); //TODO
				}
			}
			else {
				arr[i] = ctx.resolve(args[i], p.getTypes());
			}
		});
		return arr;
	}

	static <T> Optional<T> lookupViewResource(ViewResource view, Class<T> type, Entry entry, RequestContext ctx) { //pretty syntax
		return ctx.lookupViewResource(view, entry.getValue(), type, entry.getArgs());
	}

	static void assertLastEntry(EntryIterator entry, boolean tagAllowed) {
		if(entry.hasNext()) {
			throw new EntrySyntaxException("unexpected entry : " + entry.next().getValue());
		}
		if(!tagAllowed && entry.get().hasTag()) {
			throw new EntrySyntaxException("unexpected tag : " + entry.get().getTag());
		}
	}
	
	static String requireTag(Entry entry) {
		if(entry.hasTag()) {
			return entry.getTag();
		}
		throw new EntrySyntaxException("entry must have a tag : " + entry.getValue());
	}
}