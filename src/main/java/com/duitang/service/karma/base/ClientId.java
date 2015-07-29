package com.duitang.service.karma.base;

public class ClientId {
    private final String name;
    private final boolean isClient;

    /**
     *
     * @param name      名字, 一般是 interface 全名
     * @param isClient  是否client
     */
    public ClientId(String name, boolean isClient) {
        if(name == null) {
            throw new IllegalArgumentException("name==null");
        }
        this.name = name;
        this.isClient = isClient;
    }

    public String getName() {
        return name;
    }

    public boolean isClient() {
        return isClient;
    }
}
