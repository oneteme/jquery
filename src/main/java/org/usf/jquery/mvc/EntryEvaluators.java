package org.usf.jquery.mvc;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.usf.jquery.core.Column.allColumns;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.mvc.Parameters.PARTITION_OPR;
import static org.usf.jquery.mvc.Parameters.SELECT_PARAM;

import java.util.function.BiFunction;

import org.usf.jquery.core.Column;
import org.usf.jquery.core.ComparatorDefinition;
import org.usf.jquery.core.Composer;
import org.usf.jquery.core.Criteria;
import org.usf.jquery.core.Definition;
import org.usf.jquery.core.Group;
import org.usf.jquery.core.Join;
import org.usf.jquery.core.JoinGroup;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.OrderType;
import org.usf.jquery.core.Partition;
import org.usf.jquery.core.Predicate;
import org.usf.jquery.core.Query;
import org.usf.jquery.core.SingleQueryColumn;
import org.usf.jquery.core.View;

import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class EntryEvaluators {
	
	public static View evaluateView(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var view = lookupView(itr, ctx, true);
		if(itr.hasNext()) {
			var qry = evalQuery(itr, nonNull(view) ? view : ctx.getDefaultDataset(), ctx); //fast check if query matches 
			if(nonNull(qry)) {
				assertLastEntry(itr, true);
				var tag = itr.get().getTag();
				if(nonNull(tag)) {
					ctx.declareView(tag, new QueryResource(qry));
				}
				return qry;
			}
			throw new EntrySyntaxException("unexpected entry : " + itr.next().getValue());
		}
		if(nonNull(view)) {
			assertLastEntry(itr, true);
			var tag = itr.get().getTag();
			if(nonNull(tag)) {
				ctx.declareView(tag, view);
			}
			return view.getView();
		}
		throw new NoSuchResourceException("no such view : " + itr.peekNext().getValue());
	}
	
	public static View evaluateQuery(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var view = lookupView(itr, ctx, true);
		if(itr.hasNext()) {
			var qry = evalQuery(itr, nonNull(view) ? view : ctx.getDefaultDataset(), ctx); //fast check if query matches 
			if(nonNull(qry)) {
				assertLastEntry(itr, true);
				var tag = itr.get().getTag();
				if(nonNull(tag)) {
					ctx.declareView(tag, new QueryResource(qry));
				}
				return qry;
			}
			throw new EntrySyntaxException("unexpected entry : " + itr.next().getValue());
		}
		throw new NoSuchResourceException("no such query : " + itr.peekNext().getValue());
	}

	public static Column evaluateColumn(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var col = evalColumn(itr, ctx);
		if(nonNull(col)) {
			assertLastEntry(itr, true);
			var tag = itr.get().getTag();
			return nonNull(tag) ? col.as(tag) : col;
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

	public static JoinGroup evaluateJoin(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var join = lookupResource(itr, JoinGroup.class, ctx, (v, e)-> evalJoin(e, v, ctx));
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
	
	public static Group evaluateGroup(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var prt = lookupResource(itr, Group.class, ctx, (v,e)-> evalGroup(e, v, ctx));
		if(nonNull(prt)) {
			assertLastEntry(itr, false);
			return prt;
		}
		throw new NoSuchResourceException("no such within group : " + itr.peekNext().getValue());
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
	
	static SingleQueryColumn evalColumnQuery(EntryIterator itr, DatasetResource rsc, RequestContext ctx) {
		var entry = requireNonNull(itr.peekNext(), "no entry to evaluate as view resource");
		var view = rsc.getView(); 
		if(SELECT_PARAM.equals(entry.getValue())) {
			view = evalQuery(itr, rsc, ctx);
		}
		if(view instanceof Query query) {
			return query.asColumn();
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

	static Query evalQuery(EntryIterator itr, DatasetResource dr, RequestContext ctx) { 
		if(itr.hasNext() && SELECT_PARAM.equals(itr.peekNext().getValue())) {
			Object res = null;
			try {
				res = invokeDialectComposer(itr, ctx.subContext(dr));
			}
			catch (Exception e) {
				throw new EntryParseException("cannot parse query arguments ", e);
			}
			if(res instanceof Query query) {
				if(!isEmpty(query.getSelects())) {
					return query;
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
				throw new EntryParseException("cannot parse partition arguments", e);
			}
			if(res instanceof Partition part) {
				return part;
			}
			throw new EntryParseException("invalid partition definition");
		}
		return null;
	}
	
	static Group evalGroup(EntryIterator itr, DatasetResource dr, RequestContext ctx) {
		if(itr.hasNext() && "group".equals(itr.peekNext().getValue())) {
			Object res = null;
			try {
				res = invokeDialectComposer(itr, ctx.withView(dr));
			}
			catch (Exception e) {
				throw new EntryParseException("cannot parse withing arguments ", e);
			}
			if(res instanceof Group wth) {
				return wth;
			}
			throw new EntryParseException("invalid withing definition");
		}
		return null;
	}
	
	static JoinGroup evalJoin(EntryIterator itr, DatasetResource dr, RequestContext ctx) {
		var v = itr.hasNext() ? itr.peekNext().getValue() : null;
		if(nonNull(v) && v.matches("(inner|left|right|full|cross)Join")) {
			Object res = null;
			try {
				res = invokeDialectComposer(itr, ctx.withView(dr));
			}
			catch (Exception e) {
				throw new EntryParseException("cannot parse join arguments ", e);
			}
			if(res instanceof Join join) {
				return new JoinGroup(join);
			}
			throw new EntryParseException("invalid join definition");
		}
		return null;
	}	
	
	static <T> T lookupResource(EntryIterator itr, Class<T> type, RequestContext ctx, BiFunction<DatasetResource, EntryIterator, T> evaluator) {
		if(itr.hasNext()) {
			var view = lookupView(itr.mark(), ctx, false); //avoid parameterized view 
			if(nonNull(view)) {
				var res = lookupViewResource(view, type, itr, ctx);
				if(isNull(res) && nonNull(evaluator)){
					res = evaluator.apply(view, itr);
				}
				if(nonNull(res)) {
					return res;
				}
				itr.resetToMark();
			}
		}
		var res = lookupViewResource(ctx.getDefaultDataset(), type, itr, ctx);
		return isNull(res) && nonNull(evaluator) ? evaluator.apply(ctx.getDefaultDataset(), itr) : res;
	}
	
	static DatasetResource lookupView(EntryIterator itr, RequestContext ctx, boolean allowParameterized) {
		if(itr.hasNext()) {
			var entry = itr.peekNext();
			var view  = ctx.lookupView(entry.getValue(), allowParameterized, entry.getArgs());
			if(nonNull(view)) {
				itr.advance();
				return view;
			}
		}
		return null;
	}

	static <T> T lookupViewResource(DatasetResource view, Class<T> type, EntryIterator itr, RequestContext ctx) {
		if(itr.hasNext()) {
			var entry = itr.peekNext();
			var res = ctx.lookupResource(entry.getValue(), view, type, entry.getArgs());
			if(nonNull(res)) {
				itr.advance();
				return res;
			}
		}
		return null;
	}
	
	//res=3 or res.fun1.eq=3 or res.in=1,2,3 or res.express=33 or res.express(33).and(..)
	static Column chainResource(EntryIterator itr, Column prev, RequestContext ctx, Entry... outArgs) {
		var col = prev;
		while(itr.hasNext()) {
			Entry entry = itr.peekNext();
			var args = entry.getArgs();
			var r = ctx.getStore().lookup(entry.getValue(), Predicate.class);
			if(nonNull(r)) {
				itr.advance();
				if(isNull(args) && !entry.hasNext()) {
					args = outArgs;
					outArgs = null;
				}
				col = col.filter(r.invoke(ctx.evaluate(args, r.getParameters())));
			}
			else {
				var res = ctx.getStore().lookupDialect(entry.getValue(), Definition.class);
				if(isNull(res)) {
					break;
				}
				itr.advance(); 
				if(isNull(args) && !entry.hasNext() && res.getType() == ComparatorDefinition.class) {
					args = outArgs;
					outArgs = null;
				}
				var def = res.invoke(); //takes no args, args are passed to the definition instance
				if(def.invoke(ctx.resolveArgs(args, col, def)) instanceof Column cc) {
					col = cc;
				}
				else {
					throw new EntryParseException("invalid column chain definition");	
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
			var res = ctx.getStore().lookupDialect(entry.getValue(), Definition.class);
			if(nonNull(res)) {
				itr.advance();
				var def = res.invoke(); 
				var obj = def.invoke(ctx.resolveArgs(entry.getArgs(), null, def));
				while(itr.hasNext() && obj instanceof Composer<?>) {
					entry = itr.peekNext();
					res = ctx.getStore().lookupDialect(entry.getValue(), Definition.class, obj);
					if(nonNull(res)) {
						itr.advance();
						def = res.invoke(obj); 
						obj = def.invoke(ctx.resolveArgs(entry.getArgs(), null, def));
					}
					else {
						break; //stop at first non applicable operator
					}
				}
				return obj instanceof Composer<?> cmp ? cmp.compose(ctx.getStore()) : obj; //compose if last operator is composer, otherwise return result as is
			}
		}
		return null;
	}

	static void assertLastEntry(EntryIterator entry, boolean tagAllowed) {
		var e = entry.get();
		if(e.hasNext()) {
			throw new EntrySyntaxException("unexpected entry : " + e.getNext().getValue());
		}
		if(!tagAllowed && e.hasTag()) {
			throw new EntrySyntaxException("unexpected tag : " + e.getTag());
		}
	}
}