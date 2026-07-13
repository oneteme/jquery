package org.usf.jquery.mvc;

import org.usf.jquery.core.View;

/**
 * 
 * @author u$f
 *
 */
public interface DatasetCatalog<T extends StoreCatalog> extends Catalog {
	
	T getStore();
	
	View getView();
	
	DatasetCatalog<T> mirror();
	
}
