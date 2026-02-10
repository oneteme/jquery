package org.usf.jquery.web.proxy;

import static java.util.Objects.nonNull;
import static org.usf.jquery.web.proxy.EntryChainParser.TokenKind.VAR;

import org.usf.jquery.web.EntrySyntaxException;
import org.usf.jquery.web.proxy.EntryChainParser.TokenKind;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
final class EntryChain {

	private final String value;
	private final TokenKind kind; //"string"
	private final EntryChain[] args;
	private final EntryChain next;
	private final String tag;

	EntryChain(String value, TokenKind kind) {
		this(value, kind, null, null, null);
	}
	
	EntryChain(String value, TokenKind kind, EntryChain[] args, EntryChain next, String tag) {
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
	
	public EntryChainIterator iterator() {
		return new EntryChainIterator(this);
	}
}