package com.jojos.watermark.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;
import java.util.Optional;

/**
 * The watermark property
 *
 * For a book the watermark includes the properties content, title, author and topic.
 * The journal watermark includes the content, title and author.
 *
 * @author gkaranikas
 */

public class Watermark {

    private final Content content;
    private final String title;
    private final Author author;
    private final Optional<Topic> topic;

    private Watermark(Book book) {
        this.content = Content.forClass(book.getClass());
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.topic = Optional.of(book.getTopic());
    }

    private Watermark(Journal journal) {
        this.content = Content.forClass(Journal.class);
        this.title = journal.getTitle();
        this.author = journal.getAuthor();
        this.topic = Optional.empty();
    }

    public static Watermark createFor(Document document) {
        if (Objects.isNull(document)) {
            return null;
        } else if (document instanceof Book) {
            return new Watermark((Book) document);
        } else {
            return new Watermark((Journal) document);
        }
    }

    public Content getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }


    public String getAuthor() {
        return author.getFirstName() + " " + author.getLastName();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Topic getTopic() {
        // for some reason Include.NON_ABSENT and Include.NON_EMPTY didn't do the trick.
        // Explicitly set the null value and let jackson do it's magic with the Include.NON_NULL
        return topic.orElse(null);
    }
}
