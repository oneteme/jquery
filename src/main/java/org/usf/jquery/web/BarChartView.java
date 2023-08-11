package org.usf.jquery.web;

import static java.lang.String.join;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;
import static java.util.function.Predicate.not;
import static org.usf.jquery.core.SqlStringBuilder.quote;
import static org.usf.jquery.web.ResultWebView.columns;
import static org.usf.jquery.web.ResultWebView.Formatter.formatCollection;
import static org.usf.jquery.web.ResultWebView.Formatter.formatFirstItem;
import static org.usf.jquery.web.ResultWebView.WebType.NUMBER;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

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
		var yCols = requireNumberColumns(rs.getMetaData());
		var xCols = columns(rs.getMetaData(), not(yCols::contains)); //other
		var xType = "LineChart".equals(type) && xCols.size() == 1
				? formatFirstItem(typeOf(rs.getMetaData(), xCols.get(0)))
				: formatCollection("_");
		var sb = new StringBuilder()
				.append("[").append(quote(join("_", xCols)));
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
    
	static List<String> requireNumberColumns(ResultSetMetaData rsm) throws SQLException {
    	var last = rsm.getColumnCount();
    	var columns = new LinkedList<String>();
    	while(last > 1 && WebType.typeOf(rsm.getColumnType(last)) == NUMBER) {
    		columns.add(rsm.getColumnName(last--));
    	}
    	if(columns.isEmpty()) { //any numeric
    		for(var i=0; i<rsm.getColumnCount(); i++) {
    			if(WebType.typeOf(rsm.getColumnType(i+1)) == NUMBER) {
    				columns.add(rsm.getColumnName(i+1));
    			}
    		}
    	}
    	if(columns.isEmpty()) {
    		throw new IllegalArgumentException("numeric column expected");
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
    
    public static final BarChartView areaChart(Writer w) {
    	return new BarChartView("AreaChart", w);
    }
    
    public static final BarChartView barChart(Writer w) {
    	return new BarChartView("BarChart", w);
    }

    public static final BarChartView columnChart(Writer w) {
    	return new BarChartView("ColumnChart", w);
    }

    public static final BarChartView lineChart(Writer w) {
    	return new BarChartView("LineChart", w);
    }

}
