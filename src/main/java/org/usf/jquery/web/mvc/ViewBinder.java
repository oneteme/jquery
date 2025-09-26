package org.usf.jquery.web.mvc;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ViewBinder<T> {

	public T bind(String id);
}