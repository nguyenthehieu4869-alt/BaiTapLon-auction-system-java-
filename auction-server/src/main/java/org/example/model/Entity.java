package org.example.model;

public abstract class Entity {
    private final int id;

    protected Entity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public abstract String printInfo();
}
