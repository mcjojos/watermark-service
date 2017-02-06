package com.jojos.watermark.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * Base class for all book publications.
 * Apart from the fields that are defined in {@link Document} there is an additional one of type {@link Topic}
 *
 * @author gkaranikas
 */
public class Book implements Document {

    private final String title;
    private final Author author;
    private final Topic topic;
    private final Optional<Watermark> watermark;

    @JsonCreator
    public Book(@JsonProperty("title") String title,
                @JsonProperty("author") Author author,
                @JsonProperty("topic") Topic topic) {
        this.title = title;
        this.author = author;
        this.topic = topic;
        this.watermark = Optional.empty();
    }

    private Book(String title, Author author, Topic topic, Watermark watermark) {
        this.title = title;
        this.author = author;
        this.topic = topic;
        this.watermark = Optional.of(watermark);
    }

    @Override
    public Book withWatermark(Watermark watermark) {
        return new Book(title, author, topic, watermark);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Author getAuthor() {
        return author;
    }

    @Override
    public Optional<Watermark> getWatermark() {
        return watermark;
    }

    public Topic getTopic() {
        return topic;
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author=" + author +
                ", topic=" + topic +
                '}';
    }
}
