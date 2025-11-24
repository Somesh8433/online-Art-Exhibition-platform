package dao;

import model.Artwork;

import java.util.ArrayList;
import java.util.List;

public class ArtworkDAO {
    private final List<Artwork> artworks = new ArrayList<>();

    public void addArtwork(Artwork artwork) {
        artworks.add(artwork);
    }

    public List<Artwork> getAll() {
        return artworks;
    }

    public Artwork getById(int id) {
        return artworks.stream()
                .filter(a -> a.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
