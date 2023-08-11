package org.usf.jquery.web;

import static java.lang.String.join;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;
import static java.util.function.Predicate.not;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.web.ResultWebView.columns;
import static org.usf.jquery.web.ResultWebView.requireNumberColumn;
import static org.usf.jquery.web.ResultWebView.Formatter.formatCollection;

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
 */
@Slf4j
@RequiredArgsConstructor
public final class PieChartView implements ResultWebView {
	
	private static final String CHART_DATA = "$data";
	private static final String CHART_TYPE = "$chart";

    private final Writer writer;
	
    //https://developers.google.com/chart/interactive/docs/gallery/piechart?hl=fr#data-format
    public Void map(ResultSet rs) throws SQLException {
		log.debug("mapping results...");
		var bg = currentTimeMillis();
        var rw = 0;
		var yCols = requireNumberColumn(rs.getMetaData());
		var xCols = columns(rs.getMetaData(), not(yCols.getKey()::equals));
		var xType = formatCollection("_"); //join empty
		var sb = new StringBuilder()
				.append("[").append(quote(join("_", xCols))).append(",")
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
			writer.write(readString(Paths.get(getClass().getResource("../chart/chart.google.html").toURI()))
					.replace(CHART_TYPE, "PieChart")
					.replace(CHART_DATA, sb.toString())
					.replace(lineSeparator(), ""));
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("error while mapping results", e);
		}
		log.info("{} rows mapped in {} ms", rw, currentTimeMillis() - bg);
		return null;
    }
}
