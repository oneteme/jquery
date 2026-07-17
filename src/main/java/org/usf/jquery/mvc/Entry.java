package org.usf.jquery.mvc;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.mvc.EntryParser.TokenKind.TXT;
import static org.usf.jquery.mvc.EntryParser.TokenKind.VAR;

import org.usf.jquery.mvc.EntryParser.TokenKind;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.With;

/**
 * 
 * @author u$f
 *
 */
@Getter
@EqualsAndHashCode
public final class Entry {

	private final String value;
	private final TokenKind kind;
	@With private final Entry[] args;
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
	
	@Override
	public String toString() {
		var s = ""; // null == EMPTY
		if(nonNull(value)) {
			s += kind == TXT ? '"' + value + '"' : value;
		}
		if(nonNull(args)){
			s += stream(args).map(Entry::toString).collect(joining(",", "(", ")"));
		}
		if(hasNext()) {
			s += "." + next.toString();
		}
		return isNull(tag) ? s : s + ":" + tag;
	}
}