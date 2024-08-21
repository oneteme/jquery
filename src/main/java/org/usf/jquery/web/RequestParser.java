package org.usf.jquery.web;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
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
	private int size;
	private int idx;
	private char c;
	
	private RequestParser(String s) {
		this.s = s;
		this.size = s.length();
		this.c = size == 0 ? 0 : s.charAt(idx);
	}

	public static RequestEntryChain parseEntry(String s) {
		return new RequestParser(s).parseEntry(false, false);
	}
	
	public static List<RequestEntryChain> parseEntries(String s) {
		return new RequestParser(s).parseEntries(true, false);
	}
	
	public static List<RequestEntryChain> parseArgs(String s) {
		return new RequestParser(s).parseEntries(true, true);
	}
	
	private List<RequestEntryChain> parseEntries(boolean multiple, boolean argument) {
		var entries = new LinkedList<RequestEntryChain>();
		entries.add(parseEntry(multiple, argument));
		while(c == ',') {
			nextChar(!argument);
			entries.add(parseEntry(multiple, argument));
		}
		return entries.size() == 1 && isNull(entries.get(0).getValue()) //avoid () => (null)
				? emptyList()
				: entries;
	}

	private RequestEntryChain parseEntry(boolean multiple, boolean argument) {
		var entry = argument 
				? nextEntry() 
				: new RequestEntryChain(requireLegalVariable(nextVar()));
		if(c == '(') { //operator
			nextChar(true);
			entry.setArgs(parseEntries(true, true)); // no args | null
			requireChar(')'); //nextChar
			nextChar(false);
		}
		if(c == '.') {
			nextChar(true);
			entry.setNext(parseEntry(multiple, argument));
		}
		if(c == ':' && !argument) {
			nextChar(true);
			entry.setTag(requireLegalVariable(nextVar()));
		}
		if((idx == size && c == 0) || (c == ',' && multiple) || (c == ')' && argument)) {
			return entry;
		}
		throw unexpectedCharException();
	}

	private RequestEntryChain nextEntry() {
		var from = idx;
		if(c == '"') {
			nextChar(true);
			nextWhile(RequestParser::legalTxtChar); //accept any
			requireChar('"'); //nextChar
			nextChar(false);
			return new RequestEntryChain(s.substring(from+1, idx-1), true);
		}
		var v = nextVar(); //to optim
		if((idx == size || s.charAt(idx) == '.' || !legalValChar(c)) && v.matches(VAR_PATTERN)) {
			return new RequestEntryChain(v);
		}
		nextWhile(RequestParser::legalValChar);
		return new RequestEntryChain(from == idx ? null : s.substring(from, idx)); // empty => null
	}
	
	private String nextVar() {
		var from = idx;
		nextWhile(RequestParser::legalVarChar);
		return s.substring(from, idx); 
	}

	private void nextWhile(CharPredicate cp) {
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
	
	private void requireChar(char rc) {
		if(c != rc) {
			throw new EntryParseException("'" + rc + "' expected at index=" + idx); // before ends
		}
	}
	
	private String requireLegalVariable(String s) {
		if(s.matches(VAR_PATTERN)) {
			return s;
		}
		throw s.isEmpty() && idx < size 
			? unexpectedCharException() 
			: new EntryParseException("illegal variable name : " + quote(s));
	}
	
	private EntryParseException unexpectedCharException() {
		return new EntryParseException("unexpected character '" + c + "' at index=" + idx); //end
	}
	
	private EntryParseException somethingExpectedException() {
		return new EntryParseException("something expected after '" + s.charAt(size-1) + "'");
	}
	
	private static boolean legalTxtChar(char c) { //avoid SQL injection & HTTP reserved symbol
		return c != '"' && c != '\'' && c != '&' && c != '?' && c != '=';
	}
	
	private static boolean legalValChar(char c) {
		return legalVarChar(c) || c == '.' || c == '-' || c == ':' || c == '+' || c == ' '; //double, instant
	}

	private static boolean legalVarChar(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z' ) || (c >= '0' && c <= '9') || c == '_';
	}
	
	@FunctionalInterface
	interface CharPredicate {
		boolean test(char c);
	}
}
