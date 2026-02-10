package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.ViewJoin.join;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.usf.jquery.core.Comparator;
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
import org.usf.jquery.core.SingleQueryColumn;
import org.usf.jquery.core.TypedComparator;
import org.usf.jquery.core.TypedOperator;
import org.usf.jquery.web.EntryParseException;
import org.usf.jquery.web.EntrySyntaxException;

public final class ExpressionEvaluator {

	public static DBView evalView(EntryChain entry, QueryContext ctx) {
		try {
			var itr = entry.iterator();
			var view = evalView(itr, ctx);
			assertEntry(itr.get(), false, true);
			ctx.addView(requireTag(itr.get()), view);
			return view;
		}
		catch (Exception e) {
			throw new EntryParseException("invalid view expression : " + entry.getValue(), e);
		}
	}

	public static DBColumn evalColumn(EntryChain entry, QueryContext ctx) {
		try {
			var itr = entry.iterator();
			var col = parseColumn(itr, ctx);
			assertEntry(itr.get(), false, false);
			return col;
		}
		catch (Exception e) {
			throw new EntryParseException("invalid column expression : " + entry.getValue(), e);
		}
	}

	public static NamedColumn evalNamedColumn(EntryChain entry, QueryContext ctx) {
		try {
			var itr = entry.iterator();
			var col = parseColumn(itr, ctx);
			assertEntry(itr.get(), false, true);
			return col instanceof NamedColumn nc ? nc : col.as(requireTag(itr.get()));
		}
		catch (Exception e) {
			throw new EntryParseException("invalid named column expression : " + entry.getValue(), e);
		}
	}
	
	public static DBFilter evalFilter(EntryChain entry, QueryContext ctx) {
		return null;
	}
	
	public static DBOrder evalOrder(EntryChain entry, QueryContext ctx) {
		try {
			var itr = entry.iterator();
			var ord = lookupDeclaredResource(itr, DBOrder.class, ctx).orElseGet(()-> parseOrder(itr.reset(), ctx));
			assertEntry(itr.get(), false, false);
			return ord;
		}
		catch (Exception e) {
			throw new EntryParseException("invalid order expression : " + entry.getValue(), e);
		}
	}

	public static JoinsClause evalJoin(EntryChain entry, QueryContext ctx) {
		try {
			var itr = entry.iterator();
			var join = lookupDeclaredResource(itr, JoinsClause.class, ctx).orElseGet(()-> parseJoin(itr.reset(), ctx));
			assertEntry(itr.get(), false, false);
			return join;
		}
		catch (Exception e) {
			throw new EntryParseException("invalid join expression : " + entry.getValue(), e);
		}
	}
	
	public static Partition evalPartition(EntryChain entry, QueryContext ctx) {
		try {
			var itr = entry.iterator();
			var prt = lookupDeclaredResource(itr, Partition.class, ctx).orElseGet(()-> parsePartition(itr.reset(), ctx));
			assertEntry(itr.get(), false, false);
			return prt;
		}
		catch (Exception e) {
			throw new EntryParseException("invalid partition expression : " + entry.getValue(), e);
		}
	}

	public static SingleQueryColumn evalQueryColumn(EntryChain entry, QueryContext ctx) {
		throw new UnsupportedOperationException("not implemented yet");
	}
	
	static DBView evalView(EntryChainIterator itr, QueryContext ctx) {
		var entry = itr.get();
		var res = ctx.lookupView(entry.getValue(), entry.getArgs());
		if(res.isPresent()) {
			return itr.hasNext() ? parseView(itr.advance(), ctx.subContext(res.get())) : res.get(); //view.select().filter()..
		}
		throw new NoSuchElementException("view resource not found : " + entry.getValue());
	}
	
	static DBView parseView(EntryChainIterator itr, QueryContext ctx) {
		//select().filter().order()...
		return null;
	}
	
	static DBView parseFilter(EntryChainIterator itr, QueryContext ctx) {
		//select().filter().order()...
		return null;
	}
	
	static DBOrder parseOrder(EntryChainIterator itr, QueryContext ctx) {
		var col = parseColumn(itr, ctx);
		DBOrder order;
		if(itr.hasNext()) {
			var e = itr.next();
			if(e.getValue().matches("asc|desc")) {
				order = col.order(OrderType.valueOf(e.getValue().toUpperCase()));
			}
			else {
				throw new EntryParseException("invalid order type : " + e.getValue());
			}
		}
		else {
			order = col.order();
		}
		return order;
	}
	
	static JoinsClause parseJoin(EntryChainIterator itr, QueryContext ctx) {
		var entry = itr.get();
		if(nonNull(entry.getArgs()) && entry.getArgs().length == 2 && entry.getValue().matches("(inner|left|right|full|cross)Join")) {
			try {
				var type = JoinType.valueOf(entry.getValue().substring(0, entry.getValue().length()-5).toUpperCase());
				var view = evalView(entry.getArgs()[0], ctx);
				var filter = evalFilter(entry.getArgs()[1], ctx);
				return JoinsClause.of(join(type, view, filter));
			}
			catch (Exception e) {
				throw new EntryParseException("invalid join expression : ", e); //head !?
			}
		}
		throw new EntryParseException("invalid join expression : "); //head !?
	}
	
	static Partition parsePartition(EntryChainIterator itr, QueryContext ctx) {
		
		return null;
	}
	
	static DBColumn parseColumn(EntryChainIterator itr, QueryContext ctx) {
		var res = lookupDeclaredResource(itr, DBColumn.class, ctx); //column or criteria resource
		var col = chainResource(itr, res.orElse(null), ctx);
		if(nonNull(col)) {
			return col;
		}
		throw new NoSuchElementException("column resource not found : " + itr.get().getValue());
	}
	
	static <T> Optional<T> lookupDeclaredResource(EntryChainIterator itr, Class<T> type, QueryContext ctx) {
		DBView view = ctx.getDefaultView();
		var e = itr.get();
		if(e.hasNext()) {
			var res = ctx.lookupView(e.getNext().getValue());
			if(res.isPresent()) {
				view = res.get();
				e = itr.next();
			}
		}
		return ctx.lookupViewResource(view, e.getValue(), type, e.getArgs());
	}
	
	static void assertEntry(EntryChain entry, boolean nextAllowed, boolean tagAllowed) {
		if(!nextAllowed && entry.hasNext()) {
			throw new EntrySyntaxException(" entry cannot have next entry");
		}
		if(!tagAllowed && entry.hasTag()) {
			throw new EntrySyntaxException(" entry cannot have tag");
		}
	}
	
	static DBColumn chainResource(EntryChainIterator itr, DBColumn res, QueryContext ctx) {
		var col = res;
		while(itr.hasNext()) {
			var entry = itr.get().getNext();
			var tmp = invokeOperator(entry, col, ctx);
			if(isNull(tmp)) {
				tmp = invokeComparator(entry, col, ctx);
			}
			if(isNull(tmp)) {
				break;
			}
			else {
				itr.advance();
				col = tmp;
			}
		}
		return col;
	}

	static DBColumn invokeOperator(EntryChain entry, Object pre, QueryContext ctx) {
		var opr = lookup(Operator.class, TypedOperator.class, entry.getValue()); //count !?
		return nonNull(opr) 
				? opr.operation(resolveArgs(opr.getParameterSet(), pre, entry.getArgs(), ctx))
				: null;
	}
	
	static DBFilter invokeComparator(EntryChain entry, Object pre, QueryContext ctx) {
		var cmp = lookup(Comparator.class, TypedComparator.class, entry.getValue());
		return nonNull(cmp) 
			? cmp.filter(resolveArgs(cmp.getParameterSet(), pre, entry.getArgs(), ctx))
			: null;
	}
	
	static <T> T lookup(Class<?> clazz, Class<T> type, String name) {
		try {
			var mth = clazz.getMethod(name); //no parameter
			if(nonNull(mth)) {
				var mod = mth.getModifiers();
				if(mth.getReturnType() == type && mth.getParameterCount()==0 && isStatic(mod) && isPublic(mod)) {
					return type.cast(mth.invoke(null));
				}
			}
		} catch (Exception e) {/* do not throw exception */}
		return null;
	}
	
	static Object[] resolveArgs(ParameterSet ps, Object res, EntryChain[] args, QueryContext ctx){
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
				arr[i] = ctx.eval(args[i], p.getTypes());
			}
		});
		return arr;
	}
	
	static String requireTag(EntryChain entry) {
		if(entry.hasTag()) {
			return entry.getTag();
		}
		throw new EntrySyntaxException("entry must have a tag : " + entry.getValue());
	}
}
