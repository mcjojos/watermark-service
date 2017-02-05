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
 * A place to store the watermark
 * @author gkaranikas
 */
@Service
@Scope(value="singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WatermarkStore {

    private static final Logger log = LoggerFactory.getLogger(WatermarkStore.class);

    private final ConcurrentMap<Integer, Document> ticketsToDocuments = new ConcurrentHashMap<>();
    private final AtomicInteger ticketIds = new AtomicInteger();

    public Integer storeDocumentAndCreateTicket(Document document) {
        ticketIds.incrementAndGet();
        ticketsToDocuments.putIfAbsent(ticketIds.get(), document);

        log.info("Creating ticket {} for document {}.", ticketIds.get(), document);
        return ticketIds.get();
    }

    public boolean storeDocumentForTicket(Integer ticket, Document document) {
        Document previousDocument = ticketsToDocuments.put(ticket, document);

        return validate(previousDocument, document);
    }

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
