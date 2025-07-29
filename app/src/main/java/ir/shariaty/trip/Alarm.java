package ir.shariaty.trip;

import java.util.Date;

public class Alarm {
    private String id;
    private String name;
    private String note;
    private Date dateTime;
    private String tripId;

    public Alarm() {
    }

    public Alarm(String id, String name, String note, Date dateTime, String tripId) {
        this.id = id;
        this.name = name;
        this.note = note;
        this.dateTime = dateTime;
        this.tripId = tripId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNote() {
        return note;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public String getTripId() {
        return tripId;
    }
}