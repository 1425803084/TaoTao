package com.ld.bean;

import java.util.List;

public class PageBean<T> {
	private int currentPage;//��ǰҳ
	private int currentCount;//��ǰҳ��ʾ����
	private int totalCont;//������
	private int totalPage;//��ҳ��
	private List<T> list;//��ǰҳ��ʾ����
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	public int getCurrentCount() {
		return currentCount;
	}
	public void setCurrentCount(int currentCount) {
		this.currentCount = currentCount;
	}
	public int getTotalCont() {
		return totalCont;
	}
	public void setTotalCont(int totalCont) {
		this.totalCont = totalCont;
	}
	public int getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
	public List<T> getList() {
		return list;
	}
	public void setList(List<T> list) {
		this.list = list;
	}
	

}
