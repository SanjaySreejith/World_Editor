import processing.core.PImage;

import java.util.List;
import java.util.Objects;

public abstract class Entity {

    private Point position;
    private String id;
    private List<PImage> images;
    private int imageIndex;

    protected Entity(Point position, String id, List<PImage> images, int imageIndex) {
        this.id = id;
        this.images = images;
        this.imageIndex = imageIndex;
        this.position = position;
    }

    public Point getPosition() {
        return this.position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public String getId() {
        return this.id;
    }

    public List<PImage> getImages() {
        return images;
    }

    public int getImageIndex() {
        return imageIndex;
    }

    public void setImageIndex(int imageIndex) {
        this.imageIndex = imageIndex;
    }

    public PImage getCurrentImage() {
        return this.getImages().get(this.imageIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity)) return false;
        Entity entity = (Entity) o;
        return getId().equals(entity.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    public String createStoreLine() {
        String className = this.getId().split("_")[0];
        String storeLine = className + " " + this.getId() + " " + this.getPosition().x + " " + this.getPosition().y;
        storeLine = this._storeLine(storeLine);
        return storeLine;
    }

    protected abstract String _storeLine(String line);

}
