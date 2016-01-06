<%@ page pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>My JSP 'billUserInfo-save.jsp' starting page</title>
    <base target="_blank">
  <script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
  
  </head>
  <body>
  <script type="text/javascript">
	  var newView = $('<div></div>').css({'border':'1px solid #FFCF5F',
	  	'background':'#FFFFE0'}).attr('id','newView').appendTo('body');
	  var newNotView = $('<div></div>').css({'border':'1px solid #FFCF5F',
	  	'background':'#FFFFE0'}).attr('id','newNotView').appendTo('body');
		 function initView(){
		  	var data = '${jsp}';
		  	var dd = JSON.parse(data).view;
		  	var table = $('<table></table>').css({"width":"80%"}).attr('align','center');//内容居中,"text-align":"center","vertical-align":"middle"
			$('<th></th>').html("序号").appendTo(table);
			$('<th></th>').html("原始地址").appendTo(table);
			$('<th></th>').html("截取后的地址").appendTo(table);
			$('<th></th>').html("账期").appendTo(table);
			$('<th></th>').html("卡号").appendTo(table);
			$('<th></th>').html("详情").appendTo(table);
			for(var i = 0;i<dd.length;i++){
				var tr = $('<tr></tr>');
				$('<td></td>').html(i+1).appendTo(tr);
				$('<td></td>').html("<a href='"+dd[i].oldDFS+"'><sub>"+dd[i].oldHtml+"</sub>     "+dd[i].newHtml+"</a>").appendTo(tr);
				$('<td></td>').html("<a href='"+dd[i].newDFS+"'>第二份账单（经过处理）</a>").appendTo(tr);
				$('<td></td>').html(dd[i].accountOfDate).appendTo(tr);
				$('<td></td>').html(dd[i].cardEndOfFour).appendTo(tr);
				$('<td></td>').html("<a href='<%=request.getContextPath() %>/findBill.do?billCyclePkId="+dd[i].id+"&voBillType="+dd[i].bankBillType+"'>详情</a>").appendTo(tr);
				table.append(tr);
			}   
			newView.append(table);
		  }
		function initNotView(){
			var dataNotView = '${jsp }';
		  	var noview = JSON.parse(dataNotView).notview;
		  	var tableNoview = $('<table></table>').css({"width":"80%"}).attr('align','center');
			$('<th></th>').html("序号").appendTo(tableNoview);
			$('<th></th>').html("原始地址").appendTo(tableNoview);
			$('<th></th>').html("截取后的地址").appendTo(tableNoview);
			$('<th></th>').html("是否账单").appendTo(tableNoview);
			for(var i = 0; i < noview.length; i ++){
				var trn = $('<tr></tr>');
				$('<td></td>').html(1+i).appendTo(trn);
				$('<td></td>').html("<a href='"+noview[i].oldDFS+"'><sub>"+noview[i].oldHtml+"   "+noview[i].newHtml+"</sub></a>").appendTo(trn);
				$('<td></td>').html("<a href='"+noview[i].newDFS+"'>第二份账单（经过处理）</a>").appendTo(trn);
 				if (noview[i].bankBillType == 0){
 					$('<td></td>').html("非账单").appendTo(trn);
 				}
				console.log(noview[i].id);
				tableNoview.append(trn);
			}
			newNotView.append(tableNoview);
		}
	  initView();
	  initNotView();
  </script>
  </body>
</html>
