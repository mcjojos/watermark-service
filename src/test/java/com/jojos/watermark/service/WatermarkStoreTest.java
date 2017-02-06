package com.jojos.watermark.service;

import com.jojos.watermark.domain.Document;
import com.jojos.watermark.domain.Watermark;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.when;
/**
 * Unit test of the {@link WatermarkStore}
 *
 * @author gkaranikas
 */
public class WatermarkStoreTest {
    @InjectMocks
    private WatermarkStore store;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Mock
    private Document document;
    @Mock
    private Document watermarkedDocument;
    @Mock
    private Watermark watermark;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(watermarkedDocument.getWatermark()).thenReturn(Optional.of(watermark));
        when(document.getWatermark()).thenReturn(Optional.empty());
    }

    @Test
    public void testStoreDocuments() {
        for (int run = 1; run < 10; run++) {
            Assert.assertTrue(run == store.storeDocumentAndCreateTicket(document));
        }
    }

    @Test
    public void testStoreDocumentForTicketAssociatedWithNonExistingDocument() {
        Assert.assertFalse(store.storeDocumentForTicket(1, document));
    }

    @Test
    public void testStoreDocumentForTicketAssociatedWithWatermarkedDocument() {
        for (int run = 0; run < 10; run++) {
            int ticket = store.storeDocumentAndCreateTicket(watermarkedDocument);
            Assert.assertFalse(store.storeDocumentForTicket(ticket, watermarkedDocument));
        }
    }

    @Test
    public void testStoreDocumentForTicketNonWatermarkedDocument() {
        for (int run = 0; run < 10; run++) {
            int ticket = store.storeDocumentAndCreateTicket(document);
            Assert.assertFalse(store.storeDocumentForTicket(ticket, document));
        }
    }

    @Test
    public void testStoreDocumentForTicketSuccess() {
        for (int run = 0; run < 10; run++) {
            int ticket = store.storeDocumentAndCreateTicket(document);
            Assert.assertTrue(store.storeDocumentForTicket(ticket, watermarkedDocument));
        }
    }

    @Test
    public void testGetWatermarkForTicketNull() {
        Assert.assertNull(store.getWatermarkForTicket(10));
    }

    @Test
    public void testGetWatermarkForTicketDocumentWithEmptyWatermark() {
        int ticket = store.storeDocumentAndCreateTicket(document);
        Assert.assertFalse(store.storeDocumentForTicket(ticket, document));
        Assert.assertNull(store.getWatermarkForTicket(ticket));
    }

    @Test
    public void testGetWatermarkForTicket() {
        int ticket = store.storeDocumentAndCreateTicket(document);
        Assert.assertTrue(store.storeDocumentForTicket(ticket, watermarkedDocument));
        Assert.assertEquals(watermark, store.getWatermarkForTicket(ticket));
    }

}
