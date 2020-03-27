package org.jionchu.calendarexample;

public class Schedule implements Comparable<Schedule> {

    private String title;
    private String time;

    public Schedule(String title, String time) {
        this.title = title;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    @Override
    public int compareTo(Schedule o) {
        return this.time.compareTo(o.getTime());
    }
}
