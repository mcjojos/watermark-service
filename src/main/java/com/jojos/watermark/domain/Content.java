package com.jojos.watermark.domain;

/**
 * Every different type of content goes here.
 *
 * @author gkaranikas
 */
public enum Content {

    Book,
    Journal;

    public static <T> Content forClass(Class<T> clazz) {
        for (Content content : values()) {
            if (clazz.getSimpleName().equals(content.name())) {
                return content;
            }
        }
        return null;
    }

}
