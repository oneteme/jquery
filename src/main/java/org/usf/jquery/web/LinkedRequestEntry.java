package org.usf.jquery.web;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.regex.Pattern.compile;
import static org.usf.jquery.core.DBFunction.lookupFunction;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Utils.isBlank;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireLegalVariable;
import static org.usf.jquery.web.ColumnDecorator.countColumn;
import static org.usf.jquery.web.ColumnDecorator.ofColumn;
import static org.usf.jquery.web.Constants.ORDER;
import static org.usf.jquery.web.Constants.PARTITION;
import static org.usf.jquery.web.JQueryContext.context;
import static org.usf.jquery.web.ParsableJDBCType.typeOf;
import static org.usf.jquery.web.ParseException.cannotEvaluateException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.usf.jquery.core.DBColumn;
import org.usf.jquery.core.DBOrder;
import org.usf.jquery.core.OverClause;
import org.usf.jquery.core.TypedFunction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
/**
 * 
 * <code>[table.]column[.function([arg1][,arg2]*)]*[.comparator|criteria|order][:alias]</code>
 * 
 * @author u$f
 * 
 * 
 */
public final class LinkedRequestEntry {

	private static final String ARRAY_ARGS_KEY = "$args";
	
	private final LinkedList<RequestEntry> entries = new LinkedList<>();
	private String tag; //optional

	public void appendEntry(String name) {
		entries.add(new RequestEntry(name));
	}
	
	public void appendEntryArgs(Map<String, List<String>> args) {
		entries.getLast().args = args;
	}
	
	public RequestColumn toRequestColumn(TableDecorator defaultTable, boolean allowedExp) { //predicate
		if(entries.isEmpty()) {
			throw new IllegalArgumentException("empty");
		}
		else if(entries.size() == 1) {
			var cd = requireColumn(entries.getFirst());
			return new RequestColumn(defaultTable, cd, emptyList(), null, tag);
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
			var fn = new LinkedList<TypedFunction>();
			for(++idx; idx<limit; idx++) {
				fn.add(requireFunction(arr.get(idx), defaultTable));
			}
			if(idx < arr.size()) {
				try {
					fn.add(requireFunction(arr.get(idx), defaultTable));
				}
				catch(Exception e) {
					exp = arr.get(limit).requireFiedName(); // comparator | expression | order
				}
			}
			return new RequestColumn(defaultTable, cd, fn, exp, tag);
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
		if(fn.filter(TypedFunction::isWindowFunction).isPresent()) {
			return ofColumn(re.requireNoArgFunction().toLowerCase(), t-> fn.get().args()); 
		}
		throw cannotEvaluateException("column expression", re.toString()); //column expected
	}

	/**
	 * <ol>
	 * 	<li> std functions </li>
	 * 	<li> over function</li>
	 * </ol>
	 */
	private static TypedFunction requireFunction(RequestEntry re, TableDecorator td) {
		var fn = lookupFunction(re.getName())
				.orElseThrow(()-> cannotEvaluateException("function", re.getName()));
		if("OVER".equals(fn.name())) {
			var args = re.getArgs();
			if(isNull(args) || !args.containsKey(ARRAY_ARGS_KEY)) { //named arguments function
				return fn.additionalArgs(overClause(td, isNull(args) ? emptyMap() : args));
			}
			throw new UnsupportedOperationException("over function require named args");
		}
		//array arguments functions
		parseEntry(fn, re, td);
		return fn;
	}

	private static void parseEntry(TypedFunction fn, RequestEntry re, TableDecorator td){
		if(!isEmpty(re.getArgs()) && !re.getArgs().containsKey(ARRAY_ARGS_KEY)) {
			throw new UnsupportedOperationException("functions does not support named args");
		}
		if(fn.argumentCount() <= 1) { //1st argument ignored
			if(!isEmpty(re.getArgs())) {
				throw new IllegalArgumentException(fn.name() + " takes no arguments");
			}
		}
		else {
			var n = fn.argumentCount()-1;
			if(nonNull(re.getArgs()) 
					&& re.getArgs().containsKey(ARRAY_ARGS_KEY) 
					&& re.getArgs().get(ARRAY_ARGS_KEY).size() == n) {
				var params = re.getArgs().get(ARRAY_ARGS_KEY);
				var args = new LinkedList<Object>();
				for(int i=1; i<fn.getArgTypes().length; i++) { //skip first argument
					var s = params.get(i-1);
					try {
						args.add(typeOf(fn.getArgTypes()[i]).parse(s));
					}
					catch (Exception e) {
						try {
							args.add(parseSingleLinkedEntry(s).toRequestColumn(td, false).toColumn());
						}
						catch(Exception e2) {
							throw e; //first exception
						}
					}
				}
				fn.additionalArgs(args.toArray());
			}
			else {
				throw new IllegalArgumentException(fn.name() + " takes " + n + " argument(s)");
			}
		}
	}

	private static OverClause overClause(TableDecorator td, Map<String, List<String>> args) {
		var clause = new OverClause();
		if(args.containsKey(PARTITION)) {
			clause.partitions(args.get(PARTITION).stream()
					.map(LinkedRequestEntry::parseSingleLinkedEntry)
					.map(o-> o.toRequestColumn(td, false))
					.map(RequestColumn::toColumn)
					.toArray(DBColumn[]::new));
		}
		if(args.containsKey(ORDER)) {
			clause.orders(args.get(ORDER).stream()
					.map(LinkedRequestEntry::parseSingleLinkedEntry)
					.map(o-> o.toRequestColumn(td, true)) //ASC | DESC
					.map(RequestColumn::toOrder)
					.toArray(DBOrder[]::new));
		}
		return clause;
	}
	
	static LinkedRequestEntry parseSingleLinkedEntry(String s) {
		return parseLinkedEntries(s, false).get(0);
	}
	
	static List<LinkedRequestEntry> parseLinkedEntries(String s) {
		return parseLinkedEntries(s, true);
	}
	
	private static List<LinkedRequestEntry> parseLinkedEntries(String s, boolean multiple) {
		if(isBlank(s)) {
			throw new IllegalArgumentException("empty");
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
					throw new IllegalArgumentException("')' expected after index=" + --to);	
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
			if(c == ',' && multiple) {
				res.add(new LinkedRequestEntry());
			}
			else if(c != '.') {
				throw new IllegalArgumentException("'" + s.charAt(to) + "' not valid at index=" + to);
			}
			if((from = ++to) == s.length()) {
				throw new IllegalArgumentException("'" + s.charAt(to-1) + "' not allowed at the end");
			}
		}
		return res;
	}
	
	private static final String ARG_PATTERN = "\\w+(\\.\\w*)*";
	
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
			if(c == ':') { //!!!!! timestamp argument 
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
		
		@Override
		public String toString() {
			if(isNull(args)) {
				return name;
			}
			var s =  name + "(";
			if(!args.isEmpty()) {
				s+= args.containsKey(ARRAY_ARGS_KEY) ? join(", ", args.get(ARRAY_ARGS_KEY)) : args;
			}
			return s + ")";
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