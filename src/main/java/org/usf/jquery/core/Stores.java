package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Dialect.DEFAULT_DIALECT;

import javax.sql.DataSource;

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
	
	static final Store NO_STORE = new Store() {
		
		@Override
		public String name() {
			return null;
		}
		
		@Override
		public Dialect dialect() {
			return DEFAULT_DIALECT;
		}
		
		@Override
		public DataSource dataSource() {
			return null;
		}
	};
}
