package com.example.ethiopianmusicapp.Objects;

import com.google.gson.annotations.SerializedName;

public class SearchPreview {
    @SerializedName("value")
    String item;

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }
}
