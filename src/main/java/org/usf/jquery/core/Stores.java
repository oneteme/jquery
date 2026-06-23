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

	private static final ThreadLocal<Dialect> currentStore = new ThreadLocal<>();
	
	public static Dialect getCurrentDialect() {
		var dial = currentStore.get();
		return nonNull(dial) ? dial : DEFAULT_DIALECT;
	}
	
	public static void setCurrentDialect(Dialect store) {
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
