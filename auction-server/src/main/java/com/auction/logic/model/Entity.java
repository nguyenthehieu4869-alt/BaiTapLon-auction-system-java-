package com.auction.logic.model;

import java.io.Serializable;
import java.util.UUID;

public abstract class Entity implements Serializable {
    protected String id = UUID.randomUUID().toString();
    public String getId() { return id; }
}