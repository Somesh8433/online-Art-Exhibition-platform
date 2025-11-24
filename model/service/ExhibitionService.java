package service;

import dao.ArtworkDAO;
import dao.GalleryDAO;
import model.Artwork;
import model.Gallery;

import java.util.List;

public class ExhibitionService {

    private final ArtworkDAO artworkDAO = new ArtworkDAO();
    private final GalleryDAO galleryDAO = new GalleryDAO();

    public ExhibitionService() {
        // Some sample data
        addGallery(1, "Modern Art Gallery");
        addGallery(2, "Classic Art Gallery");

        addArtwork(101, "Sunset Dreams", "A. Sharma", 15000);
        addArtwork(102, "City Lights", "R. Verma", 22000);
        addArtwork(103, "Nature Bliss", "K. Rao", 18000);

        addArtworkToGallery(1, 101);
        addArtworkToGallery(1, 102);
        addArtworkToGallery(2, 103);
    }

    public void addGallery(int id, String name) {
        galleryDAO.addGallery(new Gallery(id, name));
    }

    public void addArtwork(int id, String title, String artist, double price) {
        artworkDAO.addArtwork(new Artwork(id, title, artist, price));
    }

    public void addArtworkToGallery(int galleryId, int artworkId) {
        Gallery g = galleryDAO.getById(galleryId);
        Artwork a = artworkDAO.getById(artworkId);

        if (g != null && a != null) {
            g.addArtwork(a);
        }
    }

    public List<Gallery> getGalleries() {
        return galleryDAO.getAll();
    }

    public List<Artwork> getArtworks() {
        return artworkDAO.getAll();
    }

    public List<Artwork> getArtworksInGallery(int galleryId) {
        Gallery g = galleryDAO.getById(galleryId);
        if (g != null) {
            return g.getArtworks();
        }
        return List.of();
    }

    public Artwork getArtworkById(int id) {
        return artworkDAO.getById(id);
    }
}
