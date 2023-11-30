package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
public final class KeyValueMapper implements ResultSetMapper<List<DynamicModel>> {

    @Override
    public List<DynamicModel> map(ResultSet rs) throws SQLException {
		log.debug("mapping results...");
		var bg = currentTimeMillis();
		var results = new LinkedList<DynamicModel>();
    	var columnNames = declaredColumns(rs);
        while(rs.next()) {
            var model = new DynamicModel();
            for(var i=0; i<columnNames.length; i++) {
                model.put(columnNames[i], rs.getObject(i+1));
            }
            results.add(model);
        }
		log.info("{} rows mapped in {} ms", results.size(), currentTimeMillis() - bg);
        return results;
    }
    
}
