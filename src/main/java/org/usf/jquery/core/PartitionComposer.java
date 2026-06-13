package org.usf.jquery.core;

import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.Collection;

import lombok.NonNull;

/**
 * 
 * @author u$f
 * 
 */
public class PartitionComposer implements Composer<Partition> {
	
	private Collection<Column> columns;
	private Collection<Order> orders;

	public PartitionComposer column(@NonNull Column column) {
		getColumns().add(column);
		return this;
	}
	
	public PartitionComposer columns(@NonNull Column... columns) {
		addAll(getColumns(), columns);
		return this;
	}
	
	public PartitionComposer columns(@NonNull Collection<Column> columns) {
		getColumns().addAll(columns);
		return this;
	}
	
	private Collection<Column> getColumns(){
		if(isNull(columns)) {
			columns = new ArrayList<>();
		}
		return columns;
	}
	
	public PartitionComposer order(@NonNull Order order) {
		getOrders().add(order);
		return this;
	}
	
	public PartitionComposer orders(@NonNull Order... orders) {
		addAll(getOrders(), orders);
		return this;
	}
	
	public PartitionComposer orders(@NonNull Collection<Order> orders) {
		getOrders().addAll(orders);
		return this;
	}
	
	private Collection<Order> getOrders(){
		if(isNull(orders)) {
			orders = new ArrayList<>();
		}
		return orders;
	}
	
	@Override
	public Partition compose(Store store) {
		return new Partition(
				nonNull(columns) ? unmodifiableCollection(columns) : null, 
				nonNull(orders) ? unmodifiableCollection(orders) : null);
	}
}