package org.usf.jquery.core;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.lineSeparator;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class ResultCsvExport implements ResultMapper<Void> {

	static final String SEMIC = ";";
    private final Writer writer;
    
    @Override
    public Void map(ResultSet rs, String[] columnNames) {

		log.debug("exporting results...");
		var bg = currentTimeMillis();
        var rw = 0;
        try {
            for(String c : columnNames) {
                writer.write(c);
                writer.write(SEMIC);
            }
            writer.write(lineSeparator());
            while(rs.next()) {
                for(var i=0; i<columnNames.length; i++) {
                    writer.write(String.valueOf(rs.getObject(i+1)));
                    writer.write(SEMIC);
                }
                writer.write(lineSeparator());
                rw++;
            }
        }
        catch(SQLException | IOException e) {
            throw new RuntimeException("error while exporting results", e);
        }
		log.info("{} rows exported in {} ms", rw, currentTimeMillis() - bg);
        return null;
    }
    
}
