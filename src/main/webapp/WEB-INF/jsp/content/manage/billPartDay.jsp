<%@ page  pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
 <script type="text/javascript">
	  var dayView = $('<div></div>').css({'background':'#FFFFE0'}).attr('id','newView').appendTo('body');
	 
		 function init(){
		  	var data = '${view}';
		  	var day = JSON.parse(data).billView;
		  	if (day != null){
		 		$('<h1></h1>').attr("align","center").html("日详细交易显示").appendTo(dayView);
		 		$('<h3></h3>').attr("align","right").html("1：收入，0：支出（相对银行来说）").appendTo(dayView);
		  	}
		  	var table = $('<table></table>').css({'border':'1px solid #23CF5F','width':'80%'}).attr("align","center");
			$('<th></th>').html("卡号末四位").appendTo(table);
			$('<th></th>').html("交易日期").appendTo(table);
			$('<th></th>').html("交易时间").appendTo(table);
			$('<th></th>').html("币种").appendTo(table);
			$('<th></th>').html("交易金额").appendTo(table);
			$('<th></th>').html("交易摘要").appendTo(table);
			$('<th></th>').html("收入/支出").appendTo(table);
			for(var i = 0;i<day.length;i++){
				var tr = $('<tr></tr>');
				$('<td></td>').attr("align","center").html(day[i].cardEndOfFour).appendTo(tr);
				$('<td></td>').attr("align","center").html(day[i].merchandiseDate).appendTo(tr);
				$('<td></td>').attr("align","center").html(day[i].merchandiseTime).appendTo(tr);
				$('<td></td>').attr("align","center").html(day[i].currencyType).appendTo(tr);
				$('<td></td>').attr("align","center").html(day[i].merchandiseAmount).appendTo(tr);
				$('<td></td>').attr("align","center").html(day[i].merchandiseDetail).appendTo(tr);
				if(day[i].incomeOrPay != 0){
					$('<td></td>').attr("align","center").html("收入").appendTo(tr);
				}else{
					$('<td></td>').attr("align","center").html("支出").appendTo(tr);
				}
				table.append(tr);
			}
			dayView.append(table);
		  }
	  init();
  </script>