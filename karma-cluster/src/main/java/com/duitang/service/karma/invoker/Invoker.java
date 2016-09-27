package com.duitang.service.karma.invoker;

import com.duitang.service.karma.KarmaException;

public interface Invoker {

  public Object invoke(String name, Object[] parameters) throws KarmaException;

  public Class[] lookupParameterTypes(String name) throws KarmaException;

  public Class[][] lookupParameterizedType(String name) throws KarmaException;

}
