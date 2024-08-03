package org.usf.jquery.web;

import static org.usf.jquery.core.Utils.isPresent;

import java.util.Iterator;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class ModelIterator<T> implements Iterator<T> {

	private static final ThreadLocal<Object> currentRev = new ThreadLocal<>();
	
	private final Iterator<T> it;
	
	@Override
	public boolean hasNext() {
		if(it.hasNext()) {
			return true;
		}
		currentRev.remove();
		return false;
	}
	
	@Override
	public T next() {
		var rev = it.next();
		currentRev.set(rev);
		return rev;
	}

	public static <T> ModelIterator<T> iterator(T[] model){
		if(isPresent(model)) {
			return new ModelIterator<>(Stream.of(model).iterator());
		}
		throw new IllegalArgumentException("no revision");
	}
}
