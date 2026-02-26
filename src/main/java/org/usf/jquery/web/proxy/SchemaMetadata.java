package org.usf.jquery.web.proxy;
import java.util.Map;

import org.usf.jquery.core.DatabaseVendor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor
public final class SchemaMetadata {

	private final String name;
	private final DatabaseVendor vendor;
	private final Map<String, ViewMetadata> views; //lazy loading
	
	public void updateDataset(String name, ViewMetadata meta) {
		views.put(name, meta);
	}
}