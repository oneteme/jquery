package org.usf.jquery.web;

import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Operator.lookupNoArgFunction;
import static org.usf.jquery.core.Operator.lookupOperator;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Validation.VAR_PATTERN;
import static org.usf.jquery.web.ArgumentParsers.javaTypeParser;
import static org.usf.jquery.web.JQueryContext.context;
import static org.usf.jquery.web.ParseException.cannotEvaluateException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.JavaType;
import org.usf.jquery.core.Operator;
import org.usf.jquery.core.Order;
import org.usf.jquery.core.TaggableColumn;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter(value = AccessLevel.PACKAGE)
@RequiredArgsConstructor
final class RequestEntry {

	private final String value;
	private final boolean text; //"string"
	private RequestEntry next;
	private List<RequestEntry> args;
	private String tag;

	public RequestEntry(String value) {
		this(value, false);
	}
	
	public DBOrder asOrder(TableDecorator td) {
		var t = toColumn(td);
		var c = t.td.column(t.tc); 
		return isNull(t.entry.next) ? c.order() : t.entry.next.chainOrder(td, c);
	}

	private DBOrder chainOrder(TableDecorator td, DBColumn col) {
		var c = toOperation(td, col);
		if(nonNull(c)) {
			return isNull(next) ? c.order() : next.chainOrder(td, c);
		}
		if(isNull(next)) { //last entry
			var upVal = value.toUpperCase();
			var order = Stream.of(Order.values()).filter(o-> o.name().equals(upVal)).findAny();
			if(order.isPresent()) {
				return col.order(order.get());
			}
		}
		throw cannotEvaluateException("entry", value); //column expected
	}
	
	public TaggableColumn asFilter(TableDecorator td, List<RequestEntry> values) {
		
		return null;
	}

	public TaggableColumn asColumn(TableDecorator td) { //columnName == viewName
		var t = toColumn(td);
		var c = t.td.column(t.tc); 
		return isNull(t.entry.next) ? c : t.entry.next.chainColumn(td, c, c.tagname());
	}
	
	private TaggableColumn chainColumn(TableDecorator td, DBColumn col, String alias) {
		var c = toOperation(td, col);
		if(nonNull(c)) {
			return nonNull(next) 
					? next.chainColumn(td, c, alias) 
					: c.as(isNull(tag) ? alias : tag);
		}
		throw cannotEvaluateException("entry", value); //column expected
	}
	
	private Triple toColumn(TableDecorator td) { //columnName == viewName
		requireNoArgs();
		if(next != null && context().isDeclaredTable(value) && context().isDeclaredColumn(next.value)) {
			return new Triple(
					context().getTable(value), 
					context().getColumn(next.requireNoArgs().value), 
					next);
		}
		if(context().isDeclaredColumn(value)) {
			return new Triple(td, context().getColumn(value), this);
		}
		throw cannotEvaluateException("column expression", value);
	}
	
	private DBOrder chainExpression(TableDecorator td, DBColumn col) {
		var c = toOperation(td, col);
		if(nonNull(c)) {
			return isNull(next) ? c.order() : next.chainOrder(td, c);
		}
		if(isNull(next)) {
			var order = Stream.of(Order.values()).filter(o-> o.name().equalsIgnoreCase(value)).findAny();
			if(order.isPresent()) {
				return col.order(order.get());
			}
		}
		throw cannotEvaluateException("entry", value); //column expected
	
	}
	
	private DBColumn toOperation(TableDecorator td, DBColumn prev) {
		var res = lookupOperator(value);
		if(res.isEmpty()) {
			return null;
		}
		var op = res.get();
		var min = op.requireArgCount();
		var np = args.size()+1;
		if(np < min || (!op.isVarags() && np > op.getParameters().length)) {
			throw new IllegalArgumentException();
		}
		var params = new ArrayList<Object>(np);
		params.add(prev);
		var i=1;
		for(; i<min; i++) {
			params.add(args.get(i-1).toArg(td, op.getParameters()[i].getTypes()));
		}
		for(; i<min(np, op.getParameters().length); i++) {
			params.add(args.get(i-1).toArg(td, op.getParameters()[i].getTypes()));
		}
		if(op.isVarags()) {
			var types = op.getParameters()[op.getParameters().length-1].getTypes(); 
			for(; i<np; i++) {
				params.add(args.get(i-1).toArg(td, types));
			}
		}
		return op.args(params.toArray());
	}
	
	private Object toArg(TableDecorator td, JavaType... types) {
		if(isNull(value) || text) {
			return requireNoArgs().value;
		}
		if(value.matches(VAR_PATTERN)) {
			var c = toColumn(td);
			if(nonNull(c)) {
				return c; // type will be checked later
			}
		}
		requireNoArgs();
		for(var t : types) {
			var o = javaTypeParser(t).tryParse(value);
			if(nonNull(o)) {
				return o;
			}
		}
		throw new ParseException("cannot parse value : " + value);
	}
	
	private RequestEntry requireNoArgs() {
		if(args == null) {
			return this;
		}
		throw new IllegalArgumentException(value + " takes no args");
	}
	
	@Override
	public String toString() {
		var s = "";
		if(value != null) {
			s += text ? doubleQuote(value) : value;
		}
		if(args != null){
			s += args.stream().map(RequestEntry::toString).collect(joining(",", "(", ")"));
		}
		if(next != null) {
			s += "." + next.toString();
		}
		return tag == null ? s : s + ":" + tag;
	}
	
	@RequiredArgsConstructor
	static class Triple {
		
		private final TableDecorator td;
		private final ColumnDecorator tc;
		private final RequestEntry entry;
		
	}
}