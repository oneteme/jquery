package org.usf.jquery.web.view;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.web.view.WebViewMapper.TableColumn.columns;
import static org.usf.jquery.web.view.WebViewMapper.WebType.NUMBER;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import org.usf.jquery.core.MappingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 * 
 * @see <a href="https://developers.google.com/chart/interactive/docs/gallery/calendar?hl=fr">calendar</a>
 *
 */
@Slf4j
@RequiredArgsConstructor
public final class CalendarView implements WebViewMapper {
	
	private static final String COLS = "$columns";
	private static final String DATA = "$data";

    private final Writer writer;
	
    public Void map(ResultSet rs) throws SQLException {
    	if(rs.getMetaData().getColumnCount() != 2) {
    		throw new IllegalArgumentException("require 2 columns [DATE, NUMBER]");
    	}
		log.debug("mapping results...");
		var bg = currentTimeMillis();
        var rw = 0;
        var cols = columns(rs.getMetaData());
        var xCol = Stream.of(cols).filter(c-> c.getType().isDate()).findAny().orElseThrow(()-> new IllegalArgumentException("require date column"));
		var yCol = Stream.of(cols).filter(c-> c.getType() == NUMBER).findAny().orElseThrow(()-> new IllegalArgumentException("require number column"));
		
		var sb1 = new StringBuilder(100)
		.append("[")
		.append(quote(xCol.getType().typeName())).append(",")
		.append(quote(xCol.getName())).append("],")
		.append("[")
		.append(quote(yCol.getType().typeName())).append(",")
		.append(quote(yCol.getName())).append("]");
		var sb2 = new StringBuilder(1000);
		while(rs.next()) {
			sb2.append("[")
			.append(xCol.getType().format(rs.getObject(xCol.getName()))).append(",")
			.append(yCol.getType().format(rs.getObject(yCol.getName()))).append("],");
			rw++;
		}
		if(!sb2.isEmpty()) { //no data
			sb2.deleteCharAt(sb2.length()-1); //dirty but less code
		}
		try {
			writer.write(readString(Paths.get(getClass().getResource("./calendar.google.html").toURI()))
					.replace(COLS, sb1.toString()) //TD optim this
					.replace(DATA, sb2.toString())
					.replace(lineSeparator(), ""));
		} catch (IOException | URISyntaxException e) {
			throw new MappingException("error mapping results", e);
		}
		log.info("{} rows mapped in {} ms", rw, currentTimeMillis() - bg);
		return null;
    }
    
}
