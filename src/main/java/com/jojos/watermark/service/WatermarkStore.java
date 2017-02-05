package com.jojos.watermark.service;

import com.jojos.watermark.domain.Document;
import com.jojos.watermark.domain.Watermark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A place to store the watermarks
 *
 * This is translated to simple in-memory storage and stays alive for the life time of the application.
 * If the jvm stops the store dies with it.
 *
 * The ticket ids start from 1 and are incremented by one after each and every request.
 *
 * Thread safety and atomicity of storing documents and generating unique ticket ids is guaranteed across multiple requests.
 *
 * @author gkaranikas
 */
@Service
@Scope(value="singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WatermarkStore {

    private static final Logger log = LoggerFactory.getLogger(WatermarkStore.class);

    private final ConcurrentMap<Integer, Document> ticketsToDocuments = new ConcurrentHashMap<>();
    private final AtomicInteger ticketIds = new AtomicInteger();

    /**
     * Store the {@link Document} in memory and correlates a ticket with it
     * Usually this operation shall be called for a document that's not already watermarked.
     * @param document the document to store
     * @return the ticket id associated with the passed document
     */
    public Integer storeDocumentAndCreateTicket(Document document) {
        ticketIds.incrementAndGet();
        ticketsToDocuments.putIfAbsent(ticketIds.get(), document);

        log.info("Creating ticket {} for document {}.", ticketIds.get(), document);
        return ticketIds.get();
    }

    /**
     * Store a {@link Document} AFTER it has been watermarked. Some basic validation is
     * done by means of checking the watermark property of the previous and the passed document.
     *
     * @param ticket the ticket id for the document that has just been watermarked
     * @param document the new watermarked document - book or journal.
     * @return true if the document is found to be in a valid state and validation succeeds.
     */
    public boolean storeDocumentForTicket(Integer ticket, Document document) {
        Document previousDocument = ticketsToDocuments.put(ticket, document);

        return validate(previousDocument, document);
    }

    /**
     * Get the watermark property (if any) that has been stored for the particular ticket
     *
     * @param ticket the id for which a particular (watermarked) document has been correlated
     * @return the watermark for that ticket or null if it doesn't exist or it hasn't yet been created.
     */
    public Watermark getWatermarkForTicket(Integer ticket) {
        if (isWatermarkCreatedForTicket(ticket)) {
            return ticketsToDocuments.get(ticket).getWatermark().get();
        }
        return null;
    }

    private boolean isWatermarkCreatedForTicket(Integer ticket) {
        if (ticketsToDocuments.get(ticket) != null && ticketsToDocuments.get(ticket).getWatermark().isPresent()) {
            return true;
        }
        return false;
    }
    
    private boolean validate(Document previousDocument, Document document) {
        if (previousDocument == null) {
            log.warn("Attempting to store the ticket associated with a non-existing document");
            return false;
        }
        if (previousDocument.getWatermark().isPresent()) {
            log.warn("Attempting to store the ticket associated with a document that's previously already watermarked");
            return false;
        }
        if (!document.getWatermark().isPresent()) {
            log.warn("Attempting to store the ticket associated with a document not watermarked");
            return false;
        }
        return true;
    }

}
