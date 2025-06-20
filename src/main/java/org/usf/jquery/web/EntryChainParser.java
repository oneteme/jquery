package org.usf.jquery.web;

import static java.util.Objects.nonNull;
import static org.usf.jquery.web.EntryChainParser.CharPredicate.ANY;

import java.util.ArrayList;

import lombok.NonNull;

/**
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * 
 * @author u$f
 *
 */
public final class EntryChainParser {

	private final String s;
	private final int size;
	private int idx;
	private char c;
	
	private EntryChainParser(String s) {
		this.s = s;
		this.size = s.length();
		this.c = size == 0 ? 0 : s.charAt(idx);
	}

	public static EntryChain parseEntry(@NonNull String s) {
		return new EntryChainParser(s).parseAllEntries(false)[0];
	}
	
	public static EntryChain[] parseEntries(@NonNull String s) {
		return s.isEmpty() 
				? new EntryChain[0]
				: new EntryChainParser(s).parseAllEntries(true);
	}
	
	private EntryChain[] parseAllEntries(boolean multiple) {
		var res = parseEntries(multiple);
		if(idx==size && c==0) {
			return res;
		}
		throw unexpectedCharacterException(); //end
	}
	
	private EntryChain[] parseEntries(boolean multiple) {
		var entries = new ArrayList<EntryChain>();
		entries.add(parseEntry());
		if(multiple) {
			while(c == ',') {
				assertNextChar(null);
				entries.add(parseEntry());
			}
		}
		return entries.toArray(EntryChain[]::new);
	}

	private EntryChain parseEntry() {
		if(legalLetter(c)) {
			var value = nextWhile(EntryChainParser::legalVarChar);
			EntryChain[] args = null;
			EntryChain next = null;
			String tag = null;
			if(c == '(') { //operator
				assertNextChar(ANY);
				args = parseEntries(true); // 
				requireChar(')');
				assertNextChar(null);
			}
			if(c == '.') {
				assertNextChar(EntryChainParser::legalLetter);
				next = parseEntry();
			}
			if(c == ':') {
				assertNextChar(EntryChainParser::legalLetter);
				tag = nextWhile(EntryChainParser::legalVarChar);
			}
			return new EntryChain(value, args, next, tag);
		}
		if(legalNumber(c) || c == '-') { //negative number
			return new EntryChain(nextWhile(EntryChainParser::legalValChar));
		}
		if(c == '"') {
			assertNextChar(ANY); //empty string 
			var txt = nextWhile(EntryChainParser::legalTxtChar);
			requireChar('"');
			assertNextChar(null);
			return new EntryChain(txt, true);  //no next, no args, no tag
		}
		return new EntryChain(null); 
	}
	
	private String nextWhile(CharPredicate cp) {
		var from = idx;
		while(idx<size && cp.test(c=s.charAt(idx))) ++idx;
		if(idx == size) {
			c = 0;
		}
		return s.substring(from, idx);
	}
	
	private void assertNextChar(CharPredicate pre) {
		if(++idx < size) {
			c = s.charAt(idx);
			if(nonNull(pre) && !pre.test(c)) {
				throw unexpectedCharacterException(); //end
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
	
	private EntrySyntaxException unexpectedCharacterException() {
		return new EntrySyntaxException("unexpected character '" + c + "' at index=" + idx);
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

	//param="Côte d'Azur" => exclude && c != '\''
	private static boolean legalTxtChar(char c) {
		return c != '"';
	}
	
	@FunctionalInterface
	interface CharPredicate {

		static final CharPredicate ANY = c-> true;
		
		boolean test(char c);
	}
}
