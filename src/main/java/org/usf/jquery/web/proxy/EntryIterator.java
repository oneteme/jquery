package org.usf.jquery.web.proxy;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 * @author u$f
 *
 */
public final class EntryIterator implements Iterator<Entry> {

	private final Entry head;
	private Entry cursor;
	
	public EntryIterator(Entry entry) {
		this.head = this.cursor = entry;
	}

	@Override
	public boolean hasNext() {
		return cursor.hasNext();
	}
	
	@Override
	public Entry next() {
		return advance().get();
	}
	
	public EntryIterator advance() {
		if(hasNext()) {
			cursor = cursor.getNext();
			return this;
		}
		throw new NoSuchElementException("no more entry in the chain");
	}
	
	public Entry get() {
		return cursor;
	}
	
	public EntryIterator reset() {
		this.cursor = head;
		return this;
	}	
}
