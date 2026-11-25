package org.example.marlo_site_helpdesk.dto;

public class TicketStatsDto {

    private final long total;
    private final long newTickets;
    private final long inProgress;
    private final long done;
    private final long closed;
    private final long high;
    private final long critical;

    public TicketStatsDto(
            long total,
            long newTickets,
            long inProgress,
            long done,
            long closed,
            long high,
            long critical) {

        this.total = total;
        this.newTickets = newTickets;
        this.inProgress = inProgress;
        this.done = done;
        this.closed = closed;
        this.high = high;
        this.critical = critical;
    }

    public long getTotal() {
        return total;
    }

    public long getNewTickets() {
        return newTickets;
    }

    public long getInProgress() {
        return inProgress;
    }

    public long getDone() {
        return done;
    }

    public long getClosed() {
        return closed;
    }

    public long getHigh() {
        return high;
    }

    public long getCritical() {
        return critical;
    }
}