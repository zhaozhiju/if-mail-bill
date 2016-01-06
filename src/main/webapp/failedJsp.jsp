<%@ page pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
	<script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
  </head>
  <script type="text/javascript">
  	var eorr = '${error }';
  	var eorror = JSON.parse(eorr);
  	$("<div></div>").html(""+eorror).appendTo($("body"));
  </script>
  <body>
  	This is my err page.error code : ${error }
  </body>
</html>
