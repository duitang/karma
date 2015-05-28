package com.duitang.service.karma.base;

public class ClientId {
    private final String name;
    private final boolean isClient;
    private final String implName;
    private final String location; // location of the process (host/rack/idc/pid)

    /**
     *
     * @param name      名字, 一般是 interface 全名
     * @param implName  实现, server为实现名字, client为null
     * @param location  地点, 可选, 提供 host/机柜/idc/进程 信息
     */
    public ClientId(String name, String implName, String location) {
        if(name == null) {
            throw new IllegalArgumentException("name==null");
        }
        this.name = name;
        this.location = location;
        if (implName == null || implName.isEmpty()) {
            this.implName = null;
            this.isClient = true;
        } else {
            this.implName = implName;
            this.isClient = false;
        }
    }

    public String getName() {
        return name;
    }

    public boolean isClient() {
        return isClient;
    }

    public String getImplName() {
        return implName;
    }

    public String getLocation() {
        return location;
    }
}
