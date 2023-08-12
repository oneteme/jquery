package org.usf.jquery.web;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.web.ResultWebView.requireDateColumn;
import static org.usf.jquery.web.ResultWebView.requireNumberColumn;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

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
public final class CalendarView implements ResultWebView {
	
	private static final String COLS = "$columns";
	private static final String DATA = "$data";

    private final Writer writer;
	
    public Void map(ResultSet rs) throws SQLException {
		log.debug("mapping results...");
		var bg = currentTimeMillis();
        var rw = 0;
        var xCol = requireDateColumn(rs.getMetaData());
		var yCol = requireNumberColumn(rs.getMetaData());
		var sb1 = new StringBuilder(100)
		.append("[")
		.append(quote(xCol.getValue().typeName())).append(",")
		.append(quote(xCol.getKey())).append("],")
		.append("[")
		.append(quote(yCol.getValue().typeName())).append(",")
		.append(quote(yCol.getKey())).append("]");
		var sb2 = new StringBuilder(1000);
		while(rs.next()) {
			sb2.append("[")
			.append(xCol.getValue().format(rs.getObject(xCol.getKey()))).append(",")
			.append(yCol.getValue().format(rs.getObject(yCol.getKey()))).append("],");
		}
		sb2.deleteCharAt(sb2.length()-1); //dirty but less code
		try {
			writer.write(readString(Paths.get(getClass().getResource("../chart/calendar.google.html").toURI()))
					.replace(COLS, sb1.toString()) //TD optim this
					.replace(DATA, sb2.toString())
					.replace(lineSeparator(), ""));
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("error while mapping results", e);
		}
		log.info("{} rows mapped in {} ms", rw, currentTimeMillis() - bg);
		return null;
    }
    
}
