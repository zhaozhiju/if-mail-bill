<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
  
    <base href="<%=basePath%>">
    
    <title>邮箱账单输入</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->

  <script type="text/javascript" src="js/swfobject.js"></script>
  <script type="text/javascript" src="js/web_socket.js"></script>
  <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
  <script src="js/highcharts.js"></script>
  <script src="js/exporting.js"></script>
  <script type="text/javascript" >
  var host = window.location.host.split(":")[0];

		WEB_SOCKET_DEBUG = false;

		var ws;

		function reload() {
			setTimeout(init,1000);
		}

		function init() {
			ws = new WebSocket("ws://" + host + ":8186/if-mail-bill/");
			ws.onopen = function() {
// 				output("onOpen");
			};
			ws.onmessage = function(e) {
				var dStr = e.data;
				if(dStr.indexOf("L1`") > -1){
					var socketkey = dStr.split("`")[1];
					$("#keyId").val(socketkey);
				}
				outputmem(dStr);
				//ws.send("Client:" + dStr);
			};
			ws.onclose = function() {
// 				output("onClose");
				ws.close();
				reload();
			};
			ws.onerror = function() {
// 				output("onError");
			};
		}

		function outputmem(str) {
			var menlog = $("#menlog");
			var dolghtml = $("#dolg");
			var divlog = $("#divlog");
			var divlg = $("#lg");
			var divpic = $("#pic");
			
			if (str.indexOf("A1`正在查询") > -1){
				dolghtml.html(str);
			}else if (str.indexOf("A1`正在处理") > -1){
				divlog.html(str);
			}else if (str.indexOf("C1`已完成") > -1){
				divlg.html(str);
			}else if (str.indexOf("G1`") > -1){
				divpic.html(str);
			}else if(str.indexOf("I1`") > -1){
				var userMail = document.getElementById("emailUrl").value;
				if(userMail.indexOf("163.com") > -1 || userMail.indexOf("126.com") > -1 || userMail.indexOf("yeah.net") > -1){
					var imgUrl = str.substring(str.indexOf("验证码url为：")+8, str.length);
					document.getElementById("vcodediv").style.display = 'block';
					document.getElementById("vcodeimg").src = imgUrl;
					document.getElementById("button1").onclick=setVcodeTo163;
				}
			}else if (str.indexOf("H1`") > -1){
				var userMail = document.getElementById("emailUrl").value;
				if(userMail.indexOf("qq.com") > -1){
					if(str.indexOf("需要独立密码") > -1){
						document.getElementById("qqIpwdDiv").style.display = 'block';
						document.getElementById("button1").onclick=setqqIpwd;
						document.getElementById("refresh").onclick=refreshImgIpwd;
					}
				}
			}else if (str.indexOf("D1`") > -1){
				var userMail = document.getElementById("emailUrl").value;
				if(userMail.indexOf("qq.com") > -1){
					if(str.indexOf("登录验证码url为") > -1){
						var imgUrl = str.substring(str.indexOf("登录验证码url为：")+10, str.length);
						document.getElementById("vcodediv").style.display = 'block';
						document.getElementById("vcodeimg").src = imgUrl;
						document.getElementById("button1").onclick=setVcodeToqq;
					}
					if(str.indexOf("独立密码验证码url为") > -1){
						var imgUrl = str.substring(str.indexOf("独立密码验证码url为：")+12, str.length);
						document.getElementById("vcodediv").style.display = 'block';
						document.getElementById("vcodeimg").src = imgUrl;
						document.getElementById("button1").onclick=setqqIpwd2;
					}
				}
				if(userMail.indexOf("139.com") > -1){
					var imgUrl = str.substring(str.indexOf("验证码url为：")+8, str.length);
					document.getElementById("vcodediv").style.display = 'block';
					document.getElementById("vcodeimg").src = imgUrl;
					document.getElementById("button1").onclick=setVcodeTo139;
				}
			}else if (str.indexOf("success") > -1){
				$("<p></p>").html(str).appendTo(divlog);
			}else{
				$("<p></p>").html(str).appendTo(menlog);
			}
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
  
  </head>
  
  <body>
  <div align="center">
    <form action="" method="post">
    <div align="right"><h3>目前支持：招商、建设、工商、广发、民生、交通、中国、<br>农业银行、华夏、光大、兴业、中信、平安、浦发、北京</h3>
     <h3>如果发现问题，请及时提出！谢谢！</h3></div>
     <input type="hidden" name="key" id = "keyId"><br>
     	<span id="checkinfo" style="font-family:华文中宋; color:red;"></span><br>
    	邮 &nbsp;箱：<input type="text" name="emailUrl" id ="emailUrl"><br>
    	密 &nbsp;码：<input type="password" name="password" id ="password"><br>
    	<div id="qqIpwdDiv" style="display:none">独立密码：<input type="password" name="qqIpwd" id="qqIpwd"></div><br>
    	<div id="vcodediv" style="display:none">
    		验证码：<input type="text" name="vcode0" id="vcode">
    		<img id="vcodeimg" src="">
    		<a id="refresh" href="#" onclick="refreshVcode()">看不清，换一张</a>
    		<br>
    	</div>
    	<input id="button1" type = "button" value="提交信息" onclick="sub();">
    </form>
    </div>
    <div id="mem" style="min-width: 40px; height: 40px" >
    	<div id = "menlog"></div>
    	<div id = "dolg"></div>
    	<div id = "divlog"></div>
    	<div id = "lg"></div>
    	<div id = "pic"></div>
    </div>
	<div id="log" style="min-width: 40px; height: 40px" ></div>
	<div id="content-list"></div>
  </body>
  <script type="text/javascript">
  		function sub(){
		  	var emailUrl = $("#emailUrl").val();
		  	var password = $("#password").val();
		  	var men = $("#keyId").val();
			$.post(
			 "<%=path%>/webBindingMail.do",
			 {"emailUrl":emailUrl, 
			 "password":password,
			 "phoneId":"",
			 "accountId":0,
			 "key":men
			 }, 
			 function(date){
				$("body").html(date);
			 });
		}
		function setVcodeTo163(){
			var vcode = $("#vcode").val();
			ws.send("Client:RI1`" + vcode);
		}
		function setVcodeToqq(){
			var emailUrl = $("#emailUrl").val();
			var password = $("#password").val();
			var vcode = $("#vcode").val();
			if(vcode == null || vcode == undefined || vcode == ''){
				$("#checkinfo").html("验证码不能为空！");
			}
			else{
				$("#checkinfo").empty();
				ws.send("Client:RD1`" + emailUrl +"{umP}"+password+"{umP}"+vcode);
			}
		}
		function setqqIpwd(){
			var emailUrl = $("#emailUrl").val();
			var ipwd = $("#qqIpwd").val();
			if(ipwd == null || ipwd == undefined || ipwd == ''){
				$("#checkinfo").html("独立密码不能为空！");
			}
			else{
				$("#checkinfo").empty();
				ws.send("Client:RH1`" + emailUrl +"{umP}"+ipwd);
			}
		}
		function setqqIpwd2(){
			var emailUrl = $("#emailUrl").val();
			var ipwd = $("#qqIpwd").val();
			var vcode = $("#vcode").val();
			if(vcode == null || vcode == undefined || vcode == ''){
				$("#checkinfo").html("验证码不能为空！");
			}
			else{
				$("#checkinfo").empty();
				ws.send("Client:RD1`" + emailUrl +"{umP}"+ipwd+"{umP}"+vcode);
			}
		}
		function setVcodeTo139(){
			var emailUrl = $("#emailUrl").val();
			var password = $("#password").val();
			var vcode = $("#vcode").val();
			if(vcode == vcode == null || vcode == undefined || vcode == ''){
				$("#checkinfo").html("验证码不能为空！");
			}else{
				$("#checkinfo").empty();
				ws.send("Client:RD1`" + emailUrl +"{umP}"+password+"{umP}"+vcode);
			}
		}/*
		function refreshImg(){
			var emailUrl = $("#emailUrl").val();
			if(emailUrl.indexOf("163.com") > -1 || emailUrl.indexOf("126.com") > -1 || emailUrl.indexOf("yeah.net") > -1){
				ws.send("Client:RI1`" + "errorCodeToRefresh");
			}
			if(emailUrl.indexOf("qq.com") > -1){
				ws.send("Client:RD1`" + emailUrl +"{umP}"+"errorCodeToRefresh"+"{umP}"+"errorCodeToRefresh");
			}
			if(emailUrl.indexOf("139.com") > -1){
				ws.send("Client:RD1`" + emailUrl +"{umP}"+"errorCodeToRefresh"+"{umP}"+"errorCodeToRefresh");
			}
		}*/
		function refreshVcode(){
			ws.send("Client:RJ1`refreshVcode");
		}
		function refreshImgIpwd(){
			ws.send("Client:RJ1`refreshIpwd");
		}
  </script>
</html>
