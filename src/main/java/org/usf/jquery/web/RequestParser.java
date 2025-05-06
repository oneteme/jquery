package org.usf.jquery.web;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
public final class RequestParser {
	
	private static final CharPredicate ANY = c-> true;

	private final String s;
	private final int size;
	private int idx;
	private char c;
	
	private RequestParser(String s) {
		this.s = s;
		this.size = s.length();
		this.c = size == 0 ? 0 : s.charAt(idx);
	}

	public static RequestEntryChain parseEntry(@NonNull String s) {
		return new RequestParser(s).parseAllEntries(false).get(0);
	}
	
	public static List<RequestEntryChain> parseEntries(@NonNull String s) {
		return s.isEmpty() 
				? emptyList() 
				: new RequestParser(s).parseAllEntries(true);
	}
	
	private List<RequestEntryChain> parseAllEntries(boolean multiple) {
		var res = parseEntries(multiple);
		if(idx == size) {
			return res;
		}
		throw new EntrySyntaxException("unexpected character '" + c + "' at index=" + idx); //end
	}
	
	private List<RequestEntryChain> parseEntries(boolean multiple) {
		var entries = new ArrayList<RequestEntryChain>();
		entries.add(parseEntry());
		if(multiple) {
			while(c == ',') {
				nextChar(null);
				entries.add(parseEntry());
			}
		}
		return entries;
	}

	private RequestEntryChain parseEntry() {
		if(legalLetter(c)) {
			var entry = new RequestEntryChain(nextWhile(RequestParser::legalVarChar));
			if(c == '(') { //operator
				nextChar(ANY);
				entry.setArgs(parseEntries(true)); // 
				requireChar(')');
				nextChar(null);
			}
			if(c == '.') {
				nextChar(RequestParser::legalLetter);
				entry.setNext(parseEntry());
			}
			if(c == ':') {
				nextChar(RequestParser::legalLetter);
				entry.setTag(nextWhile(RequestParser::legalVarChar));
			}
			return entry;
		}
		if(legalNumber(c) || c == '-') { //negative number
			return new RequestEntryChain(nextWhile(RequestParser::legalValChar));
		}
		if(c == '"') {
			nextChar(ANY); //empty string 
			var txt = nextWhile(RequestParser::legalTxtChar);
			requireChar('"');
			nextChar(null);
			return new RequestEntryChain(txt, true);  //no next, no args, no tag
		}
		return new RequestEntryChain(null); 
	}
	
	private String nextWhile(CharPredicate cp) {
		var from = idx;
		while(idx<size && cp.test(c=s.charAt(idx))) ++idx;
		if(idx == size) {
			c = 0;
		}
		return s.substring(from, idx);
	}
	
	private void nextChar(CharPredicate pre) {
		if(++idx < size) {
			c = s.charAt(idx);
			if(nonNull(pre) && !pre.test(c)) {
				throw new EntrySyntaxException("unexpected character '" + c + "' at index=" + idx); //end
			}
		}
		else if(idx == size) {
			c = 0;
			if(nonNull(pre)) {
				throw new EntrySyntaxException("something expected after '" + s.charAt(size-1) + "'");
			}
		}
		else { //should never happen
			throw new ArrayIndexOutOfBoundsException("idx>size");
		}
	}
	
	private void requireChar(char rc) {
		if(c != rc) {
			throw new EntrySyntaxException("expected character '" + rc + "' at index=" + idx); // before ends
		}
	}
	
	//bug param="Côte d'Azur" => exclude && c != '\''
	private static boolean legalTxtChar(char c) {
		return c != '"';
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
