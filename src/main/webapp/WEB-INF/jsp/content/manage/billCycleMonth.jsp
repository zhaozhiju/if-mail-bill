<%@ page  pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<script type="text/javascript">
		 function init(){
		  	var balanceView = $('<div></div>').css({'background':'#FFFFE0'}).attr('id','newView').appendTo('body');
		  	var data = '${view }';
		  	var balance = JSON.parse(data).billView;
		  	if (balance != null){
				$('<h1></h1>').attr("align","center").html("账期主要内容").appendTo(balanceView);
		  	}
		  	var table = $('<table></table>').css({'border':'1px solid #23CF5F','width':'80%'}).attr("align","center");
			var tr1 = $('<tr></tr>');
			var tr2 = $('<tr></tr>');
			var tr3 = $('<tr></tr>');
			var tr4 = $('<tr></tr>');
			var tr5 = $('<tr></tr>');
			var tr6 = $('<tr></tr>');
			var tr7 = $('<tr></tr>');
			var tr8 = $('<tr></tr>');
			var tr9 = $('<tr></tr>');
			$('<td></td>').html("姓名:").appendTo(tr1);
			$('<td></td>').html(balance.userName).appendTo(tr1);
			$('<td></td>').html("性别:").appendTo(tr1);
			$('<td></td>').html(balance.userGender).appendTo(tr1);
			$('<td></td>').html("账单日：").appendTo(tr2);
			$('<td></td>').html(balance.billDate).appendTo(tr2);
			$('<td></td>').html("账期:").appendTo(tr2);
			$('<td></td>').html(balance.accountOfDate).appendTo(tr2);
			$('<td></td>').html("本期应还人民币总额:").appendTo(tr3);
			$('<td></td>').html(balance.newRmbBalance).appendTo(tr3);
			$('<td></td>').html("本期应还美元总额:").appendTo(tr3);
			$('<td></td>').html(balance.newUsaBalance).appendTo(tr3);
			$('<td></td>').html("本期人民币最低还款额 :").appendTo(tr4);
			$('<td></td>').html(balance.minRmbPayment).appendTo(tr4);
			$('<td></td>').html("本期美元最低还款额 :").appendTo(tr4);
			$('<td></td>').html(balance.minUsaPayment).appendTo(tr4);
			$('<td></td>').html("人民币信用额度：").appendTo(tr5);
			$('<td></td>').html(balance.rmbCreditLimit).appendTo(tr5);
			$('<td></td>').html("美元信用额度 :").appendTo(tr5);
			$('<td></td>').html(balance.usaCreditLimit == 0.0 ? "" : balance.usaCreditLimit).appendTo(tr5);
			$('<td></td>').html("人民币预借现金额度:").appendTo(tr6);
			$('<td></td>').html(balance.cashRmbAdvanceLimit).appendTo(tr6);
			$('<td></td>').html("美元预借现金额度 :").appendTo(tr6);
			$('<td></td>').html(balance.cashUsaAdvanceLimit).appendTo(tr6);
			$('<td></td>').html("人民币可用积分:").appendTo(tr7);
			$('<td></td>').html(balance.rmbIntegration).appendTo(tr7);
			$('<td></td>').html("美元可用积分:").appendTo(tr7);
			$('<td></td>').html(balance.usaIntegration).appendTo(tr7);
			$('<td></td>').html("卡号 末4位：").appendTo(tr8);
			$('<td></td>').html(balance.cardEndOfFour).appendTo(tr8);
			$('<td></td>').html("逾期还款额：").appendTo(tr8);
			$('<td></td>').html(balance.pastDueAmount).appendTo(tr8);
			$('<td></td>').html("到期还款日:").appendTo(tr9);
			$('<td></td>').html(balance.paymentDueDate).appendTo(tr9);
			table.append(tr1,tr2,tr3,tr4,tr5,tr6,tr7,tr8,tr9);
			balanceView.append(table);
		  }
	  init();
  </script>