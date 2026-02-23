package org.usf.jquery.web.proxy;

import static java.util.Objects.isNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class EntryIterator implements Iterator<Entry> {

	private final Entry head;
	private Entry cursor;
	
	@Override
	public boolean hasNext() {
		return isNull(cursor) || cursor.hasNext();
	}
	
	@Override
	public Entry next() {
		return advance().get();
	}
	
	public EntryIterator advance() {
		if(tryAdvance()) {
			return this;
		}
		throw new NoSuchElementException("no more entry to advance");
	}

	public boolean tryAdvance() {
		if(isNull(cursor)) {
			cursor = head;
			return true;
		}
		if(cursor.hasNext()) {
			cursor = cursor.getNext();
			return true;
		}
		return false;
	}
	
	public Entry get() {
		return cursor;
	}
	
	public Entry peekNext() {
		return isNull(cursor) ? head : cursor.getNext();
	}
	
	public EntryIterator reset() {
		this.cursor = null;
		return this;
	}
	
	@Override
	public String toString() {
		return isNull(cursor) 
				? "EntryIterator[head]" 
				: "EntryIterator[cursor=" + cursor + "]";
	}
}
