package com.duitang.service.karma.support;

import java.util.ArrayList;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Test;

public class ProxiedWRRInvokerTest {
    @Test
    public void testProxiedMethod() {
        P p = new P();
        p.setValue("ttttt");
        P testInvoker = ProxiedWRRInvoker.newInstance("test", P.class, Lists.newArrayList(p));
        Assert.assertEquals(testInvoker.invokeSomeMethod(), "tttttxxx");
    }

    @Test
    public void testMultipleInstance() {
        int _i = 20;
        final Set<String> resultSet = Sets.newHashSet();
        ArrayList<P> impls = Lists.newArrayList();
        for (int i = 0; i < _i; i++) {
            P p = new P();
            p.setValue(String.valueOf(i));
            impls.add(p);
            resultSet.add(i + "xxx");
        }
        P proxied = ProxiedWRRInvoker.newInstance("test2", P.class, impls);

        for (int i = 0; i < 500; i++) {
            Assert.assertThat(proxied.invokeSomeMethod(), new BaseMatcher<String>() {
                @Override
                public boolean matches(Object o) {
                    return resultSet.contains(o);
                }

                @Override
                public void describeTo(Description description) {

                }
            });
        }
    }


    public static class P {
        private String value;

        public void setValue(String value) {
            this.value = value;
        }

        public String invokeSomeMethod() {
            return value + "xxx";
        }
    }
}