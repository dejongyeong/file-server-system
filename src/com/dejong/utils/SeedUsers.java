package com.dejong.utils;

import java.io.*;
import java.util.*;

public class SeedUsers {

    public static void main(String args[]) throws IOException {
        List<Users> users = seedUsers();
        save(users);
    }

    // generate users
    private static List<Users> seedUsers() {
        List<Users> users = new ArrayList<>();

        // insert into list
        users.add(new Users("admin", "admin"));
        users.add(new Users("john", "john"));
        users.add(new Users("michael", "michael"));

        return users;
    } // end seed users

    // output seed users into users.dat
    private static void save(List<Users> users) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("users.dat"));
        oos.writeObject(users);
        oos.close();
    } // end save

    /**
     * This method is used to open data.
     * @return a list of seeded users.
     */
    public static List<Users> open() {
        List<Users> users = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("users.dat"));
            users = (ArrayList<Users>) ois.readObject();
            ois.close();
        } catch (FileNotFoundException ex) {
            System.out.println("File not found. Please run SeedUsers module");
        } catch (Exception ex) {
            System.out.println("900: System Error");
        }
        return users;
    } // end open
}
