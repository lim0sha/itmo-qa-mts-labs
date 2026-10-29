/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.998805256869773, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "GET /get?postId=11&limit=10 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=2&limit=5 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=19&limit=20 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=15&limit=15 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=17&limit=15 - echo query"], "isController": false}, {"data": [0.9772727272727273, 500, 1500, "GET /get?postId=1&limit=5 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=9&limit=10 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=3&limit=5 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=7&limit=10 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=13&limit=15 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=4&limit=5 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=12&limit=10 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=18&limit=15 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=20&limit=20 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=10&limit=10 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=16&limit=15 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=8&limit=10 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=6&limit=10 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=5&limit=5 - echo query"], "isController": false}, {"data": [1.0, 500, 1500, "GET /get?postId=14&limit=15 - echo query"], "isController": false}, {"data": [0.9987893462469734, 500, 1500, "GET /headers - echo request headers"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 837, 0, 0.0, 187.90919952210285, 160, 1365, 180.0, 194.0, 216.0, 351.06000000000006, 14.20208704504963, 17.897050908840246, 3.4212082166793922], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET /get?postId=11&limit=10 - echo query", 21, 0, 0.0, 194.04761904761907, 172, 320, 183.0, 280.4000000000001, 318.2, 320.0, 0.4068348250610252, 0.5242274073482118, 0.09495461248982913], "isController": false}, {"data": ["GET /get?postId=2&limit=5 - echo query", 22, 0, 0.0, 195.77272727272722, 165, 480, 176.0, 280.9, 453.5999999999996, 480.0, 0.38184500564089213, 0.49022200164887614, 0.08837623665712054], "isController": false}, {"data": ["GET /get?postId=19&limit=20 - echo query", 21, 0, 0.0, 182.19047619047618, 164, 312, 176.0, 192.6, 300.1999999999998, 312.0, 0.4165757473567277, 0.5364691300013885, 0.097228128533455], "isController": false}, {"data": ["GET /get?postId=15&limit=15 - echo query", 21, 0, 0.0, 180.47619047619048, 165, 189, 181.0, 187.0, 188.8, 189.0, 0.40963620403784257, 0.5275704367014532, 0.09560844996586365], "isController": false}, {"data": ["GET /get?postId=17&limit=15 - echo query", 21, 0, 0.0, 188.38095238095238, 169, 317, 179.0, 221.60000000000002, 308.1999999999999, 317.0, 0.41379310344827586, 0.5330972906403941, 0.09657866379310345], "isController": false}, {"data": ["GET /get?postId=1&limit=5 - echo query", 22, 0, 0.0, 231.13636363636365, 164, 1365, 175.5, 193.9, 1189.6499999999974, 1365.0, 0.3765962545790681, 0.48350024286178916, 0.08716143782738198], "isController": false}, {"data": ["GET /get?postId=9&limit=10 - echo query", 21, 0, 0.0, 194.04761904761904, 165, 385, 181.0, 263.20000000000005, 374.1999999999998, 385.0, 0.40069453719780956, 0.5154209260336965, 0.09313017563777214], "isController": false}, {"data": ["GET /get?postId=3&limit=5 - echo query", 22, 0, 0.0, 185.90909090909093, 169, 293, 180.5, 196.4, 278.5999999999998, 293.0, 0.38737850401464996, 0.49736043368784333, 0.08965693891745316], "isController": false}, {"data": ["GET /get?postId=7&limit=10 - echo query", 21, 0, 0.0, 194.47619047619045, 171, 300, 184.0, 274.20000000000005, 299.4, 300.0, 0.39435138586344176, 0.5070048718827461, 0.09165588851122962], "isController": false}, {"data": ["GET /get?postId=13&limit=15 - echo query", 21, 0, 0.0, 179.28571428571428, 166, 192, 178.0, 189.4, 191.8, 192.0, 0.40526457987571884, 0.5221475283685206, 0.09458811971708674], "isController": false}, {"data": ["GET /get?postId=4&limit=5 - echo query", 22, 0, 0.0, 200.1818181818182, 167, 356, 181.5, 321.4, 351.79999999999995, 356.0, 0.39397575258322737, 0.5060231001862431, 0.09118384117404774], "isController": false}, {"data": ["GET /get?postId=12&limit=10 - echo query", 21, 0, 0.0, 193.09523809523807, 167, 343, 176.0, 311.60000000000014, 342.8, 343.0, 0.4058755315036722, 0.5230290575473521, 0.09473071487243911], "isController": false}, {"data": ["GET /get?postId=18&limit=15 - echo query", 21, 0, 0.0, 186.28571428571428, 168, 311, 181.0, 194.0, 299.39999999999986, 311.0, 0.4160393058087012, 0.5357782968638561, 0.09710292391433553], "isController": false}, {"data": ["GET /get?postId=20&limit=20 - echo query", 21, 0, 0.0, 178.47619047619048, 160, 202, 180.0, 188.8, 200.7, 202.0, 0.41939606965969006, 0.5405692365493688, 0.0978863873522128], "isController": false}, {"data": ["GET /get?postId=10&limit=10 - echo query", 21, 0, 0.0, 181.95238095238093, 171, 218, 180.0, 191.20000000000002, 215.49999999999997, 218.0, 0.4036986485707146, 0.5200360985889771, 0.09422263379726639], "isController": false}, {"data": ["GET /get?postId=16&limit=15 - echo query", 21, 0, 0.0, 198.3809523809524, 164, 327, 183.0, 306.80000000000007, 326.2, 327.0, 0.41006014215418257, 0.5285740829785988, 0.0957073964598141], "isController": false}, {"data": ["GET /get?postId=8&limit=10 - echo query", 21, 0, 0.0, 188.95238095238096, 172, 304, 181.0, 202.2, 293.89999999999986, 304.0, 0.397373550059606, 0.5107055804965277, 0.09235830558026], "isController": false}, {"data": ["GET /get?postId=6&limit=10 - echo query", 21, 0, 0.0, 188.33333333333331, 171, 336, 180.0, 203.4, 322.99999999999983, 336.0, 0.3931995206710605, 0.5057433752902187, 0.09138816984346915], "isController": false}, {"data": ["GET /get?postId=5&limit=5 - echo query", 21, 0, 0.0, 194.8095238095238, 168, 488, 182.0, 189.2, 458.1999999999996, 488.0, 0.3948852952237683, 0.5072138550676947, 0.09139435055471982], "isController": false}, {"data": ["GET /get?postId=14&limit=15 - echo query", 21, 0, 0.0, 190.80952380952382, 173, 313, 181.0, 253.60000000000005, 308.49999999999994, 313.0, 0.40682694356729115, 0.5240659023324745, 0.09495277296150642], "isController": false}, {"data": ["GET /headers - echo request headers", 413, 0, 0.0, 184.26150121065368, 163, 841, 179.0, 192.0, 200.5999999999999, 339.0400000000002, 7.301463828583551, 9.00181583582314, 1.820290124902765], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 837, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
