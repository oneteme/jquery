package org.usf.jquery.mvc;

import org.usf.jquery.core.View;

/**
 * 
 * @author u$f
 *
 */
public interface DatasetCatalog extends Catalog {
	
	View getView();
	
	DatasetCatalog mirror();
	
}
