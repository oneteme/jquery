package org.usf.jquery.web;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Validation.VAR_PATTERN;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
		return new RequestParser(s).parseEntry();
	}
	
	public static List<RequestEntryChain> parseEntries(String s) {
		return new RequestParser(s).parseEntries(false);
	}
	
	public static List<RequestEntryChain> parseArgs(String s) {
		return new RequestParser(s).parseEntries(false);
	}
	
	private List<RequestEntryChain> parseEntries(boolean inner) {
		var entries = new ArrayList<RequestEntryChain>();
		entries.add(parseEntry(true));
		while(c == ',') {
			nextChar(true);
			entries.add(parseEntry(true));
		}
		if(idx == size || (inner && c == ')')) {
			return entries.size() == 1 && isNull(entries.get(0).getValue()) //check this
					? emptyList()
					: entries;
		}
		throw unexpectedCharException();
	}

	private RequestEntryChain parseEntry() {
		var e = parseEntry(true);
		if(idx == size) {
			return e;
		}
		throw unexpectedCharException();
	}

	private RequestEntryChain parseEntry(boolean txt) {
		RequestEntryChain entry = null;
		if(c == '"') {
			if(txt) {
				nextChar(true);
				var from = idx;
				nextWhile(RequestParser::legalTxtChar); //accept any
				requireChar('"'); //nextChar
				entry = new RequestEntryChain(s.substring(from, idx), true); //no next, no args, no tag
				nextChar(false);
			}
		}
		else {
			entry = new RequestEntryChain(nextVal());
			if(c == '(') { //operator
				nextChar(true);
				entry.setArgs(parseEntries(true)); // no args | null
				requireChar(')'); //nextChar
				nextChar(false);
			}
			if(c == '.') {
				nextChar(true);
				entry.setNext(parseEntry(false));
			}
			if(c == ':') {
				nextChar(true);
				entry.setTag(requireLegalVariable(nextVar().get()));
			}
		}
		return entry;
	}

	private String nextVal() {
		var from = idx;
		var v = nextVar();
		if((idx == size || c == '.' || c == ':') && from<idx && legalLetter(s.charAt(from))) { //^[a-zA-Z]
			return v.get();
		} //!variable
		nextWhile(RequestParser::legalValChar);
		return from == idx ? null : s.substring(from, idx); // empty => null
	}
	
	private Supplier<String> nextVar() {
		var from = idx;
		nextWhile(RequestParser::legalVarChar);
		return ()-> s.substring(from, idx); 
	}

	private void nextWhile(CharPredicate cp) {
		while(idx<size && cp.test(c=s.charAt(idx))) ++idx;
		if(idx == size) {
			c = 0;
		}
	}
	
	private void nextChar(boolean require) {
		if(++idx < size) {
			c = s.charAt(idx);
		}
		else if(idx == size) { //break condition
			c = 0;
			if(require) {
				throw somethingExpectedException();
			}
		}
		else {
			throw new IllegalStateException("idx>size");
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
			: new EntryParseException("illegal identifier : " + quote(s));
	}
	
	private EntrySyntaxException unexpectedCharException() {
		return new EntrySyntaxException("unexpected character '" + c + "' at index=" + idx); //end
	}
	
	private EntrySyntaxException somethingExpectedException() {
		return new EntrySyntaxException("something expected after '" + s.charAt(size-1) + "'");
	}
	
	private static boolean legalTxtChar(char c) { //avoid SQL injection & HTTP reserved symbol
		return c != '"' && c != '\'' && c != '&' && c != '?' && c != '=';
	}
	
	private static boolean legalValChar(char c) {
		return legalVarChar(c) || c == '.' || c == '-' || c == ':' || c == '+' || c == ' '; //double, timestamp, 
	}

	private static boolean legalVarChar(char c) {
		return legalLetter(c) || (c >= '0' && c <= '9') || c == '_';
	}
	
	private static boolean legalLetter(char c){
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z' );
	}
	
	@FunctionalInterface
	interface CharPredicate {
		boolean test(char c);
	}
}
