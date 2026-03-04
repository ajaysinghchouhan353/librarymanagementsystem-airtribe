package org.lms.entity;

import java.util.Objects;

/**
 * Represents a library branch.
 */
public class Branch {
    private final String id;
    private String name;
    private String address;
    private String phoneNumber;

    public Branch(String id, String name, String address) {
        this.id = Objects.requireNonNull(id, "Branch ID cannot be null");
        this.name = Objects.requireNonNull(name, "Branch name cannot be null");
        this.address = address;
    }

    public Branch(String id, String name, String address, String phoneNumber) {
        this.id = Objects.requireNonNull(id, "Branch ID cannot be null");
        this.name = Objects.requireNonNull(name, "Branch name cannot be null");
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return id.equals(branch.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Branch{id='%s', name='%s', address='%s'}", id, name, address);
    }
}
