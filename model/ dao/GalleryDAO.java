package dao;

import model.Gallery;

import java.util.ArrayList;
import java.util.List;

public class GalleryDAO {
    private final List<Gallery> galleries = new ArrayList<>();

    public void addGallery(Gallery gallery) {
        galleries.add(gallery);
    }

    public List<Gallery> getAll() {
        return galleries;
    }

    public Gallery getById(int id) {
        return galleries.stream()
                .filter(g -> g.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
