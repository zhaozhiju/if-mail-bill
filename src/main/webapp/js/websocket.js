var host = window.location.host.split(":")[0];

		WEB_SOCKET_DEBUG = false;

		var ws;
		var series;

		function reload() {
			setTimeout(init,1000);
		}

		function init() {

			ws = new WebSocket("ws://" + host + ":8186/");

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