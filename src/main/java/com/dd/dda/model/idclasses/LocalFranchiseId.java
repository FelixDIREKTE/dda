package com.dd.dda.model.idclasses;

import java.io.Serializable;

public class LocalFranchiseId implements Serializable {

    private Long parliament_id;
    private String zipcode;

    public LocalFranchiseId(){

    }
    public LocalFranchiseId(Long parliament_id, String zipcode){
        this.parliament_id = parliament_id;
        this.zipcode = zipcode;
    }

}
