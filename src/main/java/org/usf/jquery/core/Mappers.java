package org.usf.jquery.core;

import static org.usf.jquery.core.ResultSetMapper.DataWriter.usingRowWriter;
import static org.usf.jquery.web.view.Chart2DView.areaChart;
import static org.usf.jquery.web.view.Chart2DView.barChart;
import static org.usf.jquery.web.view.Chart2DView.columnChart;
import static org.usf.jquery.web.view.Chart2DView.comboChart;
import static org.usf.jquery.web.view.Chart2DView.lineChart;

import java.io.Writer;

import org.usf.jquery.core.ResultSetMapper.DataWriter;
import org.usf.jquery.web.view.CalendarView;
import org.usf.jquery.web.view.PieChartView;
import org.usf.jquery.web.view.SankeyView;
import org.usf.jquery.web.view.TableView;
import org.usf.jquery.web.view.TimelineChartView;
import org.usf.jquery.web.view.WebViewMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Mappers {
	
	public KeyValueMapper keyValue() {
		return new KeyValueMapper();
	}
	
	public static AsciiResultMapper log() {
		return new AsciiResultMapper(usingRowWriter(log::debug));
	}
	
	public static AsciiResultMapper ascii(Writer w) {
		return ascii(w::write);
	}
	
	public static AsciiResultMapper ascii(DataWriter out) {
		return new AsciiResultMapper(out);
	}
	
	public static CsvResultMapper csv(Writer w) {
		return csv(w::write);
	}
	
	public static CsvResultMapper csv(DataWriter out) {
		return new CsvResultMapper(out);
	}
	
	public static WebViewMapper webChart(String view, Writer w) {
		switch (view) {
		case "table"	: return new TableView(w);
		case "pie"		: return new PieChartView(w);
		case "column"	: return columnChart(w);
		case "bar"		: return barChart(w);
		case "area"		: return areaChart(w);
		case "combo"	: return comboChart(w);
		case "line"		: return lineChart(w);
		case "timeline"	: return new TimelineChartView(w);
		case "calendar"	: return new CalendarView(w);
		case "sankey"	: return new SankeyView(w);
		default: throw new IllegalArgumentException(view);
		}
	}

}
