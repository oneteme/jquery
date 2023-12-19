package org.usf.jquery.web;

import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Validation.VAR_PATTERN;

import java.util.LinkedList;
import java.util.List;

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
		this.size = s.length();
	}

	public static RequestEntry parse(String s) {
		return new RequestParser(s).parseEntry(false, false);
	}
	
	public static List<RequestEntry> parseEntries(String s) {
		return new RequestParser(s).parseEntries(true, false);
	}
	
	private List<RequestEntry> parseEntries(boolean multiple, boolean argument) {
		var entries = new LinkedList<RequestEntry>();
		entries.add(parseEntry(multiple, argument));
		while(c == ',') {
			nextChar(true);
			entries.add(parseEntry(multiple, argument));
		}
		return entries;
	}

	private RequestEntry parseEntry(boolean multiple, boolean argument) {
		var entry = argument 
				? nextEntry() 
				: new RequestEntry(requireLegalVariable(jmpVar()));
		if(c == '(') { //operator
			nextChar(true);
			entry.setArgs(parseEntries(true, true)); // no args | null
			if(c != ')') {
				throw somethingExpectedException();
			}
			nextChar(false);
		}
		if(c == '.') {
			nextChar(true);
			entry.setNext(parseEntry(multiple, argument));
		}
		if(c == ':' && !argument) {
			nextChar(true);
			entry.setTag(requireLegalVariable(jmpVar()));
		}
		if((idx == size && c == 0) || (c == ',' && multiple) || (c == ')' && argument)) {
			return entry;
		}
		throw unexpectedCharException();
	}

	private RequestEntry nextEntry() {
		var from = idx;
		if(c == '"') {
			nextChar(true);
			jmp(RequestParser::legalAnyChar); //accept any character
			if(c == '"') {
				nextChar(false);
				return new RequestEntry(s.substring(from+1, idx-1), true);
			}
			throw new IllegalArgumentException("'\"' expected");
		}
		else {
			var v = jmpVar();
			if((idx == size || s.charAt(idx) == '.' || !legalValChar(c)) && v.matches(VAR_PATTERN)) {
				return new RequestEntry(v);
			}
		}
		jmp(RequestParser::legalValChar);
		return new RequestEntry(from == idx ? null : s.substring(from, idx)); // empty => null
	}
	
	private String jmpVar() {
		var from = idx;
		jmp(RequestParser::legalVarChar);
		return s.substring(from, idx); 
	}

	private void jmp(CharPredicate cp) {
		while(idx<size && cp.test(c=s.charAt(idx))) idx++;
		c = idx == size ? 0 : s.charAt(idx);
	}
	
	private void nextChar(boolean require) {
		if(++idx < size) {
			c = s.charAt(idx);
		}
		else if(!require) { //break condition
			c = 0;
		}
		else {
			throw somethingExpectedException();
		}
	}
	
	private String requireLegalVariable(String s) {
		if(s.matches(VAR_PATTERN)) {
			return s;
		}
		throw s.isEmpty() && idx < size 
			? unexpectedCharException() 
			: new IllegalArgumentException("illegal variable name : " + quote(s));
	}
	
	private IllegalArgumentException unexpectedCharException() {
		return new IllegalArgumentException("unexpected character '" + c + "' at index=" + idx); //end
	}
	
	private IllegalArgumentException somethingExpectedException() {
		return new IllegalArgumentException("something expected after '" + s.charAt(size-1) + "'");
	}
	
	private static boolean legalValChar(char c) {
		return legalVarChar(c) || c == '.' || c == '-' || c == ':' || c == '+' || c == ' '; //double, instant
	}

	private static boolean legalVarChar(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z' ) || (c >= '0' && c <= '9') || c == '_';
	}
	
	private static boolean legalAnyChar(char c) {
		return c != '"' && c != '\'' && c != '&' && c != '?' && c != '=';  //avoid SQL injection & HTTP reserved symbol
	}
	
	@FunctionalInterface
	interface CharPredicate {
		boolean test(char c);
	}
}
