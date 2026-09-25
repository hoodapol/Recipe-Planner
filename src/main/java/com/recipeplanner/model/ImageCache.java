package com.recipeplanner.model;

import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImageCache {

    private static final Map<String, Image> cache = new ConcurrentHashMap<>();

    public static Image get(String url, double width, double height) {
        return cache.computeIfAbsent(url, u -> new Image(u, width, height, false, true, true));
    }
}