package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public class OverColumn extends OperationColumn {

	public OverColumn(DBOperation operation, Object[] args) {
		super(operation, args);
	}
	
	@Override //!important
	public boolean isAggregation() {
		return false;
	}

}
