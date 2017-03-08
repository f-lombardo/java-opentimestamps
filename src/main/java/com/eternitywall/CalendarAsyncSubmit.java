package com.eternitywall;

import com.eternitywall.http.Request;
import com.eternitywall.http.Response;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Created by luca on 08/03/2017.
 */
public class CalendarAsyncSubmit implements Callable<Timestamp> {

    private static Logger log = Logger.getLogger(CalendarAsyncSubmit.class.getName());

    private String url;
    private byte[] digest;
    private BlockingQueue<Timestamp> queue;

    public CalendarAsyncSubmit(String url, byte[] digest) {
        this.url = url;
        this.digest=digest;
    }

    public void setQueue(BlockingQueue<Timestamp> queue) {
        this.queue = queue;
    }

    @Override
    public Timestamp call() throws Exception {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept","application/vnd.opentimestamps.v1");
        headers.put("User-Agent","java-opentimestamps");
        headers.put("Content-Type","application/x-www-form-urlencoded");

        URL obj = new URL(url + "/digest");
        Request task = new Request(obj);
        task.setData(new String(digest));
        task.setHeaders(headers);
        Response response = task.call();
        byte[] body = response.getBytes();

        StreamDeserializationContext ctx = new StreamDeserializationContext(body);
        Timestamp timestamp = Timestamp.deserialize(ctx, digest);
        queue.add(timestamp);
        return timestamp;
    }
}