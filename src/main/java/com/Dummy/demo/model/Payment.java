package com.Dummy.demo.model;

public class Payment {
	private int paymentId;
	private Integer orderId;
	private String method;
	private String status;
	public int getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(int paymentId) {
		this.paymentId = paymentId;
	}
	public Integer getOrderId() {
		return orderId;
	}
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Payment() {}
	public Payment(int paymentId, Integer orderId, String method, String status) {
		this.paymentId = paymentId;
		this.method = method;
		this.orderId = orderId;
		this.status = status;
	}
	
	

}
