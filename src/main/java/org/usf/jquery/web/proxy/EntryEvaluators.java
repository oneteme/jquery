package org.usf.jquery.web.proxy;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.usf.jquery.core.Column.allColumns;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.Parameters.FIELD_PARAM;
import static org.usf.jquery.web.Parameters.PARTITION_OPR;
import static org.usf.jquery.web.Parameters.SELECT_OPR;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.usf.jquery.core.Column;
import org.usf.jquery.core.ComparatorDefinition;
import org.usf.jquery.core.Composer;
import org.usf.jquery.core.Criteria;
import org.usf.jquery.core.DBView;
import org.usf.jquery.core.Definition;
import org.usf.jquery.core.JoinsClause;
import org.usf.jquery.core.NamedColumn;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.OrderType;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.Predicate;
import org.usf.jquery.core.QueryView;
import org.usf.jquery.core.SingleQueryColumn;
import org.usf.jquery.core.ViewJoin;
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
		var itr = entry.iterator();
		var view = evalView(itr, ctx, true);
		if(nonNull(view)) {
			assertLastEntry(itr, true);
			ctx.declareView(requireTag(itr.get()), view);
			return view.getView();
		}
		throw new NoSuchResourceException("no such view : " + itr.peekNext().getValue());
	}
	
	public static DBView evaluateView2(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var view = evalView(itr, ctx, true);
		if(nonNull(view)) {
			assertLastEntry(itr, false);
			return view.getView();
		}
		throw new NoSuchResourceException("no such view : " + itr.peekNext().getValue());
	}

	public static NamedColumn evaluateNamedColumn(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var col = evalColumn(itr, ctx);
		if(nonNull(col)) {
			assertLastEntry(itr, true);
			var tag = itr.get().getTag();
			if(nonNull(tag)) {
				ctx.declareColumn(tag, col);
				return col.as(tag);
			}
			if(col instanceof NamedColumn nc) {
				return nc;
			}
			throw new EntryParseException("expected tag after : " + entry);
		}
		throw new NoSuchResourceException("no such named column : " + itr.peekNext().getValue());
	}
	
	public static Column evaluateColumn(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var col = evalColumn(itr, ctx);
		if(nonNull(col)) {
			assertLastEntry(itr, false);
			return col;
		}
		throw new NoSuchResourceException("no such column : " + itr.peekNext().getValue());
	}
	
	public static Criteria evaluateFilter(Entry entry, RequestContext ctx, Entry... outerArgs) {
		var itr = entry.iterator();
		var col = evalColumn(itr, ctx, outerArgs);
		if(col instanceof Criteria crt) { 
			assertLastEntry(itr, false);
			return crt; 
		}
		if(nonNull(col)) {
			throw new EntryParseException(itr.get().getValue() + " cannot be used as filter resource");
		}
		throw new NoSuchResourceException("no such column or filter : " + itr.next().getValue());
	}
	
	public static Order evaluateOrder(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var ord = lookupResource(itr, Order.class, ctx, (v, e)-> evalOrder(e, ctx));
		if(nonNull(ord)) {
			assertLastEntry(itr, false);
			return ord;
		}
		throw new NoSuchResourceException("no such order : " + itr.peekNext().getValue());
	}

	public static JoinsClause evaluateJoin(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var join = lookupResource(itr, JoinsClause.class, ctx, (v, e)-> evalJoin(e, v, ctx));
		if(nonNull(join)) {
			assertLastEntry(itr, false);
			return join;
		}
		throw new NoSuchResourceException("no such join : " + itr.peekNext().getValue());
	}
	
	public static Partition evaluatePartition(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var prt = lookupResource(itr, Partition.class, ctx, (v,e)-> evalPartition(e, v, ctx));
		if(nonNull(prt)) {
			assertLastEntry(itr, false);
			return prt;
		}
		throw new NoSuchResourceException("no such partition : " + itr.peekNext().getValue());
	}

	public static SingleQueryColumn evaluateQueryColumn(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var col = lookupResource(itr, SingleQueryColumn.class, ctx, (v, e)-> evalColumnQuery(itr, v, ctx));
		if(nonNull(col)) {
			assertLastEntry(itr, false);
			return col;
		}
		throw new NoSuchResourceException("no such query column : " + itr.peekNext().getValue());
	}
	
	static SingleQueryColumn evalColumnQuery(EntryIterator itr, DatasetResource view, RequestContext ctx) {
		var entry = requireNonNull(itr.peekNext(), "no entry to evaluate as view resource");
		if(SELECT_OPR.equals(entry.getValue()) || FIELD_PARAM.equals(entry.getValue())) {
			view = evalQuery(itr, view, ctx);
		}
		if(view instanceof QueryResource query) {
			if(query.getQuery().getColumns().length == 1) {
				return query.getQuery().asColumn();
			}
			throw new EntryParseException(""); //TODO
		}
		return null;
	}

	static DatasetResource evalView(EntryIterator itr, RequestContext ctx, boolean allowAnonymous) {
		var entry = requireNonNull(itr.peekNext(), "no entry to evaluate as view resource");
		if(allowAnonymous && (SELECT_OPR.equals(entry.getValue()) || FIELD_PARAM.equals(entry.getValue()))) {
			return evalQuery(itr, ctx.getDefaultDataset(), ctx);
		} //parameterized view considered as anonymous view resource, not supported for direct lookup
		var view = ctx.lookupView(allowAnonymous, entry.getValue(), entry.getArgs());
		if(view.isPresent()) {
			itr.advance();
			return allowAnonymous && itr.hasNext() && (SELECT_OPR.equals(itr.peekNext().getValue()) || FIELD_PARAM.equals(itr.peekNext().getValue())) //view.column().filter()..
					? evalQuery(itr, view.get(), ctx) : view.get();
		}
		return null;
	}

	static Order evalOrder(EntryIterator itr, RequestContext ctx) {
		var col = evalColumn(itr, ctx);
		if(isNull(col)) {
			return null;
		}
		if(itr.hasNext()) {
			var entry = itr.peekNext();
			if(nonNull(entry.getValue()) && entry.getValue().matches("asc|desc")) {
				if(!entry.hasArgs()) {
					itr.advance(); //consume order type entry
					return col.order(OrderType.valueOf(entry.getValue().toUpperCase()));
				}
				throw new EntryParseException("order operator cannot have arguments");
			}
		}
		return col.order();
	}

	static Column evalColumn(EntryIterator itr, RequestContext ctx, Entry... outArgs) {
		var next = itr.peekNext();
		var res = ctx.lookupDeclaredColumn(next.getValue());
		Column col = null;
		if(res.isPresent()) {
			itr.advance();
			col = res.get();
		}
		else {
			col = lookupResource(itr, Column.class, ctx, (v, e)-> { //column | criteria
				var entry = e.peekNext();
				if("count".equals(entry.getValue())) {
					itr.advance();
					var def = ctx.getDialect().count();
					return def.invoke(entry.hasArgs() ? ctx.resolveArgs(entry.getArgs(), null, def) : allColumns(v.getView()));
				}
				if("when".equals(entry.getValue())) { //view.when
					return (Column) invokeDialectComposer(itr, ctx.withView(v)); 
				}
				return null;
			}); //column or criteria resource
		}
		return chainResource(itr, col, ctx, outArgs);
	}

	static QueryResource evalQuery(EntryIterator itr, DatasetResource dr, RequestContext ctx) { 
		if(itr.hasNext() && SELECT_OPR.equals(itr.peekNext().getValue())) {
			Object res = null;
			try {
				res = invokeDialectComposer(itr, ctx.subContext(dr));
			}
			catch (Exception e) {
				throw new EntryParseException("cannot parse query arguments ", e);
			}
			if(res instanceof QueryView query) {
				if(query.getColumns().length > 0) {
					return new QueryResource(query);
				}
				throw new EntryParseException("query must have at least one column");
			}
			throw new EntryParseException("invalid query definition");
		}
		return null;	
	}
	
	static Partition evalPartition(EntryIterator itr, DatasetResource dr, RequestContext ctx) {
		if(itr.hasNext() && PARTITION_OPR.equals(itr.peekNext().getValue())) {
			Object res = null;
			try {
				res = invokeDialectComposer(itr, ctx.withView(dr));
			}
			catch (Exception e) {
				throw new EntryParseException("cannot parse partition arguments ", e);
			}
			if(res instanceof Partition part) {
				return part;
			}
			throw new EntryParseException("invalid partition definition");
		}
		return null;
	}

	static JoinsClause evalJoin(EntryIterator itr, DatasetResource dr, RequestContext ctx) {
		var v = itr.hasNext() ? itr.peekNext().getValue() : null;
		if(nonNull(v) && v.matches("(inner|left|right|full|cross)Join")) {
			Object res = null;
			try {
				res = invokeDialectComposer(itr, ctx.withView(dr));
			}
			catch (Exception e) {
				throw new EntryParseException("cannot parse join arguments ", e);
			}
			if(res instanceof ViewJoin join) {
				return new JoinsClause(join);
			}
			throw new EntryParseException("invalid join definition");
		}
		return null;
	}	
	
	static <T> T lookupResource(EntryIterator itr, Class<T> type, RequestContext ctx, BiFunction<DatasetResource, EntryIterator, T> anonymousResolver) {
		var entry = requireNonNull(itr.peekNext(), "no entry to evaluate as resource");
		if(entry.hasNext()) {
			var view = ctx.lookupView(false, entry.getValue()); //parameterized view resource is not supported, must be declared as view resource in context
			if(view.isPresent()) {
				var res = lookupViewResource(view.get(), type, itr.mark().advance(), ctx) //consume view entry
						.orElseGet(()-> nonNull(anonymousResolver) ? anonymousResolver.apply(view.get(), itr) : null);
				if(nonNull(res)) {
					return res;
				}
				itr.resetToMark();
			}
		}
		return lookupViewResource(ctx.getDefaultDataset(), type, itr, ctx)
				.orElseGet(()-> nonNull(anonymousResolver) ? anonymousResolver.apply(ctx.getDefaultDataset(), itr) : null);
	}

	static <T> Optional<T> lookupViewResource(DatasetResource view, Class<T> type, EntryIterator itr, RequestContext ctx) {
		var entry = requireNonNull(itr.peekNext(), "no entry to evaluate as resource");
		var res = ctx.lookupViewResource(view, entry.getValue(), type, entry.getArgs());
		if(res.isPresent()) {
			itr.advance();
		}
		return res;
	}
	
	//res=3 or res.fun1.eq=3 or res.in=1,2,3 or res.express=33 or res.express(33).and(..)
	static Column chainResource(EntryIterator itr, Column res, RequestContext ctx, Entry... outArgs) {
		var col = res;
		while(itr.hasNext()) {
			Entry entry = itr.peekNext();
			var args = entry.getArgs();
			if(isNull(args) && !entry.hasNext()) {
				args = outArgs;
			}
			var r = ctx.lookupSchemaResource(entry.getValue(), Predicate.class, args);
			if(r.isPresent()) {
				itr.advance();
				col = col.filter(r.get());
				if(args == outArgs) {
					outArgs = null;
				}
			}
			else {
				var opt = ctx.lookupDialectResource(entry.getValue(), Definition.class);
				if(opt.isPresent()) {
					itr.advance(); 
					var def = opt.get();
					if(def instanceof ComparatorDefinition) {
						col = (Column) def.invoke(ctx.resolveArgs(args, col, def));
						if(args == outArgs) {
							outArgs = null;
						}
					}
					else {
						col = (Column) def.invoke(ctx.resolveArgs(entry.getArgs(), col, def));
					}
				}
				else {
					break;
				}
			}
		}
		if(!itr.hasNext() && !isEmpty(outArgs)) { //last entry, outArgs not yet applied
			var cmp = outArgs.length == 1 ? ctx.getDialect().eq() : ctx.getDialect().in();
			col = cmp.invoke(ctx.resolveArgs(outArgs, col, cmp));
		}
		return col;
	}
	
	static Object invokeDialectComposer(EntryIterator itr, RequestContext ctx) {
		if(itr.hasNext()) {
			var entry = itr.peekNext();
			var res = ctx.lookupDialectResource(entry.getValue(), Definition.class);
			if(res.isPresent()) {
				itr.advance();
				var def = res.get(); 
				var obj = def.invoke(ctx.resolveArgs(entry.getArgs(), null, def));
				while(itr.hasNext() && obj instanceof Composer<?>) {
					entry = itr.peekNext();
					res = ctx.lookupDialectResource(entry.getValue(), Definition.class, obj);
					if(res.isPresent()) {
						itr.advance();
						def = res.get(); 
						obj = def.invoke(ctx.resolveArgs(entry.getArgs(), null, def));
					}
					else {
						break; //stop at first non applicable operator
					}
				}
				return obj instanceof Composer<?> cmp ? cmp.compose() : obj; //compose if last operator is composer, otherwise return result as is
			}
		}
		return null;
	}
	
	
	static <T> T resolveSingleArgValue(Entry entry, Class<T> type, RequestContext ctx) {
		return resolveSingleArg(entry, e-> ctx.parseValue(e.next().getValue(), type));
	}
	
	static <T> T resolveSingleArg(Entry entry, Function<EntryIterator, T> resovler) {
		if(entry.hasArgs() && entry.getArgs().length == 1) {
			var itr = entry.getArgs()[0].iterator();
			var obj = resovler.apply(itr);
			assertLastEntry(itr, false);
			return obj;
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
		throw new EntrySyntaxException("expected tag after : " + entry.getValue());
	}	
}