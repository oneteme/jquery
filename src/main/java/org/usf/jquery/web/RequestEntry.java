package org.usf.jquery.web;

import static java.lang.Math.min;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.DBColumn.lookupColumnFunction;
import static org.usf.jquery.core.Operator.lookupOperator;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.core.Validation.VAR_PATTERN;
import static org.usf.jquery.web.ArgumentParsers.javaTypeParser;
import static org.usf.jquery.web.JQueryContext.context;

import java.util.ArrayList;
import java.util.List;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.JavaType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
final class RequestEntry {

	private final String value;
	private final boolean unparsable; //"string"
	private RequestEntry next;
	private List<RequestEntry> args;
	private String tag;

	public RequestEntry(String value) {
		this(value, false);
	}
	
	public DBColumn toColumn(TableDecorator td) { //columnName == viewName
		DBColumn c = null;
		RequestEntry nxt = requireNoArgs();
		if(next != null && context().isDeclaredTable(value) && context().isDeclaredColumn(next.value)) {
			nxt = next.requireNoArgs();
			c = context().getTable(value).column(context().getColumn(nxt.value));
		}
		else if(context().isDeclaredColumn(value)) {
			c = td.column(context().getColumn(value));
		}
		else {
			c = lookupColumnFunction().orElse(null);
		}
		return c == null ? null : nxt.next(td, c);
	}
	
	private Object toArg(TableDecorator td, JavaType... types) {
		if(value == null) {
			return requireNoArgs().value;
		}
		if(value.matches(VAR_PATTERN)) {
			var c = toColumn(td);
			if(c != null) {
				return c; // type will be checked later
			}
		}
		if(!unparsable) {
			requireNoArgs();
			for(var t : types) {
				var o = javaTypeParser(t).tryParse(value);
				if(o != null) {
					return o;
				}
			}
			throw new ParseException("cannot parse value : " + value);
		}
		for(var t : types) {
			if(t.accept(value)) {
				return value;
			}
		}
		throw new ParseException("illegal value : " + value);
	}
	
	DBColumn toOperation(TableDecorator td, DBColumn prev) {
		var res = lookupOperator(value);
		if(res.isPresent()) {
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
			return next(td, op.args(params.toArray()));
		}
		return null;
	}
	
	private DBColumn next(TableDecorator td, DBColumn col) {
		if(next == null) {
			return tag == null ? col : col.as(tag);
		}
		return next.toOperation(td, col);
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
			s += unparsable ? doubleQuote(value) : value;
		}
		if(args != null){
			s += args.stream().map(RequestEntry::toString).collect(joining(",", "(", ")"));
		}
		if(next != null) {
			s += "." + next.toString();
		}
		return tag == null ? s : s + ":" + tag;
	}
}