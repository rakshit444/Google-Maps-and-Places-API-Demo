package com.api.uniconn.Models;

import com.api.uniconn.Rest.Models.NearbySearch;
import com.api.uniconn.Rest.Models.Result;

import java.util.List;

/**
 * Created by Rakshit on 7/16/2016.
 */
public class MessageEvent {

    public List<Result> result;
    public int position;

    public MessageEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }


    public MessageEvent(List<Result> result) {
        this.result = result;
    }

    public List<Result> getResult() {
        return result;
    }
}