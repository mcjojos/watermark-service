package com.jojos.watermark.domain;

import java.util.Optional;

/**
 * Super type for every document
 *
 * Common attributes of all documents include the content type, the title and the author.
 *
 * @author gkaranikas
 */
public interface Document {

    String getTitle();

    Author getAuthor();

    Optional<Watermark> getWatermark();

    Document withWatermark(Watermark watermark);
}
