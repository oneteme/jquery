package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@RequiredArgsConstructor
public final class ResultCsvExport implements ResultMapper<Void> {
	
	private static final String SEMIC = ";";
    private final RowWriter writer;

    @Override
    public Void map(ResultSet rs) throws SQLException {
		log.debug("mapping results...");
		var bg = currentTimeMillis();
        var rw = 0;
        try {
        	var columnNames = declaredColumns(rs);
        	StringBuilder sb = new StringBuilder();
            for(String c : columnNames) {
            	sb.append(c).append(SEMIC);
            }
            writer.writeLine(sb.toString());
            while(rs.next()) {
            	sb.delete(0, sb.length()); //hold capacity
                for(var i=0; i<columnNames.length; i++) {
                	sb.append(rs.getObject(i+1)).append(SEMIC);
                }
                writer.writeLine(sb.toString());
                rw++;
            }
        }
        catch(IOException e) {
            throw new RuntimeException("error while mapping results", e);
        }
		log.info("{} rows mapped in {} ms", rw, currentTimeMillis() - bg);
        return null;
    }
    
}
