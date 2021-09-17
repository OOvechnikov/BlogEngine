package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {

        initializeDb();
        SpringApplication.run(Main.class, args);

    }

    private static void initializeDb() {
        try (Connection db = DriverManager.getConnection("jdbc:mysql://localhost:3306?serverTimezone=Europe/Moscow & useSSL=false", "root", "rootroot")) {
            db.createStatement().execute("CREATE SCHEMA `blog_engine` DEFAULT CHARACTER SET utf8;");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
