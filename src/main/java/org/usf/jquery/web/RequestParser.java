package org.usf.jquery.web;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.core.Validation.VAR_PATTERN;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author u$f
 *
 */
public final class RequestParser {

	private final String s;
	private final int size;
	private int idx;
	private char c;
	
	private RequestParser(String s) {
		this.s = requireNonNull(s, "value is null");
		this.size = s.length();
		this.c = size == 0 ? 0 : s.charAt(idx);
	}

	public static RequestEntryChain parseEntry(String s) {
		return new RequestParser(s).parseEntries(false, c-> false).get(0);
	}
	
	public static List<RequestEntryChain> parseEntries(String s) {
		return s.isEmpty() ? emptyList() : new RequestParser(s).parseEntries(true, c-> false);
	}
	
	private List<RequestEntryChain> parseEntries(boolean multiple, CharPredicate until) {
		var entries = new ArrayList<RequestEntryChain>();
		entries.add(parseEntry());
		if(multiple) {
			while(c == ',') {
				nextChar(true);
				entries.add(parseEntry());
			}
		}
		if(idx == size || until.test(c)) {
			return entries;
		}
		throw new EntrySyntaxException("unexpected character '" + c + "' at index=" + idx); //end
	}

	private RequestEntryChain parseEntry() {
		if(legalLetter(c)) {
			var entry = new RequestEntryChain(requireLegalIdentifier(nextWhile(RequestParser::legalVarChar)));
			if(c == '(') { //operator
				nextChar(true);
				entry.setArgs(parseEntries(true, c-> c==')')); // 
				requireChar(')');
				nextChar(false);
			}
			if(c == '.') {
				nextChar(true);
				if(legalLetter(c)) { //require identifier after '.'
					entry.setNext(parseEntry());
				}
			}
			if(c == ':') {
				nextChar(true);
				entry.setTag(requireLegalIdentifier(nextWhile(RequestParser::legalVarChar)));
			}
			return entry;
		}
		if(c == '"') {
			nextChar(true);
			var txt = nextWhile(RequestParser::legalTxtChar);
			requireChar('"');
			nextChar(false);
			return new RequestEntryChain(txt, true);  //no next, no args, no tag
		}
		return new RequestEntryChain(legalNumber(c) || c == '-'  ? nextWhile(RequestParser::legalValChar) : null); // decimal negative?  & instant format
	}
	
	private String nextWhile(CharPredicate cp) {
		var from = idx;
		while(idx<size && cp.test(c=s.charAt(idx))) ++idx;
		if(idx == size) {
			c = 0;
		}
		return s.substring(from, idx);
	}
	
	private void nextChar(boolean require) {
		if(++idx < size) {
			c = s.charAt(idx);
		}
		else if(idx == size) {
			c = 0;
			if(require) {
				throw new EntrySyntaxException("something expected after '" + s.charAt(size-1) + "'");
			}
		}
		else {
			throw new ArrayIndexOutOfBoundsException("idx>size");
		}
	}
	
	private void requireChar(char rc) {
		if(c != rc) {
			throw new EntrySyntaxException("'" + rc + "' expected at index=" + idx); // before ends
		}
	}
	
	private static String requireLegalIdentifier(String s) {
		if(s.matches(VAR_PATTERN)) {
			return s;
		}
		throw new EntrySyntaxException("illegal identifier : " + quote(s));
	}
	
	private static boolean legalTxtChar(char c) { //avoid SQL / HTTP reserved symbol
		return c != '"' && c != '\'' && c != '&' && c != '?' && c != '=';
	}
	
	private static boolean legalValChar(char c) {
		return legalVarChar(c) || c == '.' || c == '-' || c == ':' || c == '+' || c == ' '; //double, timestamp, 
	}

	private static boolean legalVarChar(char c) {
		return legalLetter(c) || legalNumber(c) || c == '_';
	}

	private static boolean legalLetter(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	}
	
	private static boolean legalNumber(char c) {
		return c >= '0' && c <= '9';
	}
	
	@FunctionalInterface
	interface CharPredicate {
		boolean test(char c);
	}
}
