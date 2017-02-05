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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Basic integration test for the api exposed through the controller.
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
    private static final AtomicBoolean delayPassed = new AtomicBoolean();

    @Test
    public void test1_PostCreateWatermarkForBook() throws Exception {
        Document book = new Book("Dummy Book", new Author("Dick", "Whittington"), Topic.Business);
        MvcResult result = mockMvc.perform(
                post("/watermark/create").
                                                 contentType(MediaType.APPLICATION_JSON_UTF8).
                                                 content(convertObjectToJsonBytes(book))).
                                          andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        assertCorrectTicketReturned(content);
    }

    @Test
    public void test2_PostCreateWatermarkForJournal() throws Exception {
        Document document = new Journal("Dummy Book", new Author("Dick", "Whittington"));
        MvcResult result = mockMvc.perform(
                post("/watermark/create").
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
        MvcResult result = mockMvc.perform(
                get("/watermark/create").
                                                param("title", "Earth").
                                                param("authorFirstName", "Ikaro").
                                                param("authorLastName", "EStinmpoutsm")).
                                          andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();
        assertCorrectTicketReturned(content);
    }

    @Test
    public void test6_GetCreateWatermarkForBook() throws Exception {
        MvcResult result = mockMvc.perform(
                get("/watermark/create").
                                                param("title", "Earth").
                                                param("authorFirstName", "Ikaro").
                                                param("authorLastName", "EStinmpoutsm").param("topic", Topic.Media.name())).

                                          andExpect(status().isOk()).andReturn();
        String content = result.getResponse().getContentAsString();

        assertCorrectTicketReturned(content);
    }

    @Test
    public void test7_GetCreateWatermarkForBookWrongTopic() throws Exception {
        mockMvc.perform(
                get("/watermark/create").
                                                param("title", "Earth").
                                                param("authorFirstName", "Ikaro").
                                                param("authorLastName", "EStinmpoutsm").param("topic", "whatever")).
                       andExpect(status().isBadRequest());
    }

    @Test
    public void test8_GetWaterMarkForTicketNoParams() throws Exception {
        mockMvc.perform(
                get("/watermark/get")).
                       andExpect(status().isBadRequest());

    }

    @Test
    public void test9_GetWaterMarkForTickets() throws Exception {
        assureArtificialDelayPassed();
        mockMvc.perform(
                get("/watermark/get").param("ticket", "1")).
                       andExpect(status().isOk()).
                       andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).
                       andExpect(jsonPath("$.content").value(Content.Book.name())).
                       andExpect(jsonPath("$.title").value("Dummy Book")).
                       andExpect(jsonPath("$.author").value("Dick Whittington")).
                       andExpect(jsonPath("$.topic").value(Topic.Business.name()));

        mockMvc.perform(
                get("/watermark/get").param("ticket", "2")).
                       andExpect(status().isOk()).
                // {"content":"Journal","title":"Dummy Book","author":"Dick Whittington"}
                        andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).
                        andExpect(jsonPath("$.content").value(Content.Journal.name())).
                        andExpect(jsonPath("$.title").value("Dummy Book")).
                        andExpect(jsonPath("$.author").value("Dick Whittington"));

        mockMvc.perform(
                get("/watermark/get").param("ticket", "3")).
                       andExpect(status().isOk()).
//                       andDo(print()).
                // {"content":"Journal","title":"Earth","author":"Ikaro EStinmpoutsm"}
                        andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).
                        andExpect(jsonPath("$.content").value(Content.Journal.name())).
                        andExpect(jsonPath("$.title").value("Earth")).
                        andExpect(jsonPath("$.author").value("Ikaro EStinmpoutsm"));

        mockMvc.perform(
                get("/watermark/get").param("ticket", "4")).
                       andExpect(status().isOk()).
                       andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).
                       andExpect(jsonPath("$.content").value(Content.Book.name())).
                       andExpect(jsonPath("$.title").value("Earth")).
                       andExpect(jsonPath("$.author").value("Ikaro EStinmpoutsm")).
                       andExpect(jsonPath("$.topic").value(Topic.Media.name()));

    }


    // we MUST call this one every time we expect a successful result on a "create watermark" request
    // since it is the only way to count the tickets
    private void assertCorrectTicketReturned(String content) {
        int returnedTicket = Integer.parseInt(content);
        Assert.assertEquals(ticketIds.incrementAndGet(), returnedTicket);
    }

    private void assureArtificialDelayPassed() {
        if (delayPassed.get()) {
            return;
        } else {
            try {
                // the artificial delay of the watermark service is 10 seconds. Add another one to be sure everything has finished
                Thread.sleep(11000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            delayPassed.set(true);
        }

    }

    private static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }

}
