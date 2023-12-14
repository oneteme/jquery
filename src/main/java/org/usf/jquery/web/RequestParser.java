package org.usf.jquery.web;

import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Validation.VAR_PATTERN;
import static org.usf.jquery.core.Validation.requireLegalVariable;

import java.util.LinkedList;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * @author u$f
 *
 */
public final class RequestParser {

	private final String s;
	private int idx;
	private int size;
	private char c;
	
	private RequestParser(String s) {
		this.s = s;
		size = s.length();
	}

	public static RequestEntry parse(String s) {
		return new RequestParser(s).parseEntry(false, true);
	}

	private RequestEntry parseEntry(boolean argument, boolean tag) {
		var entry = new RequestEntry(argument
				? jmpVal() //null value
				: requireLegalVariable(jmpVar(), v-> "illegal variable name : '" + v + "'"));
		if(c == '(') { //operation
			shift();
			if(idx < size) {
				entry.initArgs();
				if(c != ')') {
					do {
						entry.getArgs().add(parseEntry(true, tag));
					} while(idx < size && (c=s.charAt(idx)) == ',' && ++idx < size);
				}
				if(c == ')') { // !else
					shift();
				}
				else {
					throw new IllegalArgumentException("')' expected");
				}
			}
			else {
				throw new IllegalArgumentException("')' expected");
			}
		}
		if(c == '.') {
			shift();
			entry.setNext(parseEntry(argument, tag));
		}
		if(c == ':' && tag) {
			shift();
			entry.setTag(requireLegalVariable(jmpVar(), v-> "illegal variable name : '" + v + "'"));
		}
		if(c == ',' || (idx == size && c == 0) || (c == ')' && argument)) {
			return entry;
		}
		throw new IllegalArgumentException("unexpected character '" + c + "' at index=" + idx);
	}

	private String jmpVal() {
		var from = idx;
		ChartPredicate pr;
		if(s.charAt(from) == '"') {
			shift();
			pr = RequestParser::legalAnyChar; //accept any character
		}
		else {
			var v = jmpVar();
			if((idx == size || s.charAt(idx) == '.' || !legalVarChar(c)) && v.matches(VAR_PATTERN)) {
				return v;
			}
			pr = RequestParser::legalValChar; //auto switch to value
		}
		jmp(pr);
		if(s.charAt(from) != '"') {
			return s.substring(from, idx);
		}
		if(c == '"') {
			shift();
			return s.substring(from+1, idx-1); 
		}
		else {
			throw new IllegalArgumentException("'\"' expected before '" + c + "'" ); //end
		}
	}

	private String jmpVar() {
		var from = idx;
		jmp(RequestParser::legalVarChar);
		return s.substring(from, idx); 
	}

	private void jmp(ChartPredicate pr) {
		while(idx<s.length() && pr.test(c=s.charAt(idx))) idx++;
		c = idx == size ? 0 : s.charAt(idx);
	}
	
	private void shift() {
		if(idx < size) {
			c = ++idx == size ? 0 : s.charAt(idx);
		}
		else {
			throw new IllegalArgumentException("expected");
		}
	}
	
	private static boolean legalValChar(char c) {
		return legalVarChar(c) || c == '.' || c == '-' || c == ':' || c == '+' || c == ' '; //double, instant
	}

	private static boolean legalVarChar(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z' ) || (c >= '0' && c <= '9') || c == '_';
	}
	
	private static boolean legalAnyChar(char c) {
		return c != '"' && c != '\'' && c != '&' && c != '?' && c != '=';  //avoid sql injection & http reserved symbol
	}
	
	@Setter
	@Getter
	@NoArgsConstructor
	static final class RequestEntry {

		private String name;
		private RequestEntry next;
		private LinkedList<RequestEntry> args;
		private String tag;

		public RequestEntry(String name) {
			this.name = name;
		}

		public void initArgs() {
			args = new LinkedList<>();
		}

		@Override
		public String toString() {
			var s = name;
			if(args != null){
				s += args.stream().map(RequestEntry::toString).collect(joining(",", "(", ")"));
			}
			if(next != null) {
				s += "." + next.toString();
			}
			return tag == null ? s : s + ":" + tag;
		}
	}
	
	@FunctionalInterface
	interface ChartPredicate {
		boolean test(char c);
	}
}
