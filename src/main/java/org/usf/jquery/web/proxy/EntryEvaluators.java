package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Collections.addAll;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.usf.jquery.core.DBColumn.allColumns;
import static org.usf.jquery.core.JoinType.CROSS;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.ViewJoin.join;
import static org.usf.jquery.web.Parameters.COLUMN_PARAM;
import static org.usf.jquery.web.Parameters.DISTINCT_PARAM;
import static org.usf.jquery.web.Parameters.FILTER_OPR;
import static org.usf.jquery.web.Parameters.JOIN_PARAM;
import static org.usf.jquery.web.Parameters.LIMIT_PARAM;
import static org.usf.jquery.web.Parameters.OFFSET_PARAM;
import static org.usf.jquery.web.Parameters.ORDER_PARAM;
import static org.usf.jquery.web.proxy.EntryParser.parseEntry;
import static org.usf.jquery.web.proxy.SchemaProxy.createSchema;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

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
import org.usf.jquery.web.spec.SchemaSample;

import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class EntryEvaluators {
	
	public static DBView evaluateView(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var view = evalView(itr, ctx, false);
		if(nonNull(view)) {
			assertLastEntry(itr, true);
			ctx.declareView(requireTag(itr.get()), view);
			return view.getView();
		}
		throw new NoSuchResourceException("no such view : " + itr.get().getValue());
	}

	public static DBColumn evaluateColumn(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var col = evalColumn(itr, ctx);
		if(nonNull(col)) {
			assertLastEntry(itr, false);
			return col;
		}
		throw new NoSuchResourceException("no such column : " + itr.get().getValue());
	}

	public static NamedColumn evaluateNamedColumn(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var col = evalColumn(itr, ctx);
		if(nonNull(col)) {
			assertLastEntry(itr, true);
			return col instanceof NamedColumn nc ? nc : col.as(requireTag(itr.get()));
		}
		throw new NoSuchResourceException("no such named column : " + itr.get().getValue());
	}
	
	public static DBFilter evaluateFilter(Entry entry, RequestContext ctx, Entry... outerArgs) {
		var itr = entry.iterator();
		var col = evalColumn(itr, ctx, outerArgs);
		if(nonNull(col)) {
			if(col instanceof DBFilter filter) { 
				assertLastEntry(itr, false);
				return filter; 
			}
			throw new EntryParseException(entry.getValue() + " does not resolve to a filter");
		}
		throw new NoSuchResourceException("no such column or filter : " + itr.get().getValue());
	}
	
	public static DBOrder evaluateOrder(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var ord = lookupResource(itr, DBOrder.class, ctx, (v, e)-> evalOrder(e, ctx));
		if(nonNull(ord)) {
			assertLastEntry(itr, false);
			return ord;
		}
		throw new NoSuchResourceException("no such order : " + itr.get().getValue());
	}

	public static JoinsClause evaluateJoin(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var join = lookupResource(itr, JoinsClause.class, ctx, (v, e)-> evalJoin(e, ctx.withView(v)));
		if(nonNull(join)) {
			assertLastEntry(itr, false);
			return join;
		}
		throw new NoSuchResourceException("no such join : " + itr.get().getValue());
	}
	
	public static Partition evaluatePartition(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var prt = lookupResource(itr, Partition.class, ctx, (v,e)-> evalPartition(e, ctx.withView(v)));
		if(nonNull(prt)) {
			assertLastEntry(itr, false);
			return prt;
		}
		throw new NoSuchResourceException("no such partition : " + itr.get().getValue());
	}

	public static SingleQueryColumn evaluateQueryColumn(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var col = lookupResource(itr, SingleQueryColumn.class, ctx, (v, e)-> {
			var view = evalView(itr, ctx, true);
			if(view instanceof QueryResource query && query.getQuery().getColumns().length == 1) {
				return query.getQuery().asColumn();
			}
			if(nonNull(view)) {
				throw new EntryParseException("view " + itr.get().getValue() + " cannot be used as query column");
			}
			return null;
		});
		if(nonNull(col)) {
			assertLastEntry(itr, false);
			return col;
		}
		throw new NoSuchResourceException("no such query column : " + itr.get().getValue());
	}

	static ViewResource evalView(EntryIterator itr, RequestContext ctx, boolean allowAnonymous) {
		var entry = requireNonNull(itr.peekNext(), "no entry to evaluate as view resource");
		if(allowAnonymous && COLUMN_PARAM.equals(entry.getValue())) { //check query resource before view resource
			return evalQuery(itr, ctx.subContext(ctx.getDefaultView()));
		} //parameterized view considered as anonymous view resource, not supported for direct lookup
		var view = ctx.lookupView(allowAnonymous, entry.getValue(), entry.getArgs());
		if(view.isPresent()) {
			return itr.advance().hasNext() && allowAnonymous && COLUMN_PARAM.equals(itr.peekNext().getValue()) //view.column().filter()..
					? evalQuery(itr, ctx.subContext(view.get())) : view.get();
		}
		return null;
	}

	static DBOrder evalOrder(EntryIterator itr, RequestContext ctx) {
		var col = evalColumn(itr, ctx);
		if(nonNull(col)) {
			if(itr.hasNext()) {
				var entry = itr.peekNext();
				if(nonNull(entry.getValue()) && entry.getValue().matches("asc|desc")) {
					if(entry.hasArgs()) {
						throw new EntryParseException("order operator cannot have arguments");
					}
					itr.advance(); //consume order type entry
					return col.order(OrderType.valueOf(entry.getValue().toUpperCase()));
				}
				else {
					throw new EntryParseException("invalid order type : " + entry.getValue());
				}
			}
			return col.order();
		}
		return null;
	}

	static DBColumn evalColumn(EntryIterator itr, RequestContext ctx, Entry... outArgs) {
		var res = lookupResource(itr, DBColumn.class, ctx, (v, e)->{
			var entry = e.get();
			if("count".equals(entry.getValue())) {
				return invokeOperator(entry.hasArgs() ? null : allColumns(v.getView()), entry.getValue(), entry.getArgs(), ctx);
			}
			return null;
		}); //column or criteria resource
		return chainResource(itr, res, ctx, outArgs);
	}

	static QueryResource evalQuery(EntryIterator itr, RequestContext ctx) {
		var query = new QueryComposer();
		try {
			var matched = true;
			while(itr.hasNext()) {
				var entry = itr.peekNext();
				switch (entry.getValue()) {
				case COLUMN_PARAM-> query.columns(ctx.resolveAll(entry.getArgs(), NamedColumn.class));
				case FILTER_OPR-> query.filters(ctx.resolveAll(entry.getArgs(), DBFilter.class)); 
				case ORDER_PARAM-> query.orders(ctx.resolveAll(entry.getArgs(), DBOrder.class));
				case JOIN_PARAM-> query.joins2(ctx.resolveAll(entry.getArgs(), JoinsClause.class));
				case LIMIT_PARAM-> query.limit(resolveSingleArgValue(entry, Integer.class, ctx));
				case OFFSET_PARAM-> query.offset(resolveSingleArgValue(entry, Integer.class, ctx));
				case DISTINCT_PARAM-> query.distinct(resolveSingleArgValue(entry, Boolean.class, ctx));
				default-> matched = false;
				}
				if(!matched) {
					break;
				}
				itr.advance();
			}
		}
		catch (Exception e) {
			throw new EntryParseException("cannot parse query arguments ", e);
		}
		if(query.getColumns().isEmpty()) {
			throw new EntryParseException("query must have at least one column");
		}
		return new QueryResource(query.compose());	
	}
	
	static Partition evalPartition(EntryIterator itr, RequestContext ctx) {
		var cols = new ArrayList<DBColumn>();
		var ords = new ArrayList<DBOrder>();
		try {
			var matched = true;
			while(itr.hasNext()) {
				var entry = itr.peekNext();
				switch (entry.getValue()) {
				case COLUMN_PARAM-> addAll(cols, ctx.resolveAll(entry.getArgs(), DBColumn.class));
				case ORDER_PARAM-> addAll(ords, ctx.resolveAll(entry.getArgs(), DBOrder.class));
				default-> matched = false;
				}
				if(!matched) {
					break;
				}
				itr.advance();
			}
		}
		catch (Exception e) {
			throw new EntryParseException("cannot parse query arguments ", e);
		} //optional partition args ?
		return new Partition(cols.toArray(DBColumn[]::new), ords.toArray(DBOrder[]::new));
	}

	static JoinsClause evalJoin(EntryIterator itr, RequestContext ctx) {
		var entry = requireNonNull(itr.peekNext(), "no entry to evaluate as join resource");
		if(nonNull(entry.getValue()) && entry.getValue().matches("(inner|left|right|full|cross)Join")) {
			entry = itr.next();
			var type = JoinType.valueOf(entry.getValue().substring(0, entry.getValue().length()-4).toUpperCase());
			try {
				var jView = resolveSingleArg(entry, arg-> evalView(arg, ctx, false));
				if(isNull(jView)) {
					throw new NoSuchResourceException("no such view : " + entry.getValue());
				}
				DBFilter[] filters = null;
				if(itr.hasNext() && FILTER_OPR.equals(itr.peekNext().getValue())) {
					filters = ctx.resolveAll(itr.next().getArgs(), DBFilter.class);						
				}
				if(type != CROSS && isEmpty(filters)) {
					throw new IllegalArgumentException("join type " + type + " requires at least one filter");
				}
				return JoinsClause.of(join(type, jView.getView(), filters));
			}
			catch (Exception e) {
				throw new EntryParseException("cannot parse join arguments ", e);
			}
		}
		return null;
	}
	
	static <T> T lookupResource(EntryIterator itr, Class<T> type, RequestContext ctx, BiFunction<ViewResource, EntryIterator, T> anonymousResolver) {
		var entry = requireNonNull(itr.peekNext(), "no entry to evaluate as resource");
		if(entry.hasNext()) {
			var vRes = ctx.lookupView(false, entry.getValue()); //parameterized view resource is not supported, must be declared as view resource in context
			if(vRes.isPresent()) {
				var view = vRes.get();
				var res = lookupViewResource(view, type, itr.next(), ctx).orElseGet(()->
					nonNull(anonymousResolver) ? anonymousResolver.apply(view, itr) : null);
				if(nonNull(res)) {
					return res;
				}
				itr.reset(); //reset to original entry if not found in view resource
			}
		}
		return lookupViewResource(ctx.getDefaultView(), type, entry, ctx).orElseGet(()->
			nonNull(anonymousResolver) ? anonymousResolver.apply(ctx.getDefaultView(), itr) : null);
	}
	
	//res=3 or res.fun1.eq=3 or res.in=1,2,3 or res.express=33 or res.express(33).and(..)
	static DBColumn chainResource(EntryIterator itr, DBColumn res, RequestContext ctx, Entry... outArgs) {
		var col = res;
		while(itr.hasNext()) {
			var entry = itr.peekNext();
			var tmp = invokeOperator(col, entry.getValue(), entry.getArgs(), ctx);
			if(isNull(tmp)) {
				var args = itr.hasNext() || nonNull(entry.getArgs()) ? entry.getArgs() : outArgs; //do not use entry.getArgs()
				tmp = invokeComparator(col, entry.getValue(), args, ctx);
				if(isNull(tmp) && nonNull(col)) {
					tmp = ctx.lookupSchemaResource(entry.getValue(), ComparisonExpression.class, args)
							.map(res::filter).orElse(null);
				}
			}
			if(isNull(tmp)) {
				break;
			}
			itr.advance();
			col = tmp;
		}
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

	static <T> Optional<T> lookupViewResource(ViewResource view, Class<T> type, Entry entry, RequestContext ctx) { //pretty syntax
		return ctx.lookupViewResource(view, entry.getValue(), type, entry.getArgs());
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
	

	static <T> T resolveSingleArgValue(Entry entry, Class<T> type, RequestContext ctx) {
		return resolveSingleArg(entry, e-> ctx.evalValue(e.get().getValue(), type));
	}
	
	static <T> T resolveSingleArg(Entry entry, Function<EntryIterator, T> resovler) {
		if(entry.hasArgs() && entry.getArgs().length == 1) {
			var itr = entry.getArgs()[0].iterator().advance();
			var res = resovler.apply(itr);
			assertLastEntry(itr, false);
			return res;
		}
		throw new EntryParseException("entry " + entry.getValue() + " must have exactly 1 argument");
	}

	static void assertLastEntry(EntryIterator entry, boolean tagAllowed) {
		assertLastEntry(entry.get(), tagAllowed);
	}

	static void assertLastEntry(Entry entry, boolean tagAllowed) {
		if(entry.hasNext()) {
			throw new EntrySyntaxException("unexpected entry : " + entry.getNext().getValue());
		}
		if(!tagAllowed && entry.hasTag()) {
			throw new EntrySyntaxException("unexpected tag : " + entry.getTag());
		}
	}
	
	static String requireTag(Entry entry) {
		if(entry.hasTag()) {
			return entry.getTag();
		}
		throw new EntrySyntaxException("entry must have a tag : " + entry.getValue());
	}
	
	public static void main(String[] args) {
		var sch = createSchema(SchemaSample.class, null);
		var ctx = new RequestContext(sch, sch.view1(), new TypeRegistry());
		System.out.println(evaluateColumn(parseEntry("toto.pl"), ctx));
	}
}