package fr.enedis.teme.jquery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class ParametredQuery {
	
	@NonNull
	private final String query;
	private final String[] columnNames; //postgre case 
	private final Object[] params;
	
	public List<DynamicModel> execute(DataSource ds){

		List<DynamicModel> res;
		try(var cn = ds.getConnection()){
			try(var ps = cn.prepareStatement(query)){
				if(params != null) {
					for(var i=0; i<params.length; i++) {
						ps.setObject(i+1, params[i]);
					}						
				}
				try(var rs = ps.executeQuery()){
					res = mapRows(rs);
				}
			}
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
		return res;
	}

	public List<DynamicModel> mapRows(ResultSet rs) throws SQLException {
		var results = new LinkedList<DynamicModel>();
		while(rs.next()) {
	    	var model = new DynamicModel();
	        for(var i=0; i<columnNames.length; i++) {
	        	model.put(columnNames[i], rs.getObject(i+1));
	        }
	        results.add(model);
		}
        return results;
	}
	
	@Deprecated //postgre insensitive case 
	private static String[] columnNames(ResultSet rs) throws SQLException {
		var n = rs.getMetaData().getColumnCount();
		var names = new String[n];
		for(var i=0; i<n; i++) {
			names[i] = rs.getMetaData().getColumnLabel(i+1);
		}
		return names;
	}
}
