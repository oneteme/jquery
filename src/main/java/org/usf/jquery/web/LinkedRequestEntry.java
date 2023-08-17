package org.usf.jquery.web;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.regex.Pattern.compile;
import static org.usf.jquery.core.DBFunction.lookupFunction;
import static org.usf.jquery.core.DBFunction.over;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.isBlank;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.web.ArgumentParser.tryParse;
import static org.usf.jquery.web.ColumnDecorator.countColumn;
import static org.usf.jquery.web.Constants.ORDER;
import static org.usf.jquery.web.Constants.PARTITION;
import static org.usf.jquery.web.JQueryContext.context;
import static org.usf.jquery.web.ParseException.cannotEvaluateException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBFunction;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.OverClause;
import org.usf.jquery.core.TypedFunction;
import org.usf.jquery.core.WindowFunction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public final class LinkedRequestEntry {

	private static final String ARRAY_ARGS_KEY = "$args";
	
	private String tag; //optional
	private final LinkedList<RequestEntry> entries = new LinkedList<>();

	public void appendEntry(String name) {
		entries.add(new RequestEntry(name));
	}
	
	public void appendEntryArgs(Map<String, List<String>> args) {
		entries.getLast().args = args;
	}
	
	public RequestColumn toRequestColumn(TableDecorator defaultTable, boolean allowedExp) {
		if(entries.isEmpty()) {
			throw new IllegalArgumentException("empty");
		}
		else if(entries.size() == 1) {
			var cd = requireColumn(entries.getFirst());
			return new RequestColumn(defaultTable, cd, null, tag);
		}
		else {
			var arr = new ArrayList<>(entries); //improve indexing
			ColumnDecorator cd = null;
			String exp = null;
			int idx = 1;
			try {
				cd = requireColumn(arr.get(idx));
				defaultTable = context().getTable(arr.get(idx-1).requireFiedName()); //parent
			}
			catch (Exception e) {
				cd = requireColumn(arr.get(--idx));
			}
			var limit = allowedExp ? arr.size() - 1 : arr.size();
			var fn = new LinkedList<DBFunction>();
			for(var i=++idx; i<limit; i++) {
				fn.add(requireFunction(arr.get(i), defaultTable));
			}
			if(limit < arr.size()) {
				try {
					fn.add(requireFunction(arr.get(limit), defaultTable));
				}
				catch(Exception e) {
					exp = arr.get(limit).getName();
				}
			}
			var rc = new RequestColumn(defaultTable, cd, exp, tag);
			fn.forEach(f-> rc.append((TypedFunction) f));
			return rc;
		}
	}
	
	@Override
	public String toString() {
		return isNull(tag) ? entries.toString() : entries.toString() + ":" + tag;
	}

	/**
	 * <ol>
	 * 	<li> column decorator </li>
	 * 	<li> count function	  </li>
	 * 	<li> window functions </li>
	 * </ol>
	 */
	private static ColumnDecorator requireColumn(RequestEntry re) {
		if(context().isDeclaredColumn(re.getName())) {
			return context().getColumn(re.requireFiedName());
		}
		if("count".equals(re.requireNoArgFunction())) { // not arguments
			return countColumn();
		}
		var fn = lookupFunction(snakeToCamelCase(re.getName()));
		if(fn.isPresent() && fn.get().functionType() == WindowFunction.class) {
			return ColumnDecorator.of(re.requireNoArgFunction().toLowerCase(), fn.get()::args); 
		}
		throw cannotEvaluateException("column expression", re.getName()); //column expected
	}

	/**
	 * <ol>
	 * 	<li> over function</li>
	 * 	<li> std functions </li>
	 * </ol>
	 */
	private static DBFunction requireFunction(RequestEntry re, TableDecorator td) {
		if("over".equals(re.getName())) {
			if(re.hasArgMap()) {
				return overFunction(td, re.getArgs()); //use named arguments
			}
			throw new UnsupportedOperationException("window functions require named args");
		}
		var fn = lookupFunction(re.getName())
				.orElseThrow(()-> cannotEvaluateException("function expression", re.getName()));
		var args = re.arrayArgs();
		if(args.isEmpty()) {
			return fn;
		}
		if(re.hasArgArray()) {
			return fn.usingArgs(re.hasArgArray() ? re.arrayArgs().stream().map(o-> parseEntry(o, td)).toArray() : null);
		}
		throw new UnsupportedOperationException("functions does not support named args");
	}

	private static TypedFunction overFunction(TableDecorator td, Map<String, List<String>> args) {
		var clause = new OverClause();
		if(args.containsKey(PARTITION)) {
			clause.partitions(args.get(PARTITION).stream()
					.map(LinkedRequestEntry::linkedEntry)
					.map(o-> o.toRequestColumn(td, false))
					.map(RequestColumn::dbColumn)
					.toArray(DBColumn[]::new));
		}
		if(args.containsKey(ORDER)) {
			clause.orders(args.get(ORDER).stream()
					.map(LinkedRequestEntry::linkedEntry)
					.map(o-> o.toRequestColumn(td, true)) //ASC | DESC
					.map(RequestColumn::dbOrder)
					.toArray(DBOrder[]::new));
		}
		return over().usingArgs(clause);
	}
	
	private static Object parseEntry(String s, TableDecorator td){
		try {
			return linkedEntry(s).toRequestColumn(td, false).dbColumn();
		}
		catch(Exception e) {
			return tryParse(s);
		}
	}

	private static LinkedRequestEntry linkedEntry(String s) {
		var arr = parseEntries(s);
		if(arr.size() == 1) {
			return arr.get(0);
		}
		throw new IllegalStateException("expect one entry");
	}
	
	public static List<LinkedRequestEntry> parseEntries(String s) {
		if(isBlank(s)) {
			return emptyList();
		}
		var res = new LinkedList<LinkedRequestEntry>();
		res.add(new LinkedRequestEntry());
		int from = 0;
		int to = 0;
		char c = 0;
		for(;;) {
			while(to < s.length() && legalVariableChar(c = s.charAt(to))) to++;
			res.getLast().appendEntry(requireLegalVariable(s.substring(from, to)));
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
					res.getLast().appendEntryArgs(parseArgs(s.substring(to, jmp)));
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
				var jmp = s.indexOf(',', ++to);
				if(jmp == -1) {
					jmp = s.length();
				}
				res.getLast().tag = requireLegalVariable(s.substring(to, jmp));
				if((to = jmp) == s.length()) {
					break;
				}
				c = s.charAt(to);
			}
			if(c == ',') {
				res.add(new LinkedRequestEntry());
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
	
	private static final String ARG_PATTERN = "\\w+(\\.\\w+)*";
	
	private static Map<String, List<String>> parseArgs(String s) {
		if(isBlank(s)) {
			return emptyMap();
		}
		if(!s.contains(":")) {
			if(s.matches(wholeWord(ARG_PATTERN + "(," + ARG_PATTERN + ")*"))) {
				return Map.of(ARRAY_ARGS_KEY, asList(s.split(",")));
			}
			throw new IllegalArgumentException(quote(s) + " args format not valid");
		}
		int from = 0;
		int to = 0;
		char c = 0;
		var map = new LinkedHashMap<String, List<String>>();
		List<String> last = null;
		String key = null;
		for(;;) {
			while(to < s.length() && legalArgChar(c = s.charAt(to))) to++;
			var sub = s.substring(from, to);
			if(!sub.matches(wholeWord(ARG_PATTERN))) {
				throw new IllegalArgumentException("illegal arg value : " + sub);	
			}
			if(to == s.length()) {
				last.add(sub); //sonar dummy sequential analyze !?
				break;
			}
			if(c == ':') {
				last = map.computeIfAbsent((key = sub), k-> new LinkedList<>());
			}
			else if(c == ',') {
				if(key == null) {
					throw new IllegalArgumentException("arg name expected at index=" + to);	
				}
				last.add(sub);
			}
			else {
				throw new IllegalArgumentException("'" + s.charAt(to) + "' not valid at index=" + to);
			}
			from = ++to;
		}
		return map;
	}

	private static boolean legalArgChar(char c) { //later next version
		return legalVariableChar(c) || c == '.'; 
	}
	
	private static boolean legalVariableChar(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z' ) || (c >= '0' && c <= '9') || c == '_';
	}
	
	private static String wholeWord(String rex) {
		return "^" + rex + "$";
	}

	@Getter
	@RequiredArgsConstructor
	static final class RequestEntry {
		
		private final String name;
		private Map<String, List<String>> args;
		
		public String requireFiedName() {
			if(isNull(args)) { //no parentheses 
				return name;
			}
			throw new IllegalArgumentException(quote(name) + " field takes no args");
		}
		
		public String requireNoArgFunction() {
			if(isNull(args) || args.isEmpty()) { //no arguments 
				return name;
			}
			throw new IllegalArgumentException(quote(name) + " function takes no args");
		}
		
		public List<String> arrayArgs() {
			return hasArgArray() ? args.get(ARRAY_ARGS_KEY) : emptyList();
		}

		public boolean hasArgs() {
			return nonNull(args);
		}
		
		public boolean hasArgArray() {
			return hasArgs() && args.containsKey(ARRAY_ARGS_KEY);
		}
		
		public boolean hasArgMap() {
			return hasArgs() && !args.containsKey(ARRAY_ARGS_KEY);
		}
		
		public int argCount() {
			return hasArgs() ? args.size() : 0;
		}
		
		@Override
		public String toString() {
			return hasArgs() ? name + "(" + args + ")" :  name; 
		}
	}
	
	static String snakeToCamelCase(String fn) {
		StringBuffer sb = new StringBuffer(fn.length());
	    Matcher m = compile("_[a-z]").matcher(fn);
	    while (m.find()) {
	        m.appendReplacement(sb, m.group().substring(1).toUpperCase());
	    }
	    return m.appendTail(sb).toString();
	}
}