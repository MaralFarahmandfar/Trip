package ir.shariaty.trip;

import java.util.Date;

public class Attraction {
    private String id;
    private String name;
    private Date createdAt;
    private boolean isChecked;

    public Attraction() {}

    public Attraction(String id, String name, Date createdAt, boolean isChecked) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.isChecked = isChecked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}