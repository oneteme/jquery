package org.usf.jquery.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 * 
 */
@Getter
@RequiredArgsConstructor
public class Dialect implements Syntaxes, Operators, Comparators {
	
	private final Provider provider;
	
	//allow specific query building for this dialect
	public Query buildQuery(QueryView view){
		return view.build();
	}
}
