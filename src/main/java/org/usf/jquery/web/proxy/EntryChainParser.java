package org.usf.jquery.web.proxy;

import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static org.usf.jquery.web.proxy.EntryChainParser.TokenKind.TXT;
import static org.usf.jquery.web.proxy.EntryChainParser.TokenKind.VAL;
import static org.usf.jquery.web.proxy.EntryChainParser.TokenKind.VAR;

import java.util.ArrayList;

import org.usf.jquery.web.EntrySyntaxException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * 
 * @author u$f
 *
 */
public final class EntryChainParser {

	public static EntryChain parseEntry(@NonNull String s) {
		return s.isEmpty() ? new EntryChain("", VAL) : parseEntry(new Lexer(s));
	}
	
	public static EntryChain[] parseEntries(@NonNull String s) {
		return s.isEmpty() ? new EntryChain[0] : parseEntries(new Lexer(s), -1, 0);
	}

	static EntryChain parseEntry(Lexer cursor) {
		var entry = parseEntry(cursor, false, 0);
		if(cursor.peek() == -1) {
			return entry;
		}
		throw cursor.unexpectedCharacterException(); //end
	}

	static EntryChain[] parseEntries(Lexer cursor, int mark, int stack) {
		var entries = new ArrayList<EntryChain>();
		entries.add(parseEntry(cursor, false, stack));
		var c = -1;
		while((c=cursor.peek()) == ',') {
			cursor.advance();
			entries.add(parseEntry(cursor, false, stack));
		}
		if(c == mark) {
			return entries.toArray(EntryChain[]::new);
		}
		throw cursor.unexpectedCharacterException(); //end
	}

	static EntryChain parseEntry(Lexer lexer, boolean exp, int stack) {
		if(stack > 20) {
			throw new EntrySyntaxException("too deep entry nesting (>20)");
		}
		var kind = lexer.fetch();
		if(kind == VAR) {
			var value = lexer.emit();
			EntryChain[] args = null;
			EntryChain next = null;
			String tag = null;
			var c = lexer.peek();
			if(c == '(') {
				lexer.advance();
				args = lexer.peek() == ')' ? new EntryChain[0] : parseEntries(lexer, ')', stack+1);
				if(lexer.advance() != ')') {
					throw lexer.unexpectedCharacterException();
				}
			}
			if(c == '.') {
				lexer.advance();
				next = parseEntry(lexer, true, stack+1);
			}
			if(c == ':') {
				lexer.advance();
				if(lexer.fetch() == VAR) {
					tag = lexer.emit();
				}
				else {
					throw lexer.unexpectedCharacterException();
				}
			}
			return new EntryChain(value, kind, args, next, tag);
		}
		if(exp) {
			throw lexer.unexpectedCharacterException();
		}
		if(kind == TXT || kind == VAL) {
			return new EntryChain(lexer.emit(), kind);
		}
		throw lexer.unexpectedCharacterException();
	}
	
	@RequiredArgsConstructor
	static class Lexer {
		
		private final String s;
		private int anchor=0;
		private int cursor=-1;
		private TokenKind kind;
		
		TokenKind fetch() {
			while(++cursor<s.length()) {
				var v = s.charAt(cursor);
				if(v=='(' || v==',' || v==')' || ((v=='.' || v==':') && kind == VAR)) { //number, date/time, ..
					break;
				}
				if(v == '"') {
					if(anchor == cursor) {
						kind = TXT;
					}
					else if(kind == TXT) {
						anchor++; //ignore '"'
						break;
					}
					else {
						throw unexpectedCharacterException();
					}
				}
				else if(anchor == cursor) {
					if(isNull(kind)) {
						kind = isLetChar(v) ? VAR : VAL; //first char must be letter
					}
					else {
						throw new IllegalStateException();
					}
				}
				else if(!isVarChar(v) && (isNull(kind) || kind == VAR)){
					kind = VAL;
				}
			}
			return kind;
		}
		
		public String emit() {
			var sub = cursor <= anchor ? "" : s.substring(anchor, cursor);
			anchor = (kind == TXT ? cursor : --cursor)+1;
			kind = null;
			return sub;
		}
		
		public int advance(){
			var c = ++cursor<s.length() ? s.charAt(cursor) : -1;
			anchor = cursor+1;
			kind = null;
			return c;
		}
		
		public int peek(){
			var i=cursor+1;
			return i < s.length() ? s.charAt(i) : -1;
		}
		
		EntrySyntaxException unexpectedCharacterException() {
			var pos = anchor < 0 ? 0 : min(anchor, s.length()-1);
		    String msg = (anchor < s.length()) 
		            ? "unexpected character '" + s.charAt(pos) + "' at index " + pos
		            : "unexpected end of input after '" + s.charAt(s.length()-1) + "'";
		    return new EntrySyntaxException(msg + '\n'+s+'\n'+" ".repeat(pos)+ '^');
		}
		
		private static boolean isVarChar(int c) {
			return isLetChar(c) || isNumChar(c) || c == '_';
		}

		private static boolean isLetChar(int c) {
			return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
		}
		
		private static boolean isNumChar(int c) {
			return c >= '0' && c <= '9';
		}
	}
	
	public enum TokenKind {
		VAR, VAL, TXT
	}
}
