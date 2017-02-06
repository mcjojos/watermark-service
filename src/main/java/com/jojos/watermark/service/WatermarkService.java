package com.jojos.watermark.service;

import com.jojos.watermark.domain.Document;
import com.jojos.watermark.domain.Watermark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The service responsible to create watermarks and associate particular ticket ids
 * that are used to fetch the watermarked document
 *
 * An enhancement can be made so that if the same document is requested to be watermarked for a second time then
 * the ticket id that is already produced and active shall be returned. For this to happen we must change the {@link WatermarkStore} as well
 *
 * Feel free to enhance the code in any case
 *
 * @author gkaranikas
 */
@Service
public class WatermarkService {

    private static final Logger log = LoggerFactory.getLogger(WatermarkService.class);

    private WatermarkStore store;

    @Autowired
    public WatermarkService(WatermarkStore store) {
        this.store = store;
    }

    /**
     * Create a watermark for a document. Since this is an asynchronous operation the ticket associated to the document is returned
     * so that when the watermarking is finished the document can be retrieved with this ticket
     *
     * @param document the document to watermark
     * @return the ticket that is used to fetch the watermark once it's done
     */
    public Integer createWatermarkFor(Document document) {

        Integer ticket = store.storeDocumentAndCreateTicket(document);

        // simulate some time-consuming watermark task
        addWatermark(ticket, document);

        return ticket;
    }

    /**
     * Retrieves the watermark for a particular ticket id
     * @param ticket the id for which a particular watermark is produced
     * @return the watermark or null if it hasn't been created one (yet)
     */
    public Watermark getWatermarkForTicket(Integer ticket) {
        return store.getWatermarkForTicket(ticket);
    }


    /**
     * Adds a watermark to the specific document in an asynchronous manner
     *
     * @param ticket the ticket id associated to an existing document
     * @param document the document we need to add the watermark to
     */
    private void addWatermark(Integer ticket, Document document) {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService.execute(() -> {
            long startTime = System.currentTimeMillis();
            long threadId = Thread.currentThread().getId();
            // TODO make the following code configurable through some application properties
            // simulate the job to last from 1 to 10 seconds
//            int durationInSeconds = ThreadLocalRandom.current().nextInt(1, 11);
//            try {
//                log.info("ThreadId {} -- start watermarking document {}", threadId, document);
//                Thread.sleep(durationInSeconds * 1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            Watermark watermark = Watermark.createFor(document);
            Document watermarkedDoc = document.withWatermark(watermark);
            if (!store.storeDocumentForTicket(ticket, watermarkedDoc)) {
                log.error("ThreadId {} -- An error occurred while storing the watermarked document {}", threadId, document);
            } else {
                long endTime = System.currentTimeMillis();
                long seconds = (endTime - startTime) / 1000;
                log.info("ThreadId {} -- Took {} seconds to watermark {}", threadId, seconds, document);
            }

        });

    }

}
