package com.duitang.service.karma.base;


public interface ServiceFactory<T> {

  T create();

  void release(T srv);

  String getServiceName();

  Class getServiceType();

}
