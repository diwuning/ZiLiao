package com.nmpa.nmpaapp.modules.office.bean;

import java.util.List;

public class TypeTreeBean {
    private String id;
    private String name;
    private String label;
    private List<TypeTreeBean> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<TypeTreeBean> getChildren() {
        return children;
    }

    public void setChildren(List<TypeTreeBean> children) {
        this.children = children;
    }
}
