package com.jojos.watermark.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jojos.watermark.domain.Author;
import com.jojos.watermark.domain.Book;
import com.jojos.watermark.domain.Document;
import com.jojos.watermark.domain.Journal;
import com.jojos.watermark.domain.Topic;

/**
 * A "view" of the document that is only used for serializing incoming requests to documents
 *
 * @author gkaranikas
 */
public class DocumentView {

    private final Document document;

    public DocumentView(String title, Author author) {
        document = new Journal(title, author);
    }

    @JsonCreator
    public DocumentView(@JsonProperty("title") String title,
            @JsonProperty("author") Author author,
            @JsonProperty("topic") Topic topic) {
        if (topic == null) {
            document = new Journal(title, author);
        } else {
            document = new Book(title, author, topic);
        }

    }

    public Document toDocument() {
        return document;
    }

}
