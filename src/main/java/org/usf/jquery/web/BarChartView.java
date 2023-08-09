package org.usf.jquery.web;

import static java.lang.String.join;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;
import static java.util.function.Predicate.not;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.web.ResultWebView.Formatter.formatCollection;
import static org.usf.jquery.web.ResultWebView.Formatter.formatFirstItem;
import static org.usf.jquery.web.ResultWebView.WebType.NUMBER;
import static org.usf.jquery.web.ResultWebView.WebType.STRING;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@RequiredArgsConstructor
public final class BarChartView implements ResultWebView {
	
	private static final String CHART_DATA = "$data";
	private static final String CHART_TYPE = "$chart";

	private final String type;
    private final Writer writer;
    
    //https://developers.google.com/chart/interactive/docs/gallery/columnchart?hl=fr#data-format
    public Void map(ResultSet rs) throws SQLException {
		log.debug("mapping results...");
		var bg = currentTimeMillis();
        var rw = 0;
		var sb = new StringBuilder();
		var xCols = columns(rs.getMetaData(), not(NUMBER::equals)); //TD numeric columns : STATUS, ...
		Formatter<Collection<Object>> xType;
		if(xCols.isEmpty()) {
			xType = o-> STRING.format("");
		}
		else {
			xType = xCols.size() > 1
					? formatCollection()
					: formatFirstItem(typeOf(rs.getMetaData(), xCols.get(0))::format);
		}
		var yCols = columns(rs.getMetaData(), NUMBER::equals);
		if(yCols.isEmpty()) {
			throw new RuntimeException("xAxis required");
		}
		sb.append("[").append(quote(join("_", xCols)));
		yCols.forEach(y-> sb.append(",").append(quote(y)));
		sb.append("]");
		while(rs.next()) {
			sb.append(",[");
			var xVals= new LinkedList<Object>();
			for(var c : xCols) {
				xVals.add(rs.getObject(c));
			}
			sb.append(xType.format(xVals));
			for(var c : yCols) {
				sb.append(",").append(NUMBER.format(rs.getObject(c)));
			}
			sb.append("]");
		}
		try {
			writer.write(readString(Paths.get(getClass().getResource("../chart/chart.google.html").toURI()))
					.replace(CHART_TYPE, type)
					.replace(CHART_DATA, sb.toString()
					.replace(lineSeparator(), "")));
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("error while mapping results", e);
		}
		log.info("{} rows mapped in {} ms", rw, currentTimeMillis() - bg);
		return null;
    }
    
    private List<String> columns(ResultSetMetaData rsm, Predicate<WebType> test) throws SQLException {
    	List<String> columns = new LinkedList<>();
		for(var i=0; i<rsm.getColumnCount(); i++) {
			if(test.test(WebType.typeOf(rsm.getColumnType(i+1)))) {
				columns.add(rsm.getColumnName(i+1));
			}
		}
		return columns;
	}

    private WebType typeOf(ResultSetMetaData rsm, String cn) throws SQLException {

		for(var i=0; i<rsm.getColumnCount(); i++) {
			if(rsm.getColumnName(i+1).equals(cn)) {
				return WebType.typeOf(rsm.getColumnType(i+1));
			}
		}
		throw new IllegalStateException("");
    }
    
    public static final BarChartView barChart(Writer w) {
    	return new BarChartView("BarChart", w);
    }

    public static final BarChartView columnChart(Writer w) {
    	return new BarChartView("ColumnChart", w);
    }

}
