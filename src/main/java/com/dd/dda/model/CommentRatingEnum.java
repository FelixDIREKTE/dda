package com.dd.dda.model;

import com.dd.dda.model.exception.DDAException;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public enum CommentRatingEnum implements Serializable {

    @JsonProperty("GOODPOINT")
    GOODPOINT(0),

    @JsonProperty("GOODSOURCE")
    GOODSOURCE(1),

    @JsonProperty("GOODPROPOSAL")
    GOODPROPOSAL(2);

    private final int value;

    private static final Map<Integer, CommentRatingEnum> lookup = new HashMap<>();

    static {
        for (CommentRatingEnum st : CommentRatingEnum.values()) {
            lookup.put(st.getValue(), st);
        }
    }

    CommentRatingEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public CommentRatingEnum parse(int value) {
        if (!lookup.containsKey(value)) {
            throw new DDAException("Impossible to parse SourceType with value " + value);
        }

        return lookup.get(value);
    }
}
