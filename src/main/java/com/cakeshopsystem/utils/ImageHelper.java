package com.cakeshopsystem.utils;

import javafx.beans.binding.Bindings;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImageHelper {

    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();

    private static final Image DEFAULT_AVATAR =
            new Image(ImageHelper.class.getResource("/images/default-profile.jpg").toExternalForm(), true);

    public static Image load(String path) {
        if (path == null || path.isBlank()) return DEFAULT_AVATAR;

        return CACHE.computeIfAbsent(path, p -> {
            try {
                // already a URL (http/file/jar)
                if (p.matches("^(https?|file|jar):.*")) {
                    return new Image(p, true);
                }

                // classpath resource (e.g. "/images/default-profile.jpg")
                URL res = ImageHelper.class.getResource(p);
                if (res != null) {
                    return new Image(res.toExternalForm(), true);
                }

                // filesystem path
                File f = new File(p);
                if (f.exists()) {
                    return new Image(f.toURI().toString(), true);
                }

            } catch (Exception ignored) { }

            return DEFAULT_AVATAR;
        });
    }

    public static void applyCircularAvatar(ImageView view, double size) {
        if (view == null) return;

        // size
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setPreserveRatio(true);
        view.setSmooth(true);

        // crop (center square)
        Image img = view.getImage();
        if (img != null && img.getWidth() > 0 && img.getHeight() > 0) {
            double w = img.getWidth();
            double h = img.getHeight();
            double side = Math.min(w, h);

            double x = (w - side) / 2.0;
            double y = (h - side) / 2.0;

            view.setViewport(new Rectangle2D(x, y, side, side));
        } else {
            view.setViewport(null); // if no image, don't keep old viewport
        }

        // circular clip (always centered)
        Circle clip = (view.getClip() instanceof Circle c) ? c : new Circle();
        clip.centerXProperty().bind(view.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(view.fitHeightProperty().divide(2));
        clip.radiusProperty().bind(
                Bindings.min(view.fitWidthProperty(), view.fitHeightProperty()).divide(2)
        );
        view.setClip(clip);
    }

    public static void setCircularAvatar(ImageView view, Image image, double size) {
        if (view == null) return;
        view.setImage(image);
        applyCircularAvatar(view, size);
    }
}

