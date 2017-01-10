package com.bakerbeach.market.shop.service;

import java.util.Set;

import com.bakerbeach.market.core.api.model.ShopContext;

public class CheckoutStatusResolver {
	
	public static final int STEP_INIT = 0;
	public static final int STEP_ADDRESS = 1;
	public static final int STEP_PAYMENT = 2;
	public static final int STEP_SUMMARY = 3;
	public static final int STEP_ORDER = 4;
	public static final int STEP_CONFIRM = 5;
	
	private static final String[] PAGEIDS = { "checkout", "checkout-address", "checkout-payment", "checkout-summary",
			"checkout-order", "checkout-confirm" };

	public Integer nextStepID(ShopContext shopContext) {
		Integer nextStep = STEP_INIT;

		Set<Integer> validSteps = shopContext.getValidSteps();

		if (validSteps.contains(STEP_ADDRESS))
			if (validSteps.contains(STEP_PAYMENT))
				if (validSteps.contains(STEP_SUMMARY))
					if (validSteps.contains(STEP_ORDER))
						nextStep = STEP_CONFIRM;
					else
						nextStep = STEP_ORDER;
				else
					nextStep = STEP_SUMMARY;
			else
				nextStep = STEP_PAYMENT;
		else {
			nextStep = STEP_ADDRESS;
			validSteps.clear();
		}

		return nextStep;
	}
	
	public String nextStepPageId(ShopContext shopContext) {
		return PAGEIDS[nextStepID(shopContext)];
	}
	
	public Boolean isStepvalid(ShopContext shopContext, Integer step){
		if(nextStepID(shopContext) == step || shopContext.getValidSteps().contains(step))
			return true;
		else
			return false;
	}
	
}