package org.usf.jquery.mvc;

import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static org.usf.jquery.mvc.EntryParser.TokenKind.TXT;
import static org.usf.jquery.mvc.EntryParser.TokenKind.VAL;
import static org.usf.jquery.mvc.EntryParser.TokenKind.VAR;

import java.util.ArrayList;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * 
 * @author u$f
 *
 */
public final class EntryParser {

	public static Entry parseEntry(@NonNull String s) {
		return s.isEmpty() ? new Entry("", VAL) : parseEntry(new Lexer(s));
	}
	
	public static Entry[] parseEntries(@NonNull String s) {
		return s.isEmpty() ? new Entry[0] : parseEntries(new Lexer(s), -1, 0);
	}

	static Entry parseEntry(Lexer cursor) {
		var entry = parseEntry(cursor, false, 0);
		if(cursor.peek() == -1) {
			return entry;
		}
		throw cursor.unexpectedCharacterException(); //end
	}

	static Entry[] parseEntries(Lexer cursor, int mark, int stack) {
		var entries = new ArrayList<Entry>();
		entries.add(parseEntry(cursor, false, stack));
		var c = -1;
		while((c=cursor.peek()) == ',') {
			cursor.advance();
			entries.add(parseEntry(cursor, false, stack));
		}
		if(c == mark) {
			return entries.toArray(Entry[]::new);
		}
		throw cursor.unexpectedCharacterException(); //end
	}

	static Entry parseEntry(Lexer lexer, boolean exp, int stack) {
		if(stack > 20) {
			throw new EntrySyntaxException("too deep entry nesting (>20)");
		}
		var kind = lexer.fetch();
		if(kind == VAR) {
			var value = lexer.emit();
			Entry[] args = null;
			Entry next = null;
			String tag = null;
			var c = lexer.peek();
			if(c == '(') {
				lexer.advance();
				args = lexer.peek() == ')' ? new Entry[0] : parseEntries(lexer, ')', stack+1);
				if(lexer.advance() != ')') {
					throw lexer.unexpectedCharacterException();
				}
				c = lexer.peek();
			}
			if(c == '.') {
				lexer.advance();
				next = parseEntry(lexer, true, stack+1);
				c = lexer.peek();
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
			return new Entry(value, kind, args, next, tag);
		}
		if(exp) {
			throw lexer.unexpectedCharacterException();
		}
		if(kind == TXT || kind == VAL) {
			return new Entry(lexer.emit(), kind);
		}
		throw lexer.unexpectedCharacterException();
	}

	//Domain Specific Language	
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
						throw new IllegalStateException("unexpected state: " + kind);
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
