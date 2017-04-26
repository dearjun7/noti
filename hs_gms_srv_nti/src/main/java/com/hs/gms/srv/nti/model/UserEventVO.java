package com.hs.gms.srv.nti.model;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.net.SocketAddress;

import java.io.Serializable;
import java.util.List;

public class UserEventVO implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2281790080716812663L;
	
	EventBus eventBus = null;
	List<String> connectedUserList = null;
	private SocketAddress userIP = null;
	private String userName = null;
	private Object userMsg = null;
	
	
	public EventBus getEventBus(){
		return eventBus;
	}
	public void setEventBus(EventBus eventBus){
		this.eventBus = eventBus;
	}

	public List<String> getConnectedUserList(){
		return connectedUserList;
	}
	public void setConnectedUserList(List<String> connectedUserList){
		this.connectedUserList = connectedUserList;
	}
	
	public SocketAddress getUserIP(){
		return userIP;
	}
	public void setUserIP(SocketAddress userIP){
		this.userIP = userIP;
	}
	
	public String getUserName(){
		return userName;
	}
	public void setUserName(String userName){
		this.userName = userName;
	}
	
	public Object getUserMsg(){
		return userMsg;
	}
	public void setUserMsg(Object userMsg){
		this.userMsg = userMsg;
	}
}
