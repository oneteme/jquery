package org.usf.jquery.web.view;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readString;
import static java.util.Comparator.comparing;
import static java.util.Map.entry;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.core.SqlStringBuilder.doubleQuote;
import static org.usf.jquery.web.view.ResultWebView.WebType.NUMBER;
import static org.usf.jquery.web.view.ResultWebView.WebType.STRING;
import static org.usf.jquery.web.view.ResultWebView.WebType.typeOf;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Map.Entry;

import lombok.Getter;
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
        var rw = 0;
        var sb = rs.getMetaData().getColumnCount() > 2 ? pivotAndMap(rs) : simpleMap(rs);
		try {
			writer.write(readString(Paths.get(getClass().getResource("./chart.google.html").toURI()))
					.replace(TYPE, type)
					.replace(COLS, sb[0].toString()) //TD optim this
					.replace(DATA, sb[1].toString()
					.replace(lineSeparator(), "")));
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("error while mapping results", e);
		}
		log.info("{} rows mapped in {} ms", rw, currentTimeMillis() - bg);
		return null;
    }
    
    private StringBuilder[] simpleMap(ResultSet rs) throws SQLException { //2d
    	var rsm = rs.getMetaData();
    	int cols = rsm.getColumnCount();
    	var yType = typeOf(rsm.getColumnType(cols));
    	if(yType != NUMBER) {
    		throw new IllegalArgumentException("require numeric column as last column");
    	}
    	var xType = cols > 1 ? typeOf(rsm.getColumnType(1)) : STRING; //can be null
    	var sb1 = new StringBuilder();
    	sb1.append("[")
    	.append(doubleQuote(xType.typeName())).append(",")
    	.append(doubleQuote(cols > 1 ? rsm.getColumnName(1) : ""))
    	.append("],[")
    	.append(doubleQuote(yType.typeName())).append(",")
    	.append(doubleQuote(rsm.getColumnName(cols)))
    	.append("]");
    	var sb2 = new StringBuilder();
    	var data = new LinkedList<Entry<Object, Object>>();
    	while(rs.next()) {
    		data.add(entry(rs.getObject(1), rs.getObject(cols)));
    	}
    	data.stream().sorted(comparing(e-> (Comparable<Object>)e.getKey())).forEach(e->
    		sb2.append("[")
    		.append(xType.format(e.getKey())).append(",")
    		.append(NUMBER.format(e.getValue())).append("],"));
    	if(!sb2.isEmpty()) {
    		sb2.deleteCharAt(sb2.length()-1); //dirty but less code
    	}
		return new StringBuilder[]{sb1, sb2};
	}

	private StringBuilder[] pivotAndMap(ResultSet rs) throws SQLException { //xAxis,dim1,...,yAxis 

    	var rsm = rs.getMetaData();
    	int cols = rsm.getColumnCount();
    	var xType = typeOf(rsm.getColumnType(1)); //can be null
    	var yType = typeOf(rsm.getColumnType(cols));
    	if(yType != NUMBER) {
    		throw new IllegalArgumentException("require numeric column as last column");
    	}
    	var rows = new LinkedList<DataHolder>();
    	var join = new LinkedList<>();
    	while(rs.next()) {
    		for(int i=2; i<cols; i++) {
    			join.add(rs.getObject(i));
    		}
    		rows.add(new DataHolder(join.stream().map(String::valueOf).collect(joining("_")), rs.getObject(1), rs.getObject(cols)));
    		join.clear();
    	}
    	var headers = rows.stream().map(DataHolder::getKey).distinct().sorted().collect(toList());
    	var sb1 = new StringBuilder();
    	sb1.append("[")
    	.append(doubleQuote(xType.typeName())).append(",")
    	.append(doubleQuote(rsm.getColumnName(1)))
    	.append("]");
    	headers.forEach(s-> sb1.append(",[").append(doubleQuote(NUMBER.typeName())).append(",").append(doubleQuote(s)).append("]"));
    	var sb2= new StringBuilder();
    	rows.stream().collect(groupingBy(DataHolder::getXValue))
    		.entrySet().stream()
    		.sorted(comparing(e-> (Comparable<Object>)e.getKey()))
    		.forEach(e->{
    			sb2.append("[").append(xType.format(e.getKey()));
    			var map = e.getValue().stream().collect(toMap(DataHolder::getKey, o-> NUMBER.format(o.getYValue())));
    			headers.forEach(s-> sb2.append(",").append(map.getOrDefault(s, "0")));
    			sb2.append("],");
    		});
		sb2.deleteCharAt(sb2.length()-1); //dirty but less code
		return new StringBuilder[]{sb1, sb2};
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
    
    @Getter
    @RequiredArgsConstructor
    static class DataHolder {
    	
    	private final String key;
    	private final Object xValue;
    	private final Object yValue;
    }

}
