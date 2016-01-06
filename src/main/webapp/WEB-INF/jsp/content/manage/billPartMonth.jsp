<%@ page  pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
 <script type="text/javascript">
	  var monthView = $('<div></div>').css({'background':'#FFFFE0'}).attr('id','newView').appendTo('body');
	 
		 function init(){
		  	var data = '${view}';
		  	var month = JSON.parse(data).monthBillViews;
		  	if (month != null){
		 		$('<h1></h1>').attr("align","center").html("详细交易显示").appendTo(monthView);
		 		$('<h3></h3>').attr("align","right").html("1：收入，0：支出（相对银行来说）").appendTo(monthView);
		  	}
		  	var table = $('<table></table>').css({'border':'1px solid #23CF5F','width':'80%'}).attr("align","center");
			$('<th></th>').html("主/附属卡").appendTo(table);
			$('<th></th>').html("卡号末四位").appendTo(table);
			$('<th></th>').html("交易日期").appendTo(table);
			$('<th></th>').html("记账日").appendTo(table);
			$('<th></th>').html("币种").appendTo(table);
			$('<th></th>').html("交易金额").appendTo(table);
			$('<th></th>').html("交易详细内容").appendTo(table);
			$('<th></th>').html("交易地点").appendTo(table);
			$('<th></th>').html("收入/支出").appendTo(table);
			for(var i = 0;i<month.length;i++){
				var tr = $('<tr></tr>');
				if (month[i].isMaster == 1){
					$('<td></td>').attr("align","center").html("主卡").appendTo(tr);
				}else if (month[i].isMaster == 2){
					$('<td></td>').attr("align","center").html("附属卡").appendTo(tr);
				}else{
					$('<td></td>').attr("align","center").html("").appendTo(tr);
				}
				$('<td></td>').attr("align","center").html(month[i].cardEndOfFour).appendTo(tr);
				$('<td></td>').attr("align","center").html(month[i].merchandiseDate).appendTo(tr);
				$('<td></td>').attr("align","center").html(month[i].postDate).appendTo(tr);
				$('<td></td>').attr("align","center").html(month[i].currencyType).appendTo(tr);
				$('<td></td>').attr("align","center").html(month[i].amount).appendTo(tr);
				$('<td></td>').attr("align","center").html(month[i].merchandiseDetail).appendTo(tr);
				$('<td></td>').attr("align","center").html(month[i].merchandiseArea).appendTo(tr);
				if(month[i].incomeOrPay != 0){
					$('<td></td>').attr("align","center").html("收入").appendTo(tr);
				}else{
					$('<td></td>').attr("align","center").html("支出").appendTo(tr);
				}
				table.append(tr);
			}
			monthView.append(table);
		  }
	  init();
  </script>
