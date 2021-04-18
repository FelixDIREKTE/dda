package com.dd.dda.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.dd.dda.model.exception.DDAException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum VerificationStatus implements Serializable {

    @JsonProperty("DATANEEDED")
    DATANEEDED(0),

    @JsonProperty("WAITINGFORVERIF")
    WAITINGFORADMIN(1),

    @JsonProperty("LOCKEDBYADMIN")
    LOCKEDBYADMIN(2),

    @JsonProperty("VERIFIED")
    VERIFIED(3),

    @JsonProperty("REVERIFYEMAIL")
    REVERIFYEMAIL(4);

    private final int value;

    private static final Map<Integer, VerificationStatus> lookup = new HashMap<>();

    static {
        for (VerificationStatus st : VerificationStatus.values()) {
            lookup.put(st.getValue(), st);
        }
    }

    VerificationStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public VerificationStatus parse(int value) {
        if (!lookup.containsKey(value)) {
            throw new DDAException("Impossible to parse SourceType with value " + value);
        }

        return lookup.get(value);
    }
}
