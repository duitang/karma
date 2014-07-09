package com.duitang.service.base;


public interface ServiceFactory<T> {

	T create();

	void release(T srv);

	String getServiceName();

	Class getServiceType();

}
