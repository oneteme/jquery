package org.usf.jquery.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import static java.lang.System.currentTimeMillis;

@Slf4j
public final class ResultSimpleMapper implements ResultMapper<List<DynamicModel>> {

    @Override
    public List<DynamicModel> map(ResultSet rs, String[] columnNames) {
        
		log.debug("mapping results...");
		var bg = currentTimeMillis();
		var results = new LinkedList<DynamicModel>();
        try {
            while(rs.next()) {
                var model = new DynamicModel();
                for(var i=0; i<columnNames.length; i++) {
                    model.put(columnNames[i], rs.getObject(i+1));
                }
                results.add(model);
            }
        } catch(SQLException e) {
            throw new RuntimeException("error while mapping results", e);
        }
		log.info("{} rows mapped in {} ms", results.size(), currentTimeMillis() - bg);
        return results;
    }
    
}
