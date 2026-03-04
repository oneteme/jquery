package org.usf.jquery.core;

import static org.usf.jquery.core.Provider.DEFAULT;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 * 
 */
@Getter
@RequiredArgsConstructor
public class Dialect implements Operators, Comparators {
	
	public static final Dialect DEFAULT_META = new Dialect(DEFAULT);
	
	private final Provider provider;
	
	//allow specific query building for this dialect
	public Query buildQuery(QueryView view){
		return view.build(); //TODO replace null
	}
}
