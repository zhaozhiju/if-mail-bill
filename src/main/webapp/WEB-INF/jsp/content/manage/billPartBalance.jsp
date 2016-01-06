<%@ page  pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<script type="text/javascript">
		 function init(){
			 var balanceView = $('<div></div>').css({'background':'#FFFFE0'}).attr('id','newView').appendTo('body');
		  	var data = '${view }';
		  	var balance = JSON.parse(data).balanceBillViews;
		  	var table = $('<table></table>').css({'border':'1px solid #23CF5F','width':'80%'}).attr("align","center");
			if (balance != null){
				$('<h1></h1>').attr("align","center").html("本期应还总额信息详情").appendTo(balanceView);
			}
			for(var i = 0 ; i < balance.length; i ++){
				var tr = $('<tr></tr>');
				$('<td></td>').html("上期账单金额:").attr("align","center").appendTo(tr);
				$('<td></td>').html(balance[i].balance).attr("align","center").appendTo(tr);
				$('<td></td>').html("本期账单金额:").attr("align","center").appendTo(tr);
				$('<td></td>').html(balance[i].newCharges).attr("align","center").appendTo(tr);
				$('<td></td>').html("上期还款金额:").attr("align","center").appendTo(tr);
				$('<td></td>').html(balance[i].payment).attr("align","center").appendTo(tr);
				$('<td></td>').html("本期调整金额:").attr("align","center").appendTo(tr);
				$('<td></td>').html(balance[i].adjustment).attr("align","center").appendTo(tr);
				$('<td></td>').html("循环利息:").attr("align","center").appendTo(tr);
				$('<td></td>').html(balance[i].interest).attr("align","center").appendTo(tr);
				$('<td></td>').html("本期应缴余额:").attr("align","center").appendTo(tr);
				$('<td></td>').html(balance[i].newBalance).attr("align","center").appendTo(tr);
				$('<td></td>').html("币种:").attr("align","center").appendTo(tr);
				$('<td></td>').html(balance[i].currencyType).attr("align","center").appendTo(tr);
				table.append(tr);
			}
			balanceView.append(table);
		  }
	  init();
  </script>