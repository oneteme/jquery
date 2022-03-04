package fr.enedis.teme.jquery;

import java.util.Collection;

public interface ColumnTemplate {
	
	String name();
	
	Collection<TaggableColumn> getColumns();

}
