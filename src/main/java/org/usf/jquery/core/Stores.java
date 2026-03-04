package org.usf.jquery.core;

import static java.util.Objects.nonNull;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Stores {

	private static final ThreadLocal<Store> currentStore = new ThreadLocal<>();
	
	public static Store getCurrentStore() {
		return currentStore.get();
	}
	
	public static void setCurrentStore(Store store) {
		if(nonNull(store)) {
			currentStore.set(store);
		}
		else {
			currentStore.remove();
		}
	}
}
