package org.usf.jquery.web;

import java.time.YearMonth;

import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@ToString(includeFieldNames = false)
public final class YearTableDecoratorWrapper implements YearTableDecorator {
	
	@Delegate
	private final YearTableDecorator table;
	private final YearMonth[] revisions; //nullable

	YearTableDecoratorWrapper(YearTableDecorator table, YearMonth[] revisions) {
		this.table = table;
		this.revisions = revisions;
	}
	
	@Override
	public YearMonth[] availableRevisions() {
		return revisions;
	}	
}