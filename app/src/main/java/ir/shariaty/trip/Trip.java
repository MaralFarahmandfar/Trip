package ir.shariaty.trip;

import java.util.Date;

public class Trip {
    private String id;
    private String name;
    private String uid;
    private Date startDate;
    private Date endDate;

    public Trip() {}

    public Trip(String id, String name, String uid, Date startDate, Date endDate) {
        this.id = id;
        this.name = name;
        this.uid = uid;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {this.id = id;}

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
}