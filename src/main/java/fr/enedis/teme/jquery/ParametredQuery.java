package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public class ParametredQuery {
	
	private final String query;
	private Column[] columns;
	private final Object[] params;
	
	public ParametredQuery(String query, Column[] columns, Object... params) {
		this.query = requireNonNull(query);
		this.columns = columns;
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
	
	public static final ParametredQuery union(Collection<ParametredQuery> queries) {
		if(requireNonNull(queries).isEmpty()) {
			throw new IllegalArgumentException("empty list");
		}
		//check columns
		String  query = queries.stream().map(ParametredQuery::getQuery).collect(Collectors.joining(" UNION "));
		Object[] args = queries.stream().flatMap(q-> Stream.of(q.getParams())).toArray();
		return new ParametredQuery(query, queries.iterator().next().getColumns(), args);
	}
}
