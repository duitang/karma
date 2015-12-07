package com.duitang.service.karma.message;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * @author kevx
 * @since 1:17:10 PM Aug 19, 2015
 */
public class StdMessage {

    private String act;
    
    @JsonProperty("sbj_id")
    private String subjectId;
    
    @JsonProperty("sbj_t")
    private Integer subjectType;
    
    @JsonProperty("obj_id")
    private String objectId;
    
    @JsonProperty("obj_t")
    private Integer objectType;
    
    @JsonProperty("obj_owner")
    private String objectOwner;
    
    @JsonProperty("gmt_created")
    private Long gmtCreated;
    
    private Map<String, Object> data;
    
    @JsonProperty("_v")
    private int version = 0;

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(Integer subjectType) {
        this.subjectType = subjectType;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Integer getObjectType() {
        return objectType;
    }

    public void setObjectType(Integer objectType) {
        this.objectType = objectType;
    }

    public String getObjectOwner() {
        return objectOwner;
    }

    public void setObjectOwner(String objectOwner) {
        this.objectOwner = objectOwner;
    }

    public Long getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Long gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    
}
