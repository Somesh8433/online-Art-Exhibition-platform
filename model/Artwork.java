package model;

public class Artwork {
    private int id;
    private String title;
    private String artist;
    private double price;

    public Artwork(int id, String title, String artist, double price) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.price = price;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return id + " | " + title + " by " + artist + " | ₹" + price;
    }
}
