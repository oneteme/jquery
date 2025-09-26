'use strict'
 
onhashchange = process;
onresize = draw;
let conf, data, meta, chart, elemId = 'chart';

function process(promise) {
	conf = contextConfiguration('chart');
	Promise.all([
		google.charts.load('current', {
			packages: packageFor(conf.type),
			language: window.navigator.language.substring(0, 2).toLowerCase()
		}),
		promise instanceof Promise? resolveData(promise) : Promise.resolve(data)
	]).then(draw);
};

function resolveData(promise){
	return promise.then(res=> {
		meta = columnMetadata("X-JQuery-Metadata", res.headers);
		return res.json();
	}).then(res=> data=res);
}

function draw() {
	if (chart) {
		chart.clearChart();
	}
	const builder = chartBuilder(conf.type);
	chart = new google.visualization[builder.fn](document.getElementById(elemId));
	chart.draw(builder.build(), builder.options ?? {});
}

function singleSerieDataTable() {
	if (conf.y.cols?.length > 0) {
		const yCols = conf.y.cols.map(v => createColumn(v, 'number'));
		const xCol = columnJoiner(conf.x.cols, 'string'); //force type
		if (yCols.length > 1) {
			let rows = data;
			if (rows?.length > 1) {
				rows = [yCols.reduce((acc, y) => {
					acc[y.id] = data.reduce((sum, o) => { sum += o[y.id]; return sum; }, 0);
					return acc;
				}, {})];
				yCols.forEach(c => c.id = `sum(${c.id})`);
			}
			return transposeDataTable(rows, xCol, ...yCols); //changed data
		}
		return createDataTable(data, xCol, ...yCols);
	}
	throw new Error(`pie chart require y:number,[x:string]`);
}

function multipleSeriesDataTable() {
	if (conf.y.cols?.length > 0) {
		const xCol = columnJoiner(conf.x.cols);
		const yCols = conf.y.cols.map(v => createColumn(v, 'number'));
		if (conf.stack) {
			if (yCols.length == 1) {
				const res = stack(xCol, yCols[0], conf.stack);
				return createDataTable(res.data, xCol, ...res.keys.map(v => ({ id: v, type: 'number', mapper: o => o[v] })))
			}
			else {
				throw new Error(`too many column to stack ${JSON.stringify(conf.y.cols)}`);
			}
		}
		return createDataTable(data, xCol, ...yCols);
	} //else auto column detect 
	throw new Error(`${conf.type} chart require x:[any], y:number[,number]*`);
}

function stack(xCol, yCol, stack) { //not groupby
	const set = new Set();
	const map = data.reduce((acc, o) => {
		const key = xCol.mapper(o);
		if (!acc[key]) {
			acc[key] = { [xCol.id]: key };
		}
		set.add(o[stack]);
		acc[key][o[stack]] = yCol.mapper(o);
		return acc;
	}, {});
	return { data: Object.values(map), keys: [...set] };
}

//date:date, value:Date, [popup:?]
function calendarDataTable() {
	if (conf.x.cols?.length > 0 && conf.y.cols?.length > 0) {
		return createDataTable(data,
			createColumn(conf.x.cols[0], 'date'),
			createColumn(conf.y.cols[0], 'number'));
	}//else auto column detect 
	throw new Error('calendar chart require x:date,y:number');
}

//group:string, [source:string], [target:string], value:Date, [popup:?]
function sankeyDataTable() {
	if (conf.x.cols?.length > 0 && conf.y.cols?.length > 0) {
		return createDataTable(data,
			createColumn(conf.x.cols[0], 'string'),
			conf.x.cols.length > 1
				? createColumn(conf.x.cols[1], 'string')
				: { id: '?', type: 'string', mapper: () => undefined },
			createColumn(conf.y.cols[0], 'number'));
	}//else auto column detect 
	throw new Error('sankey chart require x:string[,string*],y:number');
}

//group:string, [title:string], [popup:string], start:Date|number, end:Date|number
function timelineDataTable() {
	if (conf.x.cols?.length > 0 && conf.y.cols?.length > 0) {
		let cols = [createColumn(conf.y.cols[0], 'string')]
		if (conf.y.cols.length > 1) {
			cols.push(createColumn(conf.y.cols[1], 'string')); //optional title 
		}
		let xCols = conf.x.cols;
		cols.push(createColumn(xCols[0], 'date', 'number'));
		cols.push(createColumn(xCols[xCols.length > 1 ? 1 : 0], 'date', 'number')); //required, allow start=end 
		return createDataTable(data, ...cols);
	}//else auto column detect 
	throw new Error('timeline chart require x:date[,date],y:string[,string]');
}

function tableDataTable() {
	let cols = [...(conf.x.cols ?? []), ...(conf.y.cols ?? [])]
		.map(c => metadataColumn(c) ?? createColumn(c)) //meta|auto type
	if (!cols.length && meta) {
		cols = meta.map(c => createColumn(c.id, c.type)); //all meta column
	}//else auto column detect 
	if (cols?.length && conf.transpose) {
		if (!conf.x.cols?.length) {
			cols.unshift({ id: 'serie', type: 'string', mapper: (o, i) => '' + (i + 1) });
		}
		return transposeDataTable(data, ...cols)
	}
	return createDataTable(data, ...cols);
}

function transposeDataTable(data, ...cols) { //splice first column
	console.log('data', data);
	console.log('cols', cols);
	const dt = new google.visualization.DataTable();
	const xCol = cols[0];
	dt.addColumn('string', xCol.id);
	cols.splice(0, 1);
	const type = requireSameColumnsType(cols);
	data?.forEach((o, i) => dt.addColumn(type, xCol.mapper(o, i)));  //column{id:string, type:string}
	dt.addRows(cols.map(c => [c.id, ...data?.map(c.mapper)]));
	console.log('DataTable', dt)
	return dt;
}

function createDataTable(data, ...cols) {
	console.log('data', data)
	console.log('cols', cols)
	const dt = new google.visualization.DataTable();
	cols.forEach(cm => dt.addColumn(cm.type, cm.id)); //column{id:string, type:string}
	dt.addRows(data?.map((o, i) => cols.map(c => c.mapper(o, i))));
	console.log('DataTable', dt)
	return dt;
}

function metadataColumn(id) {
	let col = meta?.find(cm => cm.id == id);
	return col ? createColumn(col.id, col.type) : undefined;
}

function columnJoiner(cols, type) {
	if (!cols?.length) {
		return !type || type == 'number'
			? { id: 'serie', type: 'number', mapper: (o, i) => i + 1 }
			: { id: 'serie', type: 'string', mapper: (o, i) => i + 1 + '' };
	}
	if(cols.length > 1){
		return { id: cols.join('_'), type: 'string', mapper: o => cols.map(c => o[c]).join('_') };
	}
	return type 
		? createColumn(cols[0], type) 
		: createColumn(cols[0], meta?.find(o => o.id == cols[0])?.type)  //!type => auto
}

function createColumn(id, ...types) {
	const col = data?.map(o => o[id]).find(v => v);
	if (col) {
		const type = typeof col; //string, number, boolean
		if (!types?.length || types.indexOf(type) > -1) { //auto or same type
			return { id: id, type: type, mapper: o => o[id] };
		}
		if (type == 'number') { //=> string|Date
			return convertNumberColumn(id, ...types);
		}
		if (type == 'string') {
			return convertStringColumn(id, ...types);
		}
		if (type == 'boolean') {
			return convertBooleanColumn(id, ...types);
		}
	}
	return { id: id, type: 'string', mapper: o => toStringMapper(o[id]) };
}

function columnMetadata(key, headers) {
	if (headers.has(key)) {
		return headers.get(key)?.split(',').map(e => {
			const arr = e.split(':');
			return { id: arr[0], type: fromJavaType(arr[1]) };
		});
	}
}

function requireSameColumnsType(cols) {
	return cols.reduce((acc, o) => {
		if (acc.length == 0) {
			acc.push(o.type);
		}
		else if (acc.indexOf(o.type) == -1) {
			throw new Error(`require same columns type ${JSON.stringify(cols)}`)
		}
		return acc;
	}, [])[0];
}

function convertStringColumn(id, ...types) {
	for (let t of types) {
		if (t == 'timeofday') {
			return { id: id, type: t, mapper: o => o[id]?.split(':').map(Number) };
		}
		if (t == 'date' || t == 'datetime') {
			return { id: id, type: t, mapper: o => safeMapper(o[id], v => new Date(v)) };
		}
		if (t == 'number') {
			return { id: id, type: t, mapper: o => safeMapper(o[id], Number) };
		}
		if (t == 'boolean') {
			return { id: id, type: t, mapper: o => safeMapper(o[id], v => v == 'true') };
		}
	}
	throw new Error(`unsupported 'string' conversion to types='${types}'`);
}

function convertNumberColumn(id, ...types) {
	for (let t of types) {
		if (t == 'date' || t == 'datetime') {
			return { id: id, type: t, mapper: o => safeMapper(o[id], v => new Date(o[id])) };
		}
		if (t == 'boolean') {
			return { id: id, type: t, mapper: o => safeMapper(o[id], v => v == 1) };
		}
		if (t == 'string') {
			return { id: id, type: t, mapper: o => toStringMapper(o[id]) };
		}
		if (t == 'timeofday') {
			//??
		}
	}
	throw new Error(`unsupported 'number' conversion to types='${JSON.stringify(types)}'`);
}

function convertBooleanColumn(id, ...types) {
	for (let t of types) {
		if (t == 'integer') {
			return { id: id, type: t, mapper: o => safeMapper(o[id], v => v ? 1 : 0) };
		}
		if (t == 'string') {
			return { id: id, type: t, mapper: o=> toStringMapper(o[id]) };
		}
	}
	throw new Error(`unsupported 'boolean' conversion to types='${JSON.stringify(types)}'`);
}

function toStringMapper(o) {
	return safeMapper(o, v => v + '');
}

function safeMapper(v, fn) {
	return [undefined, null].indexOf(v) == -1 ? fn(v) : v;
}

function contextConfiguration(prefix) {
	const sp = new URLSearchParams(location.hash.slice(1));
	return {
		type: sp.get(`${prefix}.type`) || 'table',
		title: sp.get(`${prefix}.title`),
		transpose: sp.get(`${prefix}.transpose`) == 'true',
		stack: sp.get(`${prefix}.stack`), //require one argument
		x: {
			title: sp.get(`${prefix}.x.title`),
			color: sp.get(`${prefix}.x.color`),
			cols: sp.get(`${prefix}.x.cols`)?.split(',')
		},
		y: {
			title: sp.get(`${prefix}.y.title`),
			color: sp.get(`${prefix}.y.color`),
			cols: sp.get(`${prefix}.y.cols`)?.split(`,`),
			reduce: {
				by: sp.get(`${prefix}.x.merge.by`), //row|col
				apply: sp.get(`${prefix}.x.merge.apply`)//sum|avg|max|min|first|last
			}
		}
	};
}

function packageFor(chart) { //add config
	switch (chart) {
		case 'pie':
		case 'donut':
		case 'pie3d':
		case 'bar':
		case 'column':
		case 'histogram':
		case 'line':
		case 'area':
		case 'combo': return 'corechart';
		case 'calendar':
		case 'timeline':
		case 'sankey':
		case 'table': return chart;
		default: throw new Error(`unsupported chart type='${chart}'`);
	}
}

function chartBuilder(chart) {
	const sp = new URLSearchParams(location.hash.slice(1));
	switch (chart) {
		case 'table': return { fn: 'Table', build: tableDataTable, options: { width: '100%' } };
		case 'pie': return { fn: 'PieChart', build: singleSerieDataTable, options: { title: sp.get('title') } };
		case 'donut': return { fn: 'PieChart', build: singleSerieDataTable, options: { pieHole: 0.4 }, title: sp.get('title') };
		case 'pie3d': return { fn: 'PieChart', build: singleSerieDataTable, options: { is3D: true, title: sp.get('title') } };
		case 'bar':
		case 'line':
		case 'area':
		case 'combo':
		case 'column': return { fn: chart.charAt(0).toUpperCase() + chart.slice(1) + 'Chart', build: multipleSeriesDataTable, options: { isStacked: 'true' } };
		case 'histogram': return { fn: 'Histogram', build: multipleSeriesDataTable };
		case 'sankey': return { fn: 'Sankey', build: sankeyDataTable };
		case 'calendar': return { fn: 'Calendar', build: calendarDataTable, options: { title: sp.get('title') } };
		case 'timeline': return { fn: 'Timeline', build: timelineDataTable };
		default: throw Error(`unsupported chart type='${chart}'`);
	}
}

function fromJavaType(type) {
	switch (type) {
		case 'Byte':
		case 'Short':
		case 'Integer':
		case 'Long':
		case 'Float':
		case 'Double':
		case 'BigDecimal': return 'number';
		case 'Boolean': return 'boolean';
		case 'String': return 'string';
		case 'Date': return 'date';
		case 'Time': return 'timeofday';
		case 'Timestamp': return 'datetime';
		default: throw new Error(`unsupported data type='${type}'`); //null | Object
	}
}
