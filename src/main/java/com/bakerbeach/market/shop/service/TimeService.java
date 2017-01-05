package com.bakerbeach.market.shop.service;

import java.io.Serializable;
import java.util.Date;

public class TimeService implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	Date flux = null;
	
	public Date getTime(){
		if(flux != null)
			return flux;
		return new Date();
	}
	
	public void setTime(Date date){
		flux = date;
	}

}
