package org.usf.jquery.mvc;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.usf.jquery.core.Column.allColumns;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.mvc.Parameters.PARTITION_OPR;
import static org.usf.jquery.mvc.Parameters.SELECT_PARAM;

import java.util.function.BiFunction;

import org.usf.jquery.core.CaseColumn;
import org.usf.jquery.core.Column;
import org.usf.jquery.core.ComparatorDefinition;
import org.usf.jquery.core.Composer;
import org.usf.jquery.core.ComposerDefinition;
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
			var qry = composeQuery(itr, nonNull(view) ? view : ctx.getDefaultDataset(), ctx); //fast check if query matches 
			if(nonNull(qry)) {
				assertLastEntry(itr, true);
				var tag = itr.get().getTag();
				if(nonNull(tag)) {
					ctx.declareView(tag, new QueryResource(qry));
				}
				return qry;
			}
			throw new EntrySyntaxException("unexpected entry : " + itr.peekNext().getValue());
		}
		if(nonNull(view)) { //view only
			assertLastEntry(itr, true);
			var tag = itr.get().getTag();
			if(nonNull(tag)) {
				ctx.declareView(tag, view);
			}
			return view.getView();
		}
		throw new NoSuchResourceException("no such view : " + entry);
	}
	
	public static View evaluateQuery(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var view = lookupView(itr, ctx, true);
		if(itr.hasNext()) {
			var qry = composeQuery(itr, nonNull(view) ? view : ctx.getDefaultDataset(), ctx); //fast check if query matches 
			if(nonNull(qry)) {
				assertLastEntry(itr, true);
				var tag = itr.get().getTag();
				if(nonNull(tag)) {
					ctx.declareView(tag, new QueryResource(qry));
				}
				return qry;
			}
			throw new EntrySyntaxException("unexpected entry : " + itr.peekNext().getValue());
		}
		throw new NoSuchResourceException("no such query : " + entry);
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
	
	public static Criteria evaluateCriteria(Entry entry, RequestContext ctx, Entry... outerArgs) {
		var itr = entry.iterator();
		var col = evalColumn(itr, ctx, outerArgs);
		if(col instanceof Criteria crt) { 
			assertLastEntry(itr, false);
			return crt; 
		}
		throw nonNull(col) 
			? new EntryParseException(col + " cannot be used as filter resource")
			: new NoSuchResourceException("no such criteria : " + itr.peekNext().getValue());
	}
	
	public static Order evaluateOrder(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var ord = lookupResource(itr, Order.class, ctx, (v,e)-> evalOrder(e.reset(), ctx));
		if(nonNull(ord)) {
			assertLastEntry(itr, false);
			return ord;
		}
		throw new NoSuchResourceException("no such order : " + itr.peekNext().getValue());
	}

	public static JoinGroup evaluateJoin(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var join = lookupResource(itr, JoinGroup.class, ctx, (v,e)-> composeJoin(e, v, ctx));
		if(nonNull(join)) {
			assertLastEntry(itr, false);
			return join;
		}
		throw new NoSuchResourceException("no such join : " + itr.peekNext().getValue());
	}
	
	public static Partition evaluatePartition(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var prt = lookupResource(itr, Partition.class, ctx, (v,e)-> composePartition(e, v, ctx));
		if(nonNull(prt)) {
			assertLastEntry(itr, false);
			return prt;
		}
		throw new NoSuchResourceException("no such partition : " + itr.peekNext().getValue());
	}
	
	public static Group evaluateGroup(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var prt = lookupResource(itr, Group.class, ctx, (v,e)-> composeGroup(e, v, ctx));
		if(nonNull(prt)) {
			assertLastEntry(itr, false);
			return prt;
		}
		throw new NoSuchResourceException("no such within group : " + itr.peekNext().getValue());
	}

	public static SingleQueryColumn evaluateQueryColumn(Entry entry, RequestContext ctx) {
		var itr = entry.iterator();
		var col = lookupResource(itr, SingleQueryColumn.class, ctx, (v,e)-> evalColumnQuery(itr, v, ctx));
		if(nonNull(col)) {
			assertLastEntry(itr, false);
			return col;
		}
		throw new NoSuchResourceException("no such query column : " + itr.peekNext().getValue());
	}
	
	static SingleQueryColumn evalColumnQuery(EntryIterator itr, DatasetCatalog rsc, RequestContext ctx) {
		var entry = requireNonNull(itr.peekNext(), "no entry to evaluate as view resource");
		var view = rsc.getView(); 
		if(SELECT_PARAM.equals(entry.getValue())) {
			view = composeQuery(itr, rsc, ctx);
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
		if(itr.hasNext()) {
			var res = ctx.lookupDeclaredColumn(itr.peekNext().getValue());
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
						return entry.hasArgs() 
								? def.invoke(ctx.resolveArgs(entry.getArgs(), null, def))
								: def.invoke(allColumns(v.getView()));
					}
					if("when".equals(entry.getValue())) { //view.when
						return tyComposeExpression(itr, ctx, CaseColumn.class, v, "when"::equals); 
					}
					return null;
				}); //column or criteria resource
			}
			return chainExpression(itr, col, ctx, outArgs);
		}
		return null;
	}

	static Query composeQuery(EntryIterator itr, DatasetCatalog dr, RequestContext ctx) { 
		return tyComposeExpression(itr, ctx, Query.class, dr, SELECT_PARAM::equals);	
	}
	
	static Partition composePartition(EntryIterator itr, DatasetCatalog dr, RequestContext ctx) {
		return tyComposeExpression(itr, ctx, Partition.class, dr, PARTITION_OPR::equals);
	}
	
	static Group composeGroup(EntryIterator itr, DatasetCatalog dr, RequestContext ctx) {
		return tyComposeExpression(itr, ctx, Group.class, dr, "group"::equals);
	}
	
	static JoinGroup composeJoin(EntryIterator itr, DatasetCatalog dr, RequestContext ctx) {
		var v = tyComposeExpression(itr, ctx, Join.class, dr, s-> s.matches("(inner|left|right|full|cross)Join"));
		return nonNull(v) ? new JoinGroup(v) : null;
	}

	static <T> T tyComposeExpression(EntryIterator itr, RequestContext ctx, Class<T> type, DatasetCatalog dr, java.util.function.Predicate<String> filter) {
		if(itr.hasNext() && filter.test(itr.peekNext().getValue())) {
			try {
				return chainComposerExpression(itr, ctx.withView(dr), type);
			}
			catch (Exception e) {
				throw new EntryParseException("cannot parse '%s' arguments ".formatted(type.getSimpleName()), e);
			}
		}
		return null;
	}
	
	static <T> T lookupResource(EntryIterator itr, Class<T> type, RequestContext ctx, BiFunction<DatasetCatalog, EntryIterator, T> composer) {
		if(itr.hasNext()) { //view name == resource name
			var view = lookupView(itr.mark(), ctx, false); //parameterized views are not allowed in resource lookup
			if(nonNull(view)) {
				var res = lookupViewResource(view, type, itr, ctx);
				if(isNull(res) && nonNull(composer)){
					res = composer.apply(view, itr);
				}
				if(nonNull(res)) {
					return res;
				}
			}
			itr.resetToMark();
		}
		var res = lookupViewResource(ctx.getDefaultDataset(), type, itr, ctx);
		return isNull(res) && nonNull(composer) ? composer.apply(ctx.getDefaultDataset(), itr) : res;
	}
	
	static DatasetCatalog lookupView(EntryIterator itr, RequestContext ctx, boolean allowParameterized) {
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

	static <T> T lookupViewResource(DatasetCatalog view, Class<T> type, EntryIterator itr, RequestContext ctx) {
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
	static Column chainExpression(EntryIterator itr, Column prev, RequestContext ctx, Entry... outArgs) {
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
				col = col.filter(ctx.invokeResource(r, args));
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
	
	static <T> T chainComposerExpression(EntryIterator itr, RequestContext ctx, Class<T> type) {
		if(itr.hasNext()) {
			var entry = itr.peekNext();
			var obj = ctx.lookupDialect(entry.getValue(), ComposerDefinition.class, null, entry.getArgs());
			if(nonNull(obj)) {
				itr.advance();
				while(itr.hasNext() && obj instanceof Composer<?>) {
					entry = itr.peekNext();
					var res = ctx.lookupDialect(entry.getValue(), ComposerDefinition.class, obj, entry.getArgs());
					if(isNull(res)) {
						break; //stop at first non applicable operator
					}
					itr.advance();
					obj = res;
				}
				if(obj instanceof Composer<?> cmp) {
					obj = cmp.compose(ctx.getStore());
				}
				if(type.isInstance(obj)) {
					return type.cast(obj);
				}
				throw new EntryParseException("invalid '%s' chain expression : %s".formatted(type.getSimpleName(), obj));
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