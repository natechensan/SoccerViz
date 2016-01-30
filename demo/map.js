
	
	//Global variables for selecting data	
	// var year = 2014;
	// var league = 1; //Premier League
	// var team = 142; //Arsenal
     
    var width = window.innerWidth * 1024/1426 - 50, 
        height = window.innerHeight * 768/945; 
     
    var projection = d3.geo.mercator();
     
    var path = d3.geo.path().projection(projection);

	var map_svg = d3.select("#container").append("svg")
											.attr("width", width)
											.attr("height", height)
											.attr("z-index", 1);

	//Tooltip
	var div = d3.select("#container").append("div")
		.attr("class", "tooltip")
		.style("opacity", 0);			
	
	// var table = d3.selectAll("#container").append("table");
	// 	thead = table.append("thead");
	// 	tbody = table.append("tbody");
		
	// thead.append("th").text("Name");
	// thead.append("th").text("Position");
	// thead.append("th").text("Appearances");
	// thead.append("th").text("Nationality");
	
	//Region Definitions
	
	var north_america = [
        "BMU",
		"CAN",
		"GRL",
		"MEX",
		"USA"
    ];
	
	var central_america = [
		"BLZ",
		"CRI",
		"SLV",
		"GTM",
		"HND",
		"NIC",
		"PAN"
	];
	
	var south_america = [
		"ARG",
		"BOL",
		"BRA",
		"CHL",
		"COL",
		"ECU",
		"FLK",
		"GUF",
		"GUY",
		"PRY",
		"PER",
		"SGS",
		"SUR",
		"URY",
		"VEN"
	];
	
	var caribbean = [
		"AIA",
		"ATG",
		"ABW",
		"BHS",
		"BRB",
		"BES",
		"CYM",
		"CUB",
		"CUW",
		"DMA",
		"DOM",
		"GRD",
		"GLP",
		"HTI",
		"JAM",
		"MTQ",
		"MSR",
		"PRI",
		"BLM",
		"KNA",
		"LCA",
		"MAF",
		"VCT",
		"SXM",
		"TTO",
		"TCA",
		"VGB",
		"VIR"
	];
	
	var west_europe = [
		"ALD",
		"AND",
		"AUT",
		"BEL",
		"DNK",
		"ENG",
		"FRO",
		"FIN",
		"FXX",
		"DEU",
		"GIB",
		"GRC",
		"GGY",
		"VAT",
		"ISL",
		"IRL",
		"IMN",
		"ITA",
		"JEY",
		"LIE",
		"LUX",
		"MLT",
		"MCO",
		"NLD",
		"NOR",
		"NIR",
		"PRT",
		"SCT",
		"SMR",
		"ESP",
		"SJM",
		"SWE",
		"CHE",
		"WLS"
	];
	
	var east_europe = [
		"ALB",
		"ARM",
		"AZE",
		"BLR",
		"BIH",
		"BGR",
		"HRV",
		"CZE",
		"EST",
		"GEO",
		"HUN",
		"LVA",
		"LTU",
		"MKD",
		"MDA",
		"MNE",
		"POL",
		"ROU",
		"RUS",
		"SRB",
		"SVK",
		"SVN",
		"UKR"
	];
	
	var middle_east = [
		"BHR",
		"CYP",
		"EGY",
		"IRN",
		"IRQ",
		"ISR",
		"JOR",
		"KWT",
		"LBN",
		"OMN",
		"QAT",
		"SAU",
		"SYX",
		"TUR",
		"ARE",
		"YEM"
	];
	
	var east_asia = [
		"CHN",
		"HKQ",
		"JPN",
		"PRK",
		"KOR",
		"MAC",
		"MNG",
		"TWN"
	];
	
	var se_asia = [
		"BRN",
		"KHM",
		"CXR",
		"CCK",
		"IDN",
		"LAO",
		"MYS",
		"MMR",
		"PHL",
		"SGP",
		"THA",
		"TLS",
		"VNM"
	];
	
	var south_asia = [
		"AFG",
		"BGD",
		"BTN",
		"IOT",
		"IND",
		"MDV",
		"NPL",
		"PAK",
		"LKA"
	];
	
	var africa = [
		"DZA",
		"AGO",
		"BEN",
		"BWA",
		"BFA",
		"BFA",
		"BDI",
		"CMR",
		"CPV",
		"CAF",
		"TCD",
		"COM",
		"COG",
		"COD",
		"CIV",
		"DJI",
		"GNQ",
		"ERI",
		"ETH",
		"ATF",
		"GAB",
		"GMB",
		"GHA",
		"GIN",
		"GNB",
		"KEN",
		"LSO",
		"LBR",
		"LBY",
		"MDG",
		"MWI",
		"MLI",
		"MRT",
		"MYT",
		"MUS",
		"MAR",
		"MOZ",
		"NAM",
		"NER",
		"NGA",
		"RWA",
		"REU",
		"SHN",
		"STP",
		"SYC",
		"SEN",
		"SLE",
		"SOM",
		"ZAF",
		"SDS",
		"SDN",
		"SWZ",
		"TZA",
		"TGO",
		"TUN",
		"UGA",
		"SAH",
		"ZMB",
		"ZWE"
	];
	
	var oceania = [
		"ASM",
		"AUS",
		"COK",
		"FJI",
		"PYF",
		"NRU",
		"NCL",
		"NZL",
		"NIU",
		"NFK",
		"MNP",
		"PLW",
		"PNG",
		"PCN",
		"WSM",
		"SLB",
		"TKL",
		"TON",
		"TUV",
		"VUT",
		"WLF"
	];
	
	var central_asia = [
		"KAZ",
		"KGZ",
		"TJK",
		"TKM",
		"UZB"
	];
	
	//North America
	function isNaCountry(datum) {
        var code = datum.id;
        return north_america.indexOf(code) > -1;
    }
	
	//Central America
	function isCaCountry(datum) {
        var code = datum.id;
        return central_america.indexOf(code) > -1;
    }
	
	//South America
	function isSaCountry(datum) {
        var code = datum.id;
        return south_america.indexOf(code) > -1;
    }
	
	//Caribbean
	function isCaribCountry(datum) {
        var code = datum.id;
        return caribbean.indexOf(code) > -1;
    }
	
	//West Europe
    function isWestEuCountry(datum) {
        var code = datum.id;
        return west_europe.indexOf(code) > -1;
    }
	
	//East Europe
    function isEastEuCountry(datum) {
        var code = datum.id;
        return east_europe.indexOf(code) > -1;
    }
	
	//Middle East
    function isMidEastCountry(datum) {
        var code = datum.id;
        return middle_east.indexOf(code) > -1;
    }
		
	//East Asia
    function isEastAsiaCountry(datum) {
        var code = datum.id;
        return east_asia.indexOf(code) > -1;
    }
	
	//Southeast Asia
    function isSEAsiaCountry(datum) {
        var code = datum.id;
        return se_asia.indexOf(code) > -1;
    }
	
	//South Asia
    function isSouthAsiaCountry(datum) {
        var code = datum.id;
        return south_asia.indexOf(code) > -1;
    }
	
	//Central Asia
    function isCentAsiaCountry(datum) {
        var code = datum.id;
        return central_asia.indexOf(code) > -1;
    }
	
	//Africa
    function isAfricaCountry(datum) {
        var code = datum.id;
        return africa.indexOf(code) > -1;
    }
	
	//Oceania
    function isOceaniaCountry(datum) {
        var code = datum.id;
        return oceania.indexOf(code) > -1;
    }
	
	//Draw Map
	queue()
			.defer(d3.json, "world.json")
			.await(ready_map);
	
	function ready_map(error, world) {
		if (error) return console.error(error);
	
		var map = topojson.feature(world, world.objects.world),
			countries = map.features;
		
		projection.scale(1).translate([0, 0])
         
		var b = path.bounds(map),
			s = .95 / Math.max((b[1][0] - b[0][0]) / width, (b[1][1] - b[0][1]) / height),
			t = [(width - s * (b[1][0] + b[0][0])) / 2, (height - s * (b[1][1] + b[0][1])) / 2];
         
		projection.scale(s).translate(t);
         
		map_svg.append("g")
			.selectAll("path")
			.data(countries)
		.enter().append("path")
			.attr("d", path)
			.attr("class", "country")
			.classed("namer-country", isNaCountry)
			.classed("camer-country", isCaCountry)
			.classed("samer-country", isSaCountry)
			.classed("carib-country", isCaribCountry)
			.classed("west-eu-country", isWestEuCountry)
			.classed("east-eu-country", isEastEuCountry)
			.classed("mid-east-country", isMidEastCountry)
			.classed("east-asia-country", isEastAsiaCountry)
			.classed("se-asia-country", isSEAsiaCountry)
			.classed("south-asia-country", isSouthAsiaCountry)
			.classed("africa-country", isAfricaCountry)
			.classed("oceania-country", isOceaniaCountry)
			.classed("cent-asia-country", isCentAsiaCountry)
			.style("opacity", 0.8);
	}		
	
	//Update Map Fill and Table
	function updateChart() {
	
		queue()
			.defer(d3.json, "soccerviz.json")
			.await(ready_fill);

		function ready_fill(error, data) {
			if (error) return console.error(error);
			
			//Find appearances by country per team per year
			var i = 0;
			var found = 0;
			var roster = team_data.roster;
			
			// while(i <  data.leagues.length && found == 0){
			// 	if (data.leagues[i].id_league === league){
			// 		var j = 0;
			// 		while (j < data.leagues[i].seasons.length && found == 0){
			// 			if (data.leagues[i].seasons[j].season == year){
			// 				var k = 0;
			// 				while (k < data.leagues[i].seasons[j].teams.length && found == 0){
			// 					if (data.leagues[i].seasons[j].teams[k].id_team == team){
			// 						found = 1;
			// 						roster = data.leagues[i].seasons[j].teams[k].roster
			// 					}
			// 				k = k + 1;
			// 				}
			// 			}
			// 		j = j + 1;
			// 		}
			// 	}
			// 	i = i + 1;
			// }
			
			var appearancesTotal = d3.nest()
				.key(function(d) { return d.nation_ISO_code; })
				.rollup(function(v) { return {
					value: d3.sum(v, function(d) { return d.appearances; })
				}; })
				.map(roster);
			
			//Define Color Scale
			var quantize = d3.scale.quantile()
                 .domain(d3.extent(d3.values(appearancesTotal), function (d) { return d.value; }))
                 .range(d3.range(9)),
            cb = "YlOrRd";

			//Fill
			function fill(datum, index) {
              var iso = datum.id,
					val = appearancesTotal[iso] && appearancesTotal[iso].value;
              if (val) {
                  var c = colorbrewer[cb][9][quantize(val)];
                  return c;
              } else {
                  return "lightgray";
              }
			}
			
			map_svg.selectAll("path")
				.on("mouseover", function(d) {
					d3.select(this).transition().duration(300).style("opacity", 1);
					div.transition().duration(300)
					.style("opacity", 1)
					.style("left", (d3.event.pageX) + "px")
					.style("top", (d3.event.pageY +30) + "px")
					if (appearancesTotal[d.id]) { div.text(appearancesTotal[d.id].value + " appearances from " + d.properties.name) }
					else { div.text("0 appearances from " + d.properties.name) };
					
				})
				.on("mouseout", function() {
					d3.select(this)
					.transition().duration(300)
					.style("opacity", 0.8);
					div.transition().duration(300)
					.style("opacity", 0);
				});
			
			map_svg.selectAll(".namer-country")
				.style("fill", fill);
			map_svg.selectAll(".camer-country")
				.style("fill", fill);
			map_svg.selectAll(".samer-country")
				.style("fill", fill);
			map_svg.selectAll(".carib-country")
				.style("fill", fill);
			map_svg.selectAll(".west-eu-country")
				.style("fill", fill);
			map_svg.selectAll(".east-eu-country")
				.style("fill", fill);	
			map_svg.selectAll(".mid-east-country")
				.style("fill", fill);		
			map_svg.selectAll(".east-asia-country")
				.style("fill", fill);			
			map_svg.selectAll(".se-asia-country")
				.style("fill", fill);				
			map_svg.selectAll(".south-asia-country")
				.style("fill", fill);				
			map_svg.selectAll(".africa-country")
				.style("fill", fill);				
			map_svg.selectAll(".oceania-country")
				.style("fill", fill);				
			map_svg.selectAll(".cent-asia-country")
				.style("fill", fill);	
				
			// console.log(roster)
				
			//Table
			//This needs work. Data isn't clearing and updating correctly
			// var tr = tbody.selectAll("tr")
			// 	.data(roster);
			
			// tr.enter().append('tr');
			
			// tr.exit().remove();
				
			// var td = tr.selectAll('td')
			// 	.data(function(d) { return [d.name, d.position, d.appearances, d.nation_name]; });
			// td.enter().append('td')
			// 	.text(function(d) { return d; });
				
			// td.exit().remove();
			
			
		};
	};
	
	// updateChart();
	
	//SLIDER
	d3.select('#slider3')
		.style("position","relative")
		.style("width", width+"px")
		.style("bottom", "0")
		.style("left", "24px")
		.style("z-index","3000")

	.call(d3.slider()
        .axis(d3.svg.axis().ticks(19).tickFormat(d3.format("d"))).min(1996).max(2015).step(1)
        .value(2015)
    .on("slide", function(evt, value) {
		year = value;
		team_data = findTeam(findYear(findLeague(league), year), team);
		getTeamList(findYear(findLeague(league), year));
		updateInfo();
    }))

