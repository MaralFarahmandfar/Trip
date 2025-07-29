package ir.shariaty.trip;

import java.util.Date;

public class Attraction {
    private String id;
    private String name;
    private Date createdAt;

    public Attraction() {
    }

    public Attraction(String id, String name, Date createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}