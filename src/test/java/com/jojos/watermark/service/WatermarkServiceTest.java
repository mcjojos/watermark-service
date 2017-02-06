package com.jojos.watermark.service;

import com.jojos.watermark.domain.Document;
import com.jojos.watermark.domain.Watermark;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Basic testing of the {@link WatermarkService}
 *
 * @author gkaranikas
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Watermark.class})
public class WatermarkServiceTest {
    @InjectMocks
    private WatermarkService service;
    @Mock
    private WatermarkStore store;
    @Mock
    private Document document;
    @Mock
    private Watermark watermark;

    private static final AtomicInteger ticketIds = new AtomicInteger();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Watermark.class);
        when(store.storeDocumentAndCreateTicket(any())).thenReturn(ticketIds.incrementAndGet());
        when(store.getWatermarkForTicket(any())).thenReturn(watermark);
        when(store.storeDocumentForTicket(any(), any())).thenReturn(true);
        when(Watermark.createFor(document)).thenReturn(watermark);
    }

    @Test
    public void testStoreDocumentAndCreateTicket() {
        Assert.assertTrue(ticketIds.get() == service.createWatermarkFor(document));
    }

    @Test
    public void testStoreDocumentsAndCreateMultipleTicket() {
        for (int i = 0; i < 50; i++) {
            Assert.assertTrue(ticketIds.get() == service.createWatermarkFor(document));
        }
    }

    @Test
    public void testGetWatermarkForTicket() {
        when(store.getWatermarkForTicket(any())).thenReturn(watermark);
        Assert.assertEquals(watermark, service.getWatermarkForTicket(4));
    }

}
