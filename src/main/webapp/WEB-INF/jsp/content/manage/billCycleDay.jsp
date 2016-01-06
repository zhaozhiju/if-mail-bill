<%@ page  pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<script type="text/javascript">
		 function init(){
		  	var balanceView = $('<div></div>').css({'background':'#FFFFE0'}).attr('id','newView').appendTo('body');
		  	var data = '${view }';
		  	var balance = JSON.parse(data).cycleBillView;
		  	if (balance != null){
				$('<h1></h1>').attr("align","center").html("账期主要内容").appendTo(balanceView);
		  	}
		  	var table = $('<table></table>').css({'border':'1px solid #23CF5F','width':'80%'}).attr("align","center");
			var tr1 = $('<tr></tr>');
			var tr2 = $('<tr></tr>');
			var tr3 = $('<tr></tr>');
			$('<td></td>').html("姓名:").appendTo(tr1);
			$('<td></td>').html(balance.userName).appendTo(tr1);
			$('<td></td>').html("性别:").appendTo(tr1);
			$('<td></td>').html(balance.userGender).appendTo(tr1);
			$('<td></td>').html("卡号 末4位：").appendTo(tr2);
			$('<td></td>').html(balance.cardEndOfFour).appendTo(tr2);
			$('<td></td>').html("人民币可用额度：").appendTo(tr2);
			$('<td></td>').html(balance.rmbCreditLimit).appendTo(tr2);
			$('<td></td>').html("人民币取现额度：").appendTo(tr3);
			$('<td></td>').html(balance.cashRmbAdvanceLimit).appendTo(tr3);
			table.append(tr1,tr2,tr3);
			balanceView.append(table);
		  }
	  init();
  </script>