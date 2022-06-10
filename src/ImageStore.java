import processing.core.PImage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class ImageStore
{
    private static final String DEFAULT_IMAGE_NAME = "background_default";

    private final Map<String, List<PImage>> images;
    private final List<PImage> defaultImages;

    public ImageStore(PImage defaultImage) {
        this.images = new HashMap<>();
        defaultImages = new LinkedList<>();
        defaultImages.add(defaultImage);
    }

    public List<PImage> getImageList(String key) {
        return this.images.getOrDefault(key, this.defaultImages);
    }

    public Map<String, List<PImage>> getImages() {
        return images;
    }

    public Background createDefaultBackground() {
        return new Background(DEFAULT_IMAGE_NAME, getImageList(DEFAULT_IMAGE_NAME));
    }

    public List<PImage> getImages(
            Map<String, List<PImage>> images, String key)
    {
        List<PImage> imgs = images.get(key);
        if (imgs == null) {
            imgs = new LinkedList<>();
            images.put(key, imgs);
        }
        return imgs;
    }


}
