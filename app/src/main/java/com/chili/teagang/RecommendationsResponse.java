package com.chili.teagang;


import java.util.List;

public class RecommendationsResponse {
    private List<Recommendation> data;

    public List<Recommendation> getData() {
        return data;
    }

    public void setData(List<Recommendation> data) {
        this.data = data;
    }
}