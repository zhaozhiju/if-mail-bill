package net.umpay.mailbill.entrance.http;

public interface TransportReceiver {
    public String getName();

    public void start() throws Exception;

    public void stop() throws Exception;
}
