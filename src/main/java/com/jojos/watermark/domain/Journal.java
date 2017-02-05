package com.jojos.watermark.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * The base class for all journal publications
 *
 * @author gkaranikas
 */
public class Journal implements Document {

    private final String title;
    private final Author author;
    private final Optional<Watermark> watermark;

    @JsonCreator
    public Journal(@JsonProperty String title, @JsonProperty Author author) {
        this.title = title;
        this.author = author;
        this.watermark = Optional.empty();
    }


    private Journal(String title, Author author, Watermark watermark) {
        this.title = title;
        this.author = author;
        this.watermark = Optional.of(watermark);
    }

    @Override
    public Journal withWatermark(Watermark watermark) {
        return new Journal(title, author, watermark);
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

    @Override
    public String toString() {
        return "Journal{" +
                "title='" + title + '\'' +
                ", author=" + author +
                '}';
    }

}
