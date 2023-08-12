package org.usf.jquery.web;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.web.ResultWebView.WebType.typeOf;

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
 * @see <a href="https://developers.google.com/chart/interactive/docs/gallery/table?hl=fr#data-format">table</a>
 *
 */
@Slf4j
@RequiredArgsConstructor
public final class TableView implements ResultWebView {
	
	private static final String COLS = "$columns";
	private static final String DATA = "$data";

    private final Writer writer;
	
    public Void map(ResultSet rs) throws SQLException {
		log.debug("mapping results...");
		var bg = currentTimeMillis();
        var rw = 0;
        var nc = rs.getMetaData().getColumnCount();
		var sb1 = new StringBuilder(nc * 20);
		var types = new WebType[nc];
		for(var i=0; i<nc; i++) {
			var name = rs.getMetaData().getColumnLabel(i+1);
			types[i] = typeOf(rs.getMetaData().getColumnType(i+1));
			sb1.append("[")
			.append(quote(types[i].typeName())).append(",")
			.append(quote(name)).append("],");
		}
		sb1.deleteCharAt(sb1.length()-1); //dirty but less code
		var sb2 = new StringBuilder(nc * 100);
		while(rs.next()) {
			sb2.append("[").append(types[0].format(rs.getObject(1)));
			for(int i=1; i<types.length; i++) {
				sb2.append(",").append(types[i].format(rs.getObject(i+1)));
			}
			sb2.append("],");
		}
		sb2.deleteCharAt(sb2.length()-1); //dirty but less code
		try {
			writer.write(readString(Paths.get(getClass().getResource("../chart/table.google.html").toURI()))
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
