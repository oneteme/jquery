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
		log.trace("mapping results...");
		var t = currentTimeMillis();
		var res = new LinkedList<DynamicModel>();
    	var cols = declaredColumns(rs);
        while(rs.next()) {
            var m = new DynamicModel();
            for(var i=0; i<cols.length; i++) {
                m.put(cols[i], rs.getObject(i+1));
            }
            res.add(m);
        }
		log.trace("{} rows mapped in {} ms", res.size(), currentTimeMillis()-t);
        return res;
    }
}