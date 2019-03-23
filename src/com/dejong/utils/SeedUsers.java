package com.dejong.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SeedUsers {

    public static void main(String args[]) throws IOException {
        List<Users> users = seedUsers();
        save(users);
        makeFolder(users);
        makeFiles();
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

    /**
     * This method is used output seed users into users.dat
     * @param users list of users to be saved
     * @throws IOException
     */
    public static void save(List<Users> users) throws IOException {
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
            System.out.println("File not found. Please run SeedUsers class");
        } catch (Exception ex) {
            System.out.println("900: System Error");
        }
        return users;
    } // end open

    //create unique folder
    private static void makeFolder(List<Users> users) {
        String path = "C://DC//"; //main path to store unique folder
        File dir;

        for(Users u: users) {
           dir = new File(path + u.getUsername());
           if(dir.mkdirs()) {
               System.out.println(dir.toString() + " has been created");
           }
        } //end for
    } //end make folder

    //seed files from code
    //reference: https://howtodoinjava.com/java/io/how-to-create-a-new-file-in-java/
    private static void makeFiles() {
        String path = "C://DC//";  //main path to store files
        String filename = "test.txt";
        String data = "Hello World! This is a test file.";
        try {
            Files.write(Paths.get(path + filename), data.getBytes(StandardCharsets.UTF_8)); //create new files
            System.out.println("Test files created.");
        } catch(Exception ex) {
            System.out.println("Failed to create test files.");
        }
    }
}
