package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.Stores.getCurrentStore;
import static org.usf.jquery.core.Stores.setCurrentStore;

import java.util.function.Consumer;

import javax.sql.DataSource;

/**
 * 
 * @author u$f
 *
 */
public interface Store {

	String name();

	Dialect dialect();
	
	DataSource dataSource();
	
	default Table table(String name) {
		return new Table(name, name());
	}
	
	default Query newQuery(Consumer<QueryComposer> cons) {
		var qc = new QueryComposer();
		var cs = getCurrentStore();
		if(isNull(cs)) {
			setCurrentStore(this);
			try {
				cons.accept(qc);
			}
			finally {
				setCurrentStore(null);
			}
		}
		else if(cs == this) {
			cons.accept(qc); //nested query
		}
		else {
			throw new IllegalStateException("store mismatch");
		}
		return qc.compose(this);
	}
}
