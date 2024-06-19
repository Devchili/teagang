package com.chili.teagang;

import java.io.Serializable;
import java.util.Map;

public class MenuItem implements Serializable {
    private String id;
    private String category;
    private String name;
    private Map<String, Double> sizes;
    private boolean outOfStock;

    public MenuItem() {
        // Default constructor required for calls to DataSnapshot.getValue(MenuItem.class)
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Double> getSizes() {
        return sizes;
    }

    public void setSizes(Map<String, Double> sizes) {
        this.sizes = sizes;
    }

    public boolean isOutOfStock() {
        return outOfStock;
    }

    public void setOutOfStock(boolean outOfStock) {
        this.outOfStock = outOfStock;
    }
}
