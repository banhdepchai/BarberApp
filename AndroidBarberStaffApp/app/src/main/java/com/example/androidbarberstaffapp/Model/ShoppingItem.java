package com.example.androidbarberstaffapp.Model;

public class ShoppingItem {
    private String id, name, image;
    private Long price;

    public ShoppingItem() {
    }

    public ShoppingItem(String id, String name, String image, Long price) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public ShoppingItem setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ShoppingItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getImage() {
        return image;
    }

    public ShoppingItem setImage(String image) {
        this.image = image;
        return this;
    }

    public Long getPrice() {
        return price;
    }

    public ShoppingItem setPrice(Long price) {
        this.price = price;
        return this;
    }
}
