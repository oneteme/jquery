package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Validation.requireNonBlank;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;

import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.Getter;

@Getter
public class ParametredQuery {
	
	private final String query;
	private DBColumn[] columns;
	private final Object[] params;
	
	public ParametredQuery(String query, DBColumn[] columns, Object... params) {
		this.query = requireNonBlank(query);
		this.columns = requireNonEmpty(columns);
		this.params = params;
	}

	//spring row mapper
	public DynamicModel map(ResultSet rs, int i) throws SQLException {
		return map(rs);
	}

	public DynamicModel map(ResultSet rs) throws SQLException {
    	var model = new DynamicModel();
        for(var c=0; c<columns.length; c++) {
        	model.setField(columns[c], rs.getObject(c+1));
        }
        return model;
	}
}
