package org.usf.jquery.web.view;

import static java.lang.String.join;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;
import static java.util.Collections.singleton;
import static java.util.function.Predicate.not;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.web.view.ResultWebView.columns;
import static org.usf.jquery.web.view.ResultWebView.requireNumberColumn;
import static org.usf.jquery.web.view.ResultWebView.Formatter.formatCollection;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 * @see <a href="https://developers.google.com/chart/interactive/docs/gallery/piechart?hl=fr#data-format">piechart</a>
 *
 */
@Slf4j
@RequiredArgsConstructor
public final class PieChartView implements ResultWebView {
	
	private static final String DATA = "$data";
	private static final String TYPE = "$chart";

    private final Writer writer;
    
    public Void map(ResultSet rs) throws SQLException {
		log.debug("mapping results...");
		var bg = currentTimeMillis();
        var rw = 0;
		var yCols = requireNumberColumn(rs.getMetaData());
		var xCols = rs.getMetaData().getColumnCount() == 1 ? singleton(yCols.getKey()) : columns(rs.getMetaData(), not(yCols.getKey()::equals));
		var xType = formatCollection("_"); //join empty
		var sb = new StringBuilder("[")
				.append(quote(join("_", xCols))).append(",")
				.append(quote(yCols.getKey())).append("]");
		while(rs.next()) {
			sb.append(",[");
			var xVals= new LinkedList<Object>();
			for(var c : xCols) {
				xVals.add(rs.getObject(c));
			}
			sb.append(xType.format(xVals)).append(",")
			.append(yCols.getValue().format(rs.getObject(yCols.getKey())))
			.append("]");
		}
		try {
			writer.write(readString(Paths.get(getClass().getResource("./pie.google.html").toURI()))
					.replace(TYPE, "PieChart")
					.replace(DATA, sb.toString())
					.replace(lineSeparator(), ""));
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("error while mapping results", e);
		}
		log.info("{} rows mapped in {} ms", rw, currentTimeMillis() - bg);
		return null;
    }
    
}
