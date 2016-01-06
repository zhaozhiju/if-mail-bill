<%@ page  pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script type="text/javascript" src="js/jquery-1.9.1.min.js"></script>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<script type="text/javascript">
		 function init(){
			var integrationView = $('<div></div>').css({'background':'#FFFFE0'}).attr('id','newView').appendTo('body');
		  	var data = '${view }';
		  	var integration = JSON.parse(data).integrationBillView;
		  	if (integration != null){
				 $('<h1></h1>').attr("align","center").html("积分详细").appendTo(integrationView);
		  	}
		  	var table = $('<table></table>').css({'border':'1px solid #23CF5F','width':'80%'}).attr("align","center");
			for(var i = 0;i<integration.length;i++){
				var tr = $('<tr></tr>');
				$('<td></td>').html("上期积分余额:").attr("align","center").appendTo(tr);
				$('<td></td>').html(integration[i].balancePoints).attr("align","center").appendTo(tr);
				$('<td></td>').html("本期新增积分:").attr("align","center").appendTo(tr);
				$('<td></td>').html(integration[i].addedPoints).attr("align","center").appendTo(tr);
				$('<td></td>').html("本期调整积分:").attr("align","center").appendTo(tr);
				$('<td></td>').html(integration[i].revisePoints).attr("align","center").appendTo(tr);
				$('<td></td>').html("本期奖励积分:").attr("align","center").appendTo(tr);
				$('<td></td>').html(integration[i].awardPoints).attr("align","center").appendTo(tr);
				$('<td></td>').html("本期兑换积分总数:").attr("align","center").appendTo(tr);
				$('<td></td>').html(integration[i].exchangePoints).attr("align","center").appendTo(tr);
				$('<td></td>').html("可用积分余额:").attr("align","center").appendTo(tr);
				$('<td></td>').html(integration[i].usePoints).attr("align","center").appendTo(tr);
				if (integration[i].impendingFailurePoints != null){
					$('<td></td>').html("即将失效积分:").attr("align","center").appendTo(tr);
					$('<td></td>').html(integration[i].impendingFailurePoints).attr("align","center").appendTo(tr);
				}
				if (integration[i].tourismPoints != null){
					$('<td></td>').html("旅游积分:").attr("align","center").appendTo(tr);
					$('<td></td>').html(integration[i].tourismPoints).attr("align","center").appendTo(tr);
				}
				table.append(tr);
			}
			integrationView.append(table);
		  }
	  init();
  </script>