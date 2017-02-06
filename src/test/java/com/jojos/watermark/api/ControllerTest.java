package com.jojos.watermark.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jojos.watermark.domain.Author;
import com.jojos.watermark.domain.Book;
import com.jojos.watermark.domain.Content;
import com.jojos.watermark.domain.Document;
import com.jojos.watermark.domain.Journal;
import com.jojos.watermark.domain.Topic;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Basic integration test for the api exposed through the controller.
 *
 * Include some concurrency tests as well.
 *
 * @author karanikasg@gmail.com.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.LOG_DEBUG)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final AtomicInteger ticketIds = new AtomicInteger();

    // control the number of simultaneous calls
    private static final int concurrentRuns = 5_000;

    @Test
    public void test1_PostCreateWatermarkForBook() throws Exception {
        Document book = new Book("Dummy Book", new Author("Dick", "Whittington"), Topic.Business);
        MvcResult result = mockMvc.perform(post("/watermark/create").
                contentType(MediaType.APPLICATION_JSON_UTF8).
                content(convertObjectToJsonBytes(book))).
                andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        assertCorrectTicketReturned(content);
    }

    @Test
    public void test2_PostCreateWatermarkForJournal() throws Exception {
        Document document = new Journal("Magnetospheric Multiscale", new Author("Roy", "Torbert"));
        MvcResult result = mockMvc.perform(post("/watermark/create").
                contentType(MediaType.APPLICATION_JSON_UTF8).
                content(convertObjectToJsonBytes(document))).
                andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        assertCorrectTicketReturned(content);
    }

    @Test
    public void test3_GetCreateWatermarkNoParams() throws Exception {
        mockMvc.perform(get("/watermark/create")).
                andExpect(status().isBadRequest());
    }

    @Test
    public void test4_GetCreateWatermarkLessParams() throws Exception {
        mockMvc.perform(get("/watermark/create").param("title", "Earth")).
                andExpect(status().isBadRequest());
    }

    @Test
    public void test5_GetCreateWatermarkForJournal() throws Exception {
        MvcResult result = mockMvc.perform(get("/watermark/create").
                param("title", "Digital Media").
                param("authorFirstName", "Elias").
                param("authorLastName", "Rimon")).
                andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        assertCorrectTicketReturned(content);
    }

    @Test
    public void test6_GetCreateWatermarkForBook() throws Exception {
        MvcResult result = mockMvc.perform(get("/watermark/create").
                param("title", "Earth").
                param("authorFirstName", "Sougamoto").
                param("authorLastName", "Soi").param("topic", Topic.Science.name())).
                andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        assertCorrectTicketReturned(content);
    }

    @Test
    public void test7_GetCreateWatermarkForBookWrongTopic() throws Exception {
        mockMvc.perform(get("/watermark/create").
                param("title", "Earth").
                param("authorFirstName", "Oti").
                param("authorLastName", "Katsei").param("topic", "whatever")).
                andExpect(status().isBadRequest());
    }

    @Test
    public void test8_GetWaterMarkForTicketNoParams() throws Exception {
        mockMvc.perform(get("/watermark/get")).
                andExpect(status().isBadRequest());
    }

    @Test
    public void test9_GetWaterMarkForTicketsConcurrent() throws Exception {
        //        assureArtificialDelayPassed();
        mockMvc.perform(get("/watermark/get").param("ticket", "1")).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).
                andExpect(jsonPath("$.content").value(Content.Book.name())).
                andExpect(jsonPath("$.title").value("Dummy Book")).
                andExpect(jsonPath("$.author").value("Dick Whittington")).
                andExpect(jsonPath("$.topic").value(Topic.Business.name()));

        mockMvc.perform(get("/watermark/get").param("ticket", "2")).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).
                andExpect(jsonPath("$.content").value(Content.Journal.name())).
                andExpect(jsonPath("$.title").value("Magnetospheric Multiscale")).
                andExpect(jsonPath("$.author").value("Roy Torbert"));

        mockMvc.perform(get("/watermark/get").param("ticket", "3")).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).
                andExpect(jsonPath("$.content").value(Content.Journal.name())).
                andExpect(jsonPath("$.title").value("Digital Media")).
                andExpect(jsonPath("$.author").value("Elias Rimon"));

        mockMvc.perform(get("/watermark/get").param("ticket", "4")).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).
                andExpect(jsonPath("$.content").value(Content.Book.name())).
                andExpect(jsonPath("$.title").value("Earth")).
                andExpect(jsonPath("$.author").value("Sougamoto Soi")).
                andExpect(jsonPath("$.topic").value(Topic.Science.name()));

        // concurrency test - perform all 5000 requests at the same time. they are all waiting a single {@link CountDownLatch} to perform the request
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(1);

        int runStart = 5;
        IntStream.range(runStart, concurrentRuns).forEachOrdered(run ->
                executorService.submit(() -> {
                    try {
                        latch.await(10, TimeUnit.SECONDS);
                        MvcResult result = mockMvc.perform(
                                get("/watermark/create").
                                        param("title", "Earth" + run).
                                        param("authorFirstName", "Sougamoto").
                                        param("authorLastName", "Soi")).
                                andExpect(status().isOk()).andReturn();
                        String content = result.getResponse().getContentAsString();
                        assertCorrectTicketReturned(content);

                    } catch (Exception e) {
                        Assert.fail("Concurrency test execution interrupted. Exiting.");
                    }
                }));
        executorService.shutdown();
        latch.countDown();

        // since all previous requests were done "in parallel" the order of execution is not fixed, therefor there is no way
        // to guarantee the order of watermark generation. Use a set of objects for which only the range of possible values
        // is known and not their relative order and operate on it.
        Set<String> allEarths = new HashSet<>();
        for (int i = runStart; i < concurrentRuns; i++) {
            allEarths.add("Earth" + i);
        }

        for (int run = runStart; run < concurrentRuns; run++) {
            int retries = 3;
            // give it a try and don't be so strict. this is probably going to happen the first couple of times after which
            // the previously created threads are expected to complete
            while (!getWatermarkForTicket(allEarths, run) && retries-- > 0) {
                Thread.sleep(3000);
            }
        }

        // make sure we have received every single "earth" journal
        Assert.assertTrue(allEarths.isEmpty());

    }

    private boolean getWatermarkForTicket(Set<String> allEarths, int i) throws Exception {
        ResultActions resultAction = mockMvc.perform(get("/watermark/get").param("ticket", Integer.toString(i)));
        if (resultAction.andReturn().getResponse().getContentAsString().isEmpty()) {
            return false;
        }

        // if the response is not empty continue normally
        MvcResult result = resultAction.andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).
                andExpect(jsonPath("$.content").value(Content.Journal.name())).
                andExpect(jsonPath("$.author").value("Sougamoto Soi")).
                andReturn();
        String body = result.getResponse().getContentAsString();
        // this is ugly I know but it's the only way I came up with in order to parse the data of the body. The actual content looks like this:
        // {"content":"Journal","title":"Earth6","author":"Sougamoto Soi"}
        int start = body.indexOf("Earth");
        int end = body.indexOf(",", start) - 1;
        String earthStr = body.substring(start, end);
        Assert.assertTrue(allEarths.remove(earthStr));
        return true;
    }


    // we MUST call this one every time we expect a successful result on a "create watermark" request
    // since it is the only way to count the tickets
    private void assertCorrectTicketReturned(String content) {
        int returnedTicket = Integer.parseInt(content);
        Assert.assertEquals(ticketIds.incrementAndGet(), returnedTicket);
    }

    private static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }

}
