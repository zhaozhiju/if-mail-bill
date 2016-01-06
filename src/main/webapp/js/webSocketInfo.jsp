<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<html lang="UTF-8">
<head>
<script type="text/javascript" src="swfobject.js"></script>
<script type="text/javascript" src="web_socket.js"></script>
<script type="text/javascript" src="jquery-1.9.1.min.js"></script>
<script src="highcharts.js"></script>
<script src="exporting.js"></script>
<meta http-equiv="" charset="UTF-8">
<!-- <script type="text/javascript" src="../../../../js/swfobject.js"></script> -->
<!-- <script type="text/javascript" src="../../../../js/web_socket.js"></script> -->
<!-- <script type="text/javascript" src="../../../../js/jquery-1.9.1.min.js"></script> -->
<!-- <script type="text/javascript" src="../../../../js/highcharts.js"></script> -->
<!-- <script type="text/javascript" src="../../../../js/exporting.js"></script> -->
</head>
<body>
 <script type="text/javascript">
		var host = window.location.host.split(":")[0];

		WEB_SOCKET_DEBUG = false;

		var ws;
		var series;

		function reload() {
			setTimeout(init,1000);
		}

		function init() {

			ws = new WebSocket("ws://" + host + ":8186/if-mail-bill/");

			ws.onopen = function() {
				//output("onOpen");
			};
			ws.onmessage = function(e) {
				var dStr = e.data;
				outputmem(dStr);
				ws.send("woshuo" + dStr);
			};
			ws.onclose = function() {
				//output("onClose");
				ws.close();
				reload();
			};
			ws.onerror = function() {
				//output("onError");
			};

		}

		function outputmem(str) {
			var mem = document.getElementById("mem");
			mem.innerHTML = str;
		}

		var i = 0;
		function output(str) {
			i++;
			var log = document.getElementById("log");

			var escaped = str.replace(/&/, "&amp;").replace(/</, "&lt;")
					.replace(/>/, "&gt;").replace(/"/, "&quot;"); // "
			log.innerHTML = escaped + "<br>" + log.innerHTML;
			
		}

		$(function() {
			$(document).ready(function() {
				init();
			});

		});
	</script>
	看我的变化
	<div id="mem" style="min-width: 400px; height: 400px" ></div>
	<div id="log" style="min-width: 400px; height: 400px" ></div>
	<div id="container"
		style="min-width: 400px; height: 400px; margin: 0 auto"></div>
  </body>
</html>
