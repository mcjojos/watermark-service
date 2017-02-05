package com.jojos.watermark.api;

import com.jojos.watermark.data.DocumentView;
import com.jojos.watermark.domain.Author;
import com.jojos.watermark.domain.Book;
import com.jojos.watermark.domain.Document;
import com.jojos.watermark.domain.Journal;
import com.jojos.watermark.domain.Topic;
import com.jojos.watermark.domain.Watermark;
import com.jojos.watermark.service.WatermarkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * The main api that will:
 * 1. make an asynchronous request to watermark a particular document. You can use a post or a get request, they should behave the same
 * 2. try to get the actual watermark based on the ticket from the initial request
 *
 * @author gkaranikas
 */
@RestController
@RequestMapping("/watermark")
public class Controller {

    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    @Autowired
    private WatermarkService watermarkService;

    /**
     * This method can accept currently two types of documents depending on the json body of the request: books and journals
     *
     * the json object for the book type looks like
     * {
     *      "title" : "title1",
     *      "author" : {
     *          "firstName" : "first1",
     *          "lastName" : "last1"
     *      },
     *      "topic" : "Business"
     * }
     *
     * The equivalent for the journal type is identical with the exception that we ommit the topic
     * {
     *      "title" : "title1",
     *      "author" : {
     *          "firstName" : "first1",
     *          "lastName" : "last1"
     *      },
     *      "topic" : "Business"
     * }
     *
     * @param documentView a view of the document which is mapped to a book or a journal
     * @return the ticket number for which we associate this book for later watermark retrieval
     */
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public int createWatermarkForDocument(@RequestBody DocumentView documentView) {
        Document document = documentView.toDocument();
        log.info("Create a new watermark via post for document with title {}", document.getTitle());
        return watermarkService.createWatermarkFor(document);
    }

    /**
     * This method is effectively the same as the #createWatermarkForDocument
     *
     * @param title the title of the document
     * @param authorFirstName author's first name
     * @param authorLastName author's last name
     * @param topic, this is optional and depending on whether it's included or not in the request we map the request to a book or a journal
     * @return the ticket number for which we associate this book for later watermark retrieval
     */
    @GetMapping("/create")
    public int createWatermarkFor(@RequestParam String title,
            @RequestParam String authorFirstName,
            @RequestParam String authorLastName,
            @RequestParam(required = false) Topic topic) {
        log.info("Create a new watermark for {}", title);
        Document document;
        Author author = new Author(authorFirstName, authorLastName);
        if (topic == null) {
            document = new Journal(title, author);
        } else {
            document = new Book(title, author, topic);
        }
        return watermarkService.createWatermarkFor(document);
    }

    @GetMapping("/get")
    public Watermark getWatermarkForTicket(@RequestParam String ticket) {
        log.info("Retrieving watermark for ticket {}", ticket);
        return watermarkService.getWatermarkForTicket(Integer.valueOf(ticket));
    }


}
