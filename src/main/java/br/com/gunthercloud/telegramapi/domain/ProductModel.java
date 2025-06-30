package br.com.gunthercloud.telegramapi.domain;

public class ProductModel {
    private String name;
    private String description;
    private double price;
    private String link;
    private int quantity;

    public ProductModel(String name, String description, double price, int quantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public ProductModel(String name, String description, double price, String link, int quantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.link = link;
        this.quantity = quantity;
    }

    public ProductModel() {
        super();
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}
