package com.duitang.service.karma.invoker;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.duitang.service.karma.KarmaException;
import com.duitang.service.karma.base.MetricCenter;

public class ReflectInvoker implements Invoker {

    protected String clientId;
    protected Class iface;
    protected Object impl;

    // direct name lookup
    protected Map<String, Method> proxy1;

    // parameter types
    protected Map<String, Class[]> types;

    /**
     * <pre>
     * generic parameterized types caution:
     *
     * void aaa(Map<String, Float>, Map<Double, Boolean>)
     *
     * 	will =>
     *
     *    Class[][] = {
     *    				[String, Float],
     *                  [Double, Boolean]
     * }
     *
     * <pre>
     */
    protected Map<String, Class[][]> paramTypes;

    public ReflectInvoker(String clientId, Class iface, Object impl) throws KarmaException {
        this.clientId = clientId;
        this.iface = iface;
        this.impl = impl;
        init();
    }

    private void init() {
        proxy1 = new HashMap<>();
        types = new HashMap<>();
        paramTypes = new HashMap<>();
        Method[] iface_methods = iface.getMethods();
        Type[] the_types = null;
        Class[][] ptypes;
        for (Method m : iface_methods) {
            String name = m.getName();
            m.setAccessible(true);
            // FIXME: check no name duplication here
            proxy1.put(name, m);
            Class[] oldtt = m.getParameterTypes();
            Class[] newtt = new Class[oldtt.length];
            for (int i = 0; i < oldtt.length; i++) {
                if (oldtt[i].isPrimitive()) {
                    newtt[i] = getBigType(oldtt[i]);
                    continue;
                }
                newtt[i] = oldtt[i];
            }
            types.put(name, newtt);
            the_types = m.getGenericParameterTypes();
            ptypes = new Class[the_types.length][];
            for (int ii = 0; ii < ptypes.length; ii++) {
                if (the_types[ii] instanceof ParameterizedType) {
                    ptypes[ii] = new Class[2];
                    Type[] ata = ((ParameterizedType) the_types[ii]).getActualTypeArguments();
                    try {
                        String nm1 = ata[0].toString();
                        ptypes[ii][0] = Class.forName(nm1.split(" ")[1]);
                        if (ata.length > 1) {
                            String nm2 = ata[1].toString();
                            ptypes[ii][1] = Class.forName(nm2.split(" ")[1]);

                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            paramTypes.put(name, ptypes);
        }
//        MetricCenter.initMetric(this.iface, this.clientId);
    }

    @Override
    public Object invoke(String name, Object[] parameters) throws KarmaException {
        Method m = getMethod(name);
        return invokeMethod(m, parameters);
    }

    @Override
    public Class[] lookupParameterTypes(String name) throws KarmaException {
        if (!types.containsKey(name)) {
            throw new KarmaException("Not found method: " + name);
        }
        return types.get(name);
    }

    @Override
    public Class[][] lookupParameterizedType(String name) throws KarmaException {
        if (!paramTypes.containsKey(name)) {
            throw new KarmaException("Not found method: " + name);
        }
        return paramTypes.get(name);
    }

    protected Method getMethod(String name) throws KarmaException {
        Method ret = proxy1.get(name);
        if (ret == null) {
            throw new KarmaException("Not found method[" + name + "]");
        }
        return ret;
    }

    protected Object invokeMethod(Method m, Object[] parameters) throws KarmaException {
        Object ret;
        long startNanos = System.nanoTime();
        boolean fail = false;
        try {
            ret = m.invoke(impl, parameters);
            fail = false;
        } catch (Throwable e) {
            StringBuilder sb = new StringBuilder();
            sb.append(m.getClass().getName()).append(".").append(m.getName()).append("(");
            for (Object parameter : parameters) {
                if (parameter == null) {
                    sb.append("null");
                } else {
                    sb.append(parameter.getClass().getName());
                }
                sb.append(",");
            }
            sb.replace(sb.length() - 1, sb.length(), ")");
            fail = true;
            throw new KarmaException(sb.toString(), e);
        } finally {
            long endNanos = System.nanoTime() - startNanos;
            MetricCenter.methodMetric(clientId, m.getName(), TimeUnit.NANOSECONDS.toMillis(endNanos), fail);
        }
        return ret;
    }

    protected Class getBigType(Class t) {
        if (t == int.class) {
            return Integer.class;
        }
        if (t == float.class) {
            return Float.class;
        }
        if (t == double.class) {
            return Double.class;
        }
        if (t == long.class) {
            return Long.class;
        }
        if (t == short.class) {
            return Short.class;
        }
        if (t == byte.class) {
            return Byte.class;
        }
        if (t == boolean.class) {
            return Boolean.class;
        }
        return t;
    }

}
