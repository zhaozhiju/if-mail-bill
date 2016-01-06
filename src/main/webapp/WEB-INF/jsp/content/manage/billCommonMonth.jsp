<%@ page  pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
</head>
<body>
  	<jsp:include page="billCycleMonth.jsp"></jsp:include>
  	<jsp:include page="billPartBalance.jsp"></jsp:include>
  	<jsp:include page="billPartIntegration.jsp"></jsp:include>
  	<jsp:include page="billPartMonth.jsp"></jsp:include>
  </body>
</html>
