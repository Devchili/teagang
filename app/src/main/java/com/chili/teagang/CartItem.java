package com.chili.teagang;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private String id; // Add this field
    private String name;
    private String size;
    private int quantity;
    private double price;

    // Default constructor required for calls to DataSnapshot.getValue(CartItem.class)
    public CartItem() {
    }

    // Constructor with id
    public CartItem(String id, String name, String size, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.quantity = quantity;
        this.price = price;
    }

    // Constructor without id
    public CartItem(String name, String size, int quantity, double price) {
        this(null, name, size, quantity, price); // Call the constructor with id and set id to null
    }

    public String getId() { // Add this getter
        return id;
    }

    public void setId(String id) { // Add this setter
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { // Add this setter
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) { // Add this setter
        this.size = size;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) { // Add this setter
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) { // Add this setter
        this.price = price;
    }

    // Parcelable implementation
    protected CartItem(Parcel in) {
        id = in.readString(); // Add this line
        name = in.readString();
        size = in.readString();
        quantity = in.readInt();
        price = in.readDouble();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id); // Add this line
        dest.writeString(name);
        dest.writeString(size);
        dest.writeInt(quantity);
        dest.writeDouble(price);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", size='" + size + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
