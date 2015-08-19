package com.duitang.service.karma.message;

import java.util.Objects;

/**
 * 
 * @author kevx
 * @since 2:35:26 PM Dec 16, 2014
 */
public enum EntityType {
	UNKNOWN(-1),
	BLOG(0),
	ALBUM(1),
	USER(2),
	COMMENT(3),
	CLUB(4),
	LETTER(5),
	TOPIC(6),
	TOPIC_COMMENT(7),
	TOPIC_REPLY(8),
	ORDER(9),//商业-订单
	CART(10),//商业-购物车
	INVENTORY(11),//商业-最小商品单元
	COUPON(12),//商业-优惠券
	PROMOTION(13);//商业-优惠活动

	public final int value;
	private EntityType(int v) {
		this.value = v;
	}
	public static int toValue(String name) {
		for (EntityType ot : EntityType.values()) {
			if (name.equalsIgnoreCase(ot.name())) 
				return ot.value;
		}
		return -1;
	}

	public static EntityType of(Integer i) {
		for (EntityType actionObjType : EntityType.values()) {
			if(Objects.equals(actionObjType.value, i))
				return actionObjType;
		}
		return UNKNOWN;
	}

	public static int toValue(Object name) {
		if (name != null) {
			//1. 直接传了Number
			if (name instanceof Number) {
				return (int)name;
			}
			//2. 传了numberal String
			try {
				return Integer.parseInt(name.toString());
			}catch (NumberFormatException e) {
			//3. 传了非numberal String
			for (EntityType ot : EntityType.values()) {
				if (name.toString().equalsIgnoreCase(ot.name()))
					return ot.value;
			}
			}
		}
		return -1;
	}
}
