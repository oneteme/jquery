package org.usf.jquery.web.proxy;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 * @author u$f
 *
 */
public final class EntryChainIterator implements Iterator<EntryChain> {

	private final EntryChain head;
	private EntryChain cursor;
	
	public EntryChainIterator(EntryChain entry) {
		this.head = this.cursor = entry;
	}

	@Override
	public boolean hasNext() {
		return cursor.hasNext();
	}
	
	@Override
	public EntryChain next() {
		return advance().get();
	}
	
	public EntryChainIterator advance() {
		if(hasNext()) {
			cursor = cursor.getNext();
			return this;
		}
		throw new NoSuchElementException("no more entry in the chain");
	}
	
	public EntryChain get() {
		return cursor;
	}
	
	public EntryChainIterator reset() {
		this.cursor = head;
		return this;
	}
}
