package net.umpay.mailbill.api.model;

import java.util.List;

import net.umpay.mailbill.api.model.viewpart.JspInfoPartView;

/**
 * 主列表页
 */
public class JspView {

	List<JspInfoPartView> view ;
	List<JspInfoPartView> notview ;
	
	public List<JspInfoPartView> getView() {
		return view;
	}
	public void setView(List<JspInfoPartView> view) {
		this.view = view;
	}
	public List<JspInfoPartView> getNotview() {
		return notview;
	}
	public void setNotview(List<JspInfoPartView> notview) {
		this.notview = notview;
	}


}
