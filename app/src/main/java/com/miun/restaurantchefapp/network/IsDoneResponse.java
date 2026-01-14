package com.miun.restaurantchefapp.network;

import com.google.gson.annotations.SerializedName;

/**
 * Response model för isDone-anropet
 */
public class IsDoneResponse {

    @SerializedName("isDone")
    private boolean isDone;

    public IsDoneResponse() {
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }
}