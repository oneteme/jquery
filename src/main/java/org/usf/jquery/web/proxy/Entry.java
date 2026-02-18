package org.usf.jquery.web.proxy;

import static java.util.Objects.nonNull;
import static org.usf.jquery.web.proxy.EntryParser.TokenKind.VAR;

import org.usf.jquery.web.EntrySyntaxException;
import org.usf.jquery.web.proxy.EntryParser.TokenKind;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class Entry {

	private final String value;
	private final TokenKind kind;
	private final Entry[] args;
	private final Entry next;
	private final String tag;

	Entry(String value, TokenKind kind) {
		this(value, kind, null, null, null);
	}
	
	Entry(String value, TokenKind kind, Entry[] args, Entry next, String tag) {
		if(kind == VAR) {
			if(nonNull(next) && next.isValue()) {
				throw new EntrySyntaxException("next entry must be a variable");
			}
		}
		else if(nonNull(args) || nonNull(next) || nonNull(tag)) {
			throw new EntrySyntaxException("only 'VAR' kind can have arguments, next or tag entry");
		}
		this.value = value;
		this.kind = kind;
		this.args = args;
		this.next = next;
		this.tag = tag;
	}
	
	public boolean hasNext() {
		return nonNull(next);
	}
	
	public boolean hasArgs() {
		return nonNull(args);
	}
	
	public boolean hasTag() {
		return nonNull(tag);
	}
	
	public boolean isVariable() {
		return kind == VAR;
	}
	
	public boolean isValue() {
		return kind != VAR; //VAL or TXT
	}
	
	public EntryIterator iterator() {
		return new EntryIterator(this);
	}
}