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
import org.usf.jquery.core.DBFilter;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.JavaType;
import org.usf.jquery.core.OperationColumn;
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
	
	private static final ColumnDecorator DEFAUL_COLUMN = ()-> null; //unused identity

	private final String value;
	private final boolean text; //"string"
	private RequestEntry next;
	private List<RequestEntry> args;
	private String tag;

	public RequestEntry(String value) {
		this(value, false);
	}

	public TaggableColumn asColumn(TableDecorator td) { //columnName == viewName
		var t = lookup(td, true);
		var c = t.buildColumn();
		var e = t.entry;
		DBColumn oc = c;
		while(e.next()) {
			e = e.next;
			oc = e.toOperation(td, oc);
			if(isNull(oc)) {
				throw cannotEvaluateException("entry", e.value); //column expected
			}
		}
		return oc.as(isNull(e.tag) ? c.tagname() : e.tag);
	}

	public DBFilter asFilter(TableDecorator td, String[] values) {
		var t = lookup(td, false);
		var c = t.buildColumn();
		var e = t.entry.next;
		DBColumn oc = c;
		while(nonNull(e)) {
			var op = e.toOperation(td, oc);
			if(isNull(oc)) {
				break;
			}
			else {
				oc = op; //preserve last non value
			}
			e = e.next;
		}
		var cd = c == oc ? t.cd : DEFAUL_COLUMN; //no operation
		if(isNull(e)) { // no expression
			return oc.filter(cd.expression(null, values));
		}
		else if(e.isLast()) {
			return oc.filter(cd.expression(e.value, values));
		}
		throw cannotEvaluateException("entry", e.toString()); //more detail
	}
	
	public DBOrder asOrder(TableDecorator td) {
		var t = lookup(td, false);
		var c = t.buildColumn();
		var e = t.entry.next;
		DBColumn oc = c;
		while(nonNull(e)) {
			var op = e.toOperation(td, c);
			if(isNull(op)) {
				break;
			}
			else {
				oc = op; //preserve last non value
			}
			e = e.next;
		}
		if(isNull(e)) { // no expression
			return c.order();
		}
		else if(e.isLast()) { //last entry
			var upVal = e.value.toUpperCase();
			var order = Stream.of(Order.values()).filter(o-> o.name().equals(upVal)).findAny();
			if(order.isPresent()) {
				return oc.order(order.get());
			}
		}
		throw cannotEvaluateException("entry", e.toString()); //column expected
	}
	
	private OperationColumn toOperation(TableDecorator td, DBColumn col) {
		var res = lookupOperator(value);
		if(res.isEmpty()) {
			return null;
		}
		var op = res.get();
		var min = op.requireArgCount();
		var np = args.size()+1; // col
		if(np < min || (!op.isVarags() && np > op.getParameters().length)) {
			throw new IllegalArgumentException();
		}
		var params = new ArrayList<Object>(np);
		params.add(col);
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
			var c = lookup(td, true);
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
	
	private Triple lookup(TableDecorator td, boolean noArgFn) { //columnName == viewName
		if(next() && context().isDeclaredTable(value) && context().isDeclaredColumn(next.value)) {
			return new Triple(
					context().getTable(requireNoArgs().value), 
					context().getColumn(next.requireNoArgs().value), 
					next, null);
		}
		if(context().isDeclaredColumn(value)) {
			return new Triple(td, context().getColumn(requireNoArgs().value), this, null);
		}
		if(noArgFn) {
			var res = lookupNoArgFunction(value);
			if(res.isPresent()) {
				var op = res.get(); 
				if(isNull(args) || args.isEmpty()) {
					return new Triple(null, null, this, op.args().as(op.id()));
				}
				throw new IllegalArgumentException(op.id() + " takes no arguments");
			}
		}
		throw cannotEvaluateException("column expression", value);
	}
	
	private RequestEntry requireNoArgs() {
		if(args == null) {
			return this;
		}
		throw new IllegalArgumentException(value + " takes no args");
	}
	
	public boolean isLast() {
		return isNull(next);
	}

	public boolean next() {
		return nonNull(next);
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
		private final ColumnDecorator cd;
		private final RequestEntry entry;
		private final TaggableColumn column;
		
		TaggableColumn buildColumn() {
			return isNull(column) ? td.column(cd) : column;
		}
	}
}