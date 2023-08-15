package org.usf.jquery.web;

import static java.lang.String.join;
import static java.util.Arrays.copyOfRange;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;
import static org.usf.jquery.core.DBColumn.count;
import static org.usf.jquery.core.DBFunction.lookup;
import static org.usf.jquery.core.Utils.AUTO_TYPE;
import static org.usf.jquery.core.Utils.isBlank;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.VARIABLE_PATTERN;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.web.JQueryContext.context;
import static org.usf.jquery.web.ParseException.cannotEvaluateException;
import static org.usf.jquery.web.RequestColumn.RequestEntry.ressource;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.TypedFunction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * 
 * <code>[table.]column[.function]*[.comparator|order][:alias]</code>
 * 
 * @author u$f
 * 
 * @see RequestFilter
 * 
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestColumn {
	
	private final TableDecorator td;
	private final ColumnDecorator cd;
	private final List<TypedFunction> fns = new LinkedList<>();
	private final String exp;
	private final String tag;
	
	private static final Predicate<String> EXPRESSION_MATCHER = 
			compile(VARIABLE_PATTERN + "(\\." + VARIABLE_PATTERN +")*(\\:" + VARIABLE_PATTERN + ")?").asMatchPredicate();
	
	private static final ColumnDecorator countColumn = new ColumnDecorator() {
		@Override
		public String identity() {
			return null; //unused
		}
		@Override
		public String reference() {
			return "count";
		}
		@Override
		public ColumnBuilder builder() {
			return t-> count();
		}
	};

	public TableDecorator tableDecorator() {
		return td;
	}
	
	public ColumnDecorator columnDecorator() {
		return fns.isEmpty() && isBlank(tag) ? cd : wrapColumn();
	}
	
	public String expression() {
		return exp;
	}

	private RequestColumn append(TypedFunction fn) {
		fns.add(fn);
		return this;
	}

	static RequestColumn decodeColumn(String value, TableDecorator defaultTable, boolean allowedExp) {
		if(!EXPRESSION_MATCHER.test(value)) {
			throw cannotEvaluateException("column expression", value);
		}
		String tag = null;
		int index = value.lastIndexOf(':');
		if(index > -1) {
			tag = value.substring(index+1);
			value = value.substring(0, index);
		}
		String exp = null;
		var arr  = value.split("\\.");
		int from = arr.length-1;
		if(arr.length > 1 && allowedExp && !context().isDeclaredColumn(arr[arr.length-1]) && lookup(arr[arr.length-1]).isEmpty()) {//!function && !column
			exp  = arr[arr.length-1];
			from = arr.length-2;
		}
		return decode(arr, from, defaultTable, exp, tag);
	}
	
	private static RequestColumn decode(String[] arr, int index, TableDecorator defaultTable, String exp, String tag) {
		var value = arr[index];
		if(context().isDeclaredColumn(value)) {//column found => break recursive call
			var cd = context().getColumn(value);
			if(index > 1) {
				throw cannotEvaluateException("column prefix", join(".", copyOfRange(arr, 0, index))); //too many prefix
			}
			var td = index == 0 ? defaultTable : context().getTable(arr[0]);
			return new RequestColumn(td, cd, exp, tag);
		}
		if(index == 0) {
			if("count".equals(arr[index])) {
				return new RequestColumn(defaultTable, countColumn, exp, tag);
			}
			throw cannotEvaluateException("column expression", value); //column expected
		}
		var fn = lookup(value).orElseThrow(()-> cannotEvaluateException("column expression", value)); //function expected
		return decode(arr, --index, defaultTable, exp, tag).append(fn);
	}
	
	private ColumnDecorator wrapColumn() {
		
		return new ColumnDecorator() {
			
			@Override
			public String identity() {
				return null; //unused
			}
			
			@Override
			public String reference() {
				return ofNullable(tag).orElseGet(cd::reference); //join function !?
			}
			
			@Override
			public ColumnBuilder builder() { //logical column
				return t-> {
					DBColumn col = t.column(cd);
					return fns.stream() //TD check types
							.reduce(col, (c, fn)-> fn.args(c), (c1,c2)-> c1) //sequentially
							.as(reference());
				};
			}
			
			@Override
			public int dataType() {
				var i = fns.size();
				while(--i>=0 && fns.get(i).getReturnedType() == AUTO_TYPE);
				return i<0 ? cd.dataType() : fns.get(i).getReturnedType();
			}
		};
	}
	
	private static List<ResourceList> parseRequestColumn(String s) {
		if(isBlank(s)) {
			return emptyList();
		}
		var res = new LinkedList<ResourceList>();
		res.add(new ResourceList());
		int from = 0;
		int to = 0;
		char c = 0;
		for(;;) {
			while(to < s.length() && legalVariableChar(c = s.charAt(to))) to++;
			res.getLast().add(ressource(s.substring(from, to)));
			if(to == s.length()) {
				break;
			}
			if(c == '(') {
				var jmp = s.indexOf(')', ++to);
				if(jmp > -1) {
					var nest = s.indexOf('(', to);
					if(nest > -1 && nest < jmp) { //avoid (..(..)
						throw new IllegalArgumentException("'(' not allowed at index=" + nest);
					}
					res.getLast().getList().getLast().setArgs(s.substring(to, jmp).split(",")); //split later !?
				}
				else {
					throw new IllegalArgumentException("')' expected after index=" + jmp);	
				}
				if((to = ++jmp) == s.length()) {
					break;
				}
				c = s.charAt(to);
			}
			if(c == ':') {
				var jmp = s.indexOf(',', to+1);
				if(jmp == -1) {
					jmp = s.length();
				}
				res.getLast().setTag(s.substring(to, jmp));
				if((to = jmp) == s.length()) {
					break;
				}
				c = s.charAt(to);
			}
			if(c == ',') {
				res.add(new ResourceList());
			}
			else if(c != '.') {
				throw new IllegalArgumentException("'" + s.charAt(to) + "' not valid at index=" + to);
			}
			if((from = ++to) == s.length()) {
				throw new IllegalArgumentException("'" + s.charAt(to-1) + "' not allowed at the end");
			}
		}
		System.out.println(s+ " => " + res);
		return res;
	}
	
	private static boolean legalVariableChar(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z' ) || (c >= '0' && c <= '9') || c == '_';
	}
	
	
	@Setter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	static final class RequestEntry {
		
		private final String name;
		private String[] args;
		
		public static RequestEntry ressource(String name) {
			return new RequestEntry(requireLegalVariable(name));
		}
		
		@Override
		public String toString() {
			return isEmpty(args) ? name : name + "(" + join(", ", args) + ")"; 
		}
	}

	@Setter
	@Getter
	static final class ResourceList {
		
		private String tag;
		private final LinkedList<RequestEntry> list = new LinkedList<>();

		public void add(RequestEntry res) {
			list.add(res);
		}
		
		@Override
		public String toString() {
			return isNull(tag) ? list.toString() : list.toString() + ":" + tag;
		}
		
	}
	
}