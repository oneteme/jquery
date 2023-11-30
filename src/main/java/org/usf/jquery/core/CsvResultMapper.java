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
public final class CsvResultMapper implements ResultSetMapper<Void> {
	
	private static final String SEMIC = ";";
    private final DataWriter writer;

    @Override
    public Void map(ResultSet rs) throws SQLException {
		log.debug("mapping results...");
		var bg = currentTimeMillis();
        var rw = 0;
        try {
        	var columnNames = declaredColumns(rs);
            for(String c : columnNames) {
            	writer.write(c);
            	writer.write(SEMIC);
            }
            writer.writeLine();
            while(rs.next()) {
                for(var i=0; i<columnNames.length; i++) {
                	writer.write(String.valueOf(rs.getObject(i+1)));
                	writer.write(SEMIC);
                }
                writer.writeLine();
                rw++;
            }
        }
        catch(IOException e) {
            throw new MappingException("error writing results", e);
        }
		log.info("{} rows written in {} ms", rw, currentTimeMillis() - bg);
        return null;
    }
    
}
