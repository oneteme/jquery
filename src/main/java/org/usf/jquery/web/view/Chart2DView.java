package org.usf.jquery.web.view;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;
import static java.util.Map.ofEntries;
import static java.util.stream.Collectors.toList;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.web.view.ResultWebView.DataTable.fromMetaData;
import static org.usf.jquery.web.view.ResultWebView.WebType.NUMBER;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 * 
 * @see <a href="https://developers.google.com/chart/interactive/docs/gallery/columnchart?hl=fr#data-format">columnchart</a>
 * @see <a href="https://developers.google.com/chart/interactive/docs/gallery/barchart?hl=fr#data-format">barchart</a>
 * @see <a href="https://developers.google.com/chart/interactive/docs/gallery/linechart?hl=fr#data-format">linechart</a>
 * @see <a href="https://developers.google.com/chart/interactive/docs/gallery/areachart?hl=fr#data-format">areachart</a>
 * @see <a href="https://developers.google.com/chart/interactive/docs/gallery/combochart?hl=fr#data-format">areachart</a>
 *
 */
@Slf4j
@RequiredArgsConstructor
public final class Chart2DView implements ResultWebView {
	
	private static final String COLS = "$columns";
	private static final String DATA = "$data";
	private static final String TYPE = "$chart";

	private final String type;
    private final Writer writer;
    
    public Void map(ResultSet rs) throws SQLException {
		log.debug("mapping results...");
		var bg = currentTimeMillis();
		var dt = fromMetaData(rs.getMetaData());
		while(rs.next()) {
			dt.fetchRow(rs);
		}
		var sb1 = new StringBuilder();
		var xAxis = dt.getXAxis();
		sb1.append("[").append(doubleQuote(xAxis.getType().typeName())).append(",").append(doubleQuote(xAxis.getName())).append("]");
		var cols = dt.getRows().stream().flatMap(c-> c.stream().skip(1)).map(Entry::getKey).distinct().sorted().collect(toList());
		if(cols.isEmpty()) { //no data
			for(var c : dt.getYAxis()) {
				sb1.append(",[").append(doubleQuote(NUMBER.typeName())).append(",").append(doubleQuote(c.getName())).append("]");
			}		
		}
		else {
			for(var c : cols) {
				sb1.append(",[").append(doubleQuote(NUMBER.typeName())).append(",").append(doubleQuote(c)).append("]");
			}	
		}
		var sb2 = new StringBuilder();
		for(var r : dt.getRows()) {
			@SuppressWarnings("unchecked")
			var map = (Map<String, String>) ofEntries(r.toArray(Entry[]::new));
			sb2.append("[").append(map.get(xAxis.getName()));
			cols.forEach(c-> sb2.append(",").append(map.getOrDefault(c, "0")));
			sb2.append("],");
		}
		if(!sb2.isEmpty()) { //no data
			sb2.deleteCharAt(sb2.length()-1); //dirty but less code
		}
		try {
			writer.write(readString(Paths.get(getClass().getResource("./chart.google.html").toURI()))
					.replace(TYPE, type)
					.replace(COLS, sb1.toString()) //TD optim this
					.replace(DATA, sb2.toString()
					.replace(lineSeparator(), "")));
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("error while mapping results", e);
		}
		log.info("{} rows mapped in {} ms", dt.getRows().size(), currentTimeMillis() - bg);
		return null;
    }
    
    public static final Chart2DView areaChart(Writer w) {
    	return new Chart2DView("AreaChart", w);
    }
    
    public static final Chart2DView barChart(Writer w) {
    	return new Chart2DView("BarChart", w);
    }

    public static final Chart2DView columnChart(Writer w) {
    	return new Chart2DView("ColumnChart", w);
    }

    public static final Chart2DView lineChart(Writer w) {
    	return new Chart2DView("LineChart", w);
    }
    
    public static final Chart2DView comboChart(Writer w) {
    	return new Chart2DView("ComboChart", w);
    }
    
}
