package org.usf.jquery.web;

import static java.lang.reflect.Array.newInstance;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.BadArgumentException.badArgumentCountException;
import static org.usf.jquery.core.Comparator.lookupComparator;
import static org.usf.jquery.core.Operator.lookupOperator;
import static org.usf.jquery.core.Operator.lookupWindowFunction;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Parameter.varargs;
import static org.usf.jquery.core.ParameterSet.ofParameters;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Utils.findEnum;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.web.ArgumentParsers.parse;
import static org.usf.jquery.web.ColumnDecorator.ofColumn;
import static org.usf.jquery.web.JQueryContext.context;

import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;

import org.usf.jquery.core.BadArgumentException;
import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBObject;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.JqueryType;
import org.usf.jquery.core.LogicalOperator;
import org.usf.jquery.core.OperationColumn;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.ParameterSet;
import org.usf.jquery.core.TaggableColumn;
import org.usf.jquery.core.TypedComparator;
import org.usf.jquery.core.TypedOperator;

import lombok.AccessLevel;
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
@RequiredArgsConstructor
final class RequestEntryChain {
	
	private final String value;
	private final boolean text; //"string"
	private RequestEntryChain next;
	private List<RequestEntryChain> args;
	private String tag;

	public RequestEntryChain(String value) {
		this(value, false);
	}
	
	public TaggableColumn evalColumn(TableDecorator td) {
		var r = chainResourceOperations(td);
		if(r.entry.isLast()) {
			if(r.col instanceof TaggableColumn) { //no operation
				return (TaggableColumn) r.col;
			}
			return r.col.as(isNull(r.entry.tag) ? r.cd.identity() : r.entry.tag);
		}
		throw cannotEvaluateException("operation", r.entry.next);
	}
	
	public DBOrder evalOrder(TableDecorator td) {
		var r = chainResourceOperations(td);
		if(r.entry.isLast()) { // no order
			return r.col.order();
		}
		var e = r.entry.next;
		if(e.isLast()) { // next must be last
			var o = findEnum(e.value, Order.class);
			if(o.isPresent()) {
				e.requireNoArgs(); //throw exception on valid order
				return r.col.order(o.get());
			}
		}
		throw cannotEvaluateException("order", e);
	}

	public DBFilter evalFilter(TableDecorator td) {
		return evalFilter(td, null);
	}

	public DBFilter evalFilter(TableDecorator td, List<RequestEntryChain> values) {
		var r = chainResourceOperations(td);
		if(r.entry.isLast()) {
			return defaultFilter(r, values);
		}
		var e = r.entry.next;
		if(e.isLast()) {
			if(nonNull(e.args) && !isEmpty(values)) {
				throw new IllegalArgumentException("both");
			}
			else if(isNull(e.args)) {
				e.args = values;
			}
			var o = e.toComparison(td, r.col);
			if(o.isPresent()) {
				return o.get();
			} //eval comparator first to avoid overriding default comparators
			if(r.col instanceof TaggableColumn) {
				var c = r.cd.criteria(e.value); 
				if(nonNull(c)) {
					return r.col.filter(c.build(toStringArray(e.args)));
				}
			}
			throw cannotEvaluateException("comparator|criteria", e);
		}
		if(isEmpty(values)) {
			return chainFilters(td, r.col);
		}
		throw new IllegalArgumentException();
	}

	public Object[] evalFunction(TableDecorator td, String fnName, ParameterSet ps) {
		if(fnName.equals(value)) {
			return requireNoNext().toArgs(td, null, ps, Object[]::new);
		}
		throw cannotEvaluateException(fnName, this);
	}
	
	public Object[] evalArrayFunction(TableDecorator td, String fnName, JqueryType type) {
		if(fnName.equals(value)) {
			var c = type.typeClass();
			if(!c.isArray()) { //basic type
				return toArgs(td, null, ofParameters(required(type), varargs(type)), s-> (Object[]) newInstance(c, s));
			}
			throw new UnsupportedOperationException();
		}
		throw cannotEvaluateException(fnName, this);
	}
	
	static DBFilter defaultFilter(ResourceCursor r, List<RequestEntryChain> values) {
		if(isEmpty(values)) {
			return r.col.isNull();
		}
		if(values.size() > 1) {
			return r.col.in(r.cd.parser(r.td).parseAll(toStringArray(values)));
		}
		Object o;
		try {
			o = values.get(0).evalColumn(r.td);
		}
		catch(Exception e) {
			o = r.cd.parser(r.td).parse(values.get(0).toString());
		}
		return r.col.equal(o);
	}
	
	ResourceCursor chainResourceOperations(TableDecorator td) {
		var r = lookupResource(td);
		var e = r.entry.next;
		while(nonNull(e)) {
			var c = e.toOperation(td, r.col);
			if(c.isEmpty()) {
				break;
			}
			r.col = c.get(); // preserve last non null column
			r.entry = e;
			e = e.next;
		}
		return r;
	}

	Optional<OperationColumn> toOperation(TableDecorator td, DBColumn col) {
		var res = lookupOperator(value);
		return res.map(fn-> {
			var c = col;
			if(isNull(c) && isEmpty(args) && "count".equals(fn.id())) {
				c = b-> {
					b.view(td.table()); // important! register view
					return "*";
				};
			}
			return fillArgs(td, c, fn);
		});
	}
	
	DBFilter chainFilters(TableDecorator td, DBColumn col) {
		var f = toComparison(td, col).orElseThrow(()-> cannotEvaluateException("comparator", this));
		var e = this.next;
		while(nonNull(e)) {
			var op = findEnum(e.value, LogicalOperator.class).orElseThrow(()-> cannotEvaluateException("logical operator", this));
			if(isEmpty(e.args)) {
				throw badArgumentCountException(1, 0);
			}
			if(e.args.size() > 1) {
				throw badArgumentCountException(1, e.args.size());
			}
			f = f.append(op, e.args.get(0).evalFilter(td));				
		}
		return f;
	}

	Optional<DBFilter> toComparison(TableDecorator td, DBObject col) {
		return lookupComparator(value).map(c-> fillArgs(td, col, c));
	}
	
	private ResourceCursor lookupResource(TableDecorator td) {
		if(next() && context().isDeclaredTable(value)) {  //sometimes td.id == cd.id
			var tr = next.lookupViewResource(context().getTable(value));
			if(nonNull(tr)) {
				return tr;
			}
		}
		var tp = lookupViewResource(td);
		if(nonNull(tp)) {
			return tp;
		}
		var op = toOperation(td, null);
		if(op.isPresent()) {
			return new ResourceCursor(td, ofColumn(value, b-> op.get()), this);
		}
		throw cannotEvaluateException("resource", this);
	}

	private ResourceCursor lookupViewResource(TableDecorator td) {
		return context().isDeclaredColumn(value) 
				? new ResourceCursor(td, context().getColumn(requireNoArgs().value), this)
				: lookupWindowFunction(value)  //rank, rowNumber, ..
				.map(fn-> new ResourceCursor(td, ofColumn(value, b-> fillArgs(td, null, fn)), this))
				.orElse(null);
	}

	private DBFilter fillArgs(TableDecorator td, DBObject col, TypedComparator cmp) {
		try {
			return cmp.args(toArgs(td, col, cmp.getParameterSet(), Object[]::new));
		}
		catch(Exception e) {
			for(var ps : cmp.getOverloads()) {
				try {
					return cmp.args(toArgs(td, col, ps, Object[]::new));
				}
				catch(Exception e1) {
					System.out.println(e1);
				}
			}
			throw e;
		}
	}
	
	private OperationColumn fillArgs(TableDecorator td, DBColumn col, TypedOperator opr) {
		return opr.args(toArgs(td, col, opr.getParameterSet(), Object[]::new));
	}
	
	private Object[] toArgs(TableDecorator td, DBObject col, ParameterSet ps, IntFunction<Object[]> arrFn) {
		int inc = nonNull(col) ? 1 : 0;
		var arr = arrFn.apply(isNull(args) ? inc : args.size() + inc);
		if(nonNull(col)) {
			arr[0] = col;
		}
		ps.forEach(arr.length, (p,i)-> {
			if(i>=inc) { //arg0 already parsed
				var e = args.get(i-inc);
				arr[i] = isNull(e.value) || e.text
						? e.requireNoArgs().value
						: parse(e, td, p.types(arr));
			}
		});
		return arr;
	}
	
	RequestEntryChain requireNoArgs() {
		if(isNull(args)) {
			return this;
		}
		throw new IllegalArgumentException(value + " takes no args");
	}
	
	RequestEntryChain requireNoNext(){
		if(isLast()) {
			return this;
		}
		throw new IllegalArgumentException(value + " must be the last entry");
	}
	
	public boolean isLast() {
		return isNull(next);
	}

	public boolean next() {
		return nonNull(next);
	}
	
	@Override
	public String toString() {
		var s = ""; // null == EMPTY
		if(nonNull(value)) {
			s += text ? doubleQuote(value) : value;
		}
		if(nonNull(args)){
			s += args.stream().map(RequestEntryChain::toString).collect(joining(",", "(", ")"));
		}
		if(nonNull(next)) {
			s += "." + next.toString();
		}
		return isNull(tag) ? s : s + ":" + tag;
	}
	
	static String[] toStringArray(List<RequestEntryChain> entries) {
		return entries.stream()
				.map(e-> isNull(e.value) ? null : e.toString())
				.toArray(String[]::new);
	}

	static EvalException cannotEvaluateException(String type, RequestEntryChain entry) {
		return EvalException.cannotEvaluateException(type, entry.toString());
	}
	
	@RequiredArgsConstructor
	static final class ResourceCursor {
		
		private final TableDecorator td;
		private final ColumnDecorator cd;
		private RequestEntryChain entry;
		private DBColumn col;
		
		public ResourceCursor(TableDecorator td, ColumnDecorator cd, RequestEntryChain entry) {
			this.td = td;
			this.cd = cd;
			this.entry = entry;
			this.col = td.column(cd);
		}
		
		@Override
		public String toString() {
			return td + "." + cd + " => " + entry.toString();
		}
	}
}