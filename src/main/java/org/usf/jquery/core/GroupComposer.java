package org.usf.jquery.core;

import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.isNull;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
public class GroupComposer implements Composer<Group> {
	
	private Collection<Order> orders;
	
	public GroupComposer order(@NonNull Order order) {
		getOrders().add(order);
		return this;
	}
	
	public GroupComposer orders(@NonNull Order... orders) {
		addAll(getOrders(), orders);
		return this;
	}
	
	public GroupComposer orders(@NonNull Collection<Order> orders) {
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
	public Group compose(Store store) {
		if(!isEmpty(orders)) {
			return new Group(unmodifiableCollection(orders));
		}
		throw new ComposeException("within group requires at least one order");
	}
}
