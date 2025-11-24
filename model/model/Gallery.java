package model;

import java.util.ArrayList;
import java.util.List;

public class Gallery {
    private int id;
    private String name;
    private List<Artwork> artworks = new ArrayList<>();

    public Gallery(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public List<Artwork> getArtworks() { return artworks; }

    public void addArtwork(Artwork artwork) {
        artworks.add(artwork);
    }

    @Override
    public String toString() {
        return id + " | " + name + " (Artworks: " + artworks.size() + ")";
    }
}
