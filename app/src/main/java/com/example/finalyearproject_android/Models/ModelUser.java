package com.example.finalyearproject_android.Models;

public class ModelUser {
    private String uId, name, email, phone, image, role, gender, height, weight, age;

    public ModelUser() {
    }

    public ModelUser(String uId, String email) {
        this.uId = uId;
        this.name = "null";
        this.email = email;
        this.phone = "null";
        this.image = "default";
        this.role = "user";
        this.gender = "null";
        this.height = "70.0";
        this.weight = "50.0";
        this.age = "20";
    }

    public ModelUser(String uId, String name, String email, String phone, String image, String gender, String height, String weight, String age) {
        this.uId = uId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.image = image;
        this.gender = gender;
        this.height = height;
        this.age = age;
        this.weight=weight;
        this.role="user";
    }

    public ModelUser(String uId, String name, String email, String phone, String gender, String height, String weight, String age) {
        this.uId = uId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.height = height;
        this.age = age;
        this.weight = weight;
        this.image="default";
        this.role="user";
    }

    public ModelUser(String uId, String name, String email, String phone, String image, String role, String gender, String height, String weight, String age) {
        this.uId = uId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.image = image;
        this.gender = gender;
        this.height = height;
        this.age = age;
        this.role = role;
        this.weight = weight;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}
