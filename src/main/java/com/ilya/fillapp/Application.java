package com.ilya.fillapp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class Application {

    private static final String SQL_CONNECTION_STRING = "jdbc:postgresql://localhost/kinopoisk";
    private static final String KINOPOISK_CONNECTION_STRING = "https://www.kinopoisk.ru/";

    private static final String PREPARED_MOVIES_STRING = "insert into movies(name, position, year, rating, count, date) values(?, ?, ? ,?, ?, ?)";



    /*
    Была создана бд с таблицей:
    create table movies(id serial primary key, name text, position int, year int, rating float, count int, date timestamp );
    */

    public static void main(String[] args) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            int counter = 1;
            for (int i = 1;i <= 5 ; i++){
                Date date = new Date(System.currentTimeMillis());
                connection = DriverManager.getConnection(SQL_CONNECTION_STRING, "postgres", "1234");
                preparedStatement = connection.prepareStatement(PREPARED_MOVIES_STRING);

                Document outerDocument = Jsoup.connect(KINOPOISK_CONNECTION_STRING + "lists/top250/?page=" + i)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        //.proxy("70.185.68.155", 4145)
                        .get();
                Elements elements = outerDocument.select("div.selection-list");



                for (Element el : elements.select("div.desktop-rating-selection-film-item__upper-wrapper")){

                    Document innerDocument = Jsoup.connect(KINOPOISK_CONNECTION_STRING + el.select("a.selection-film-item-meta__link").attr("href"))
                            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                            .referrer("http://www.google.com")
                            .get();

                    preparedStatement.setString(1,innerDocument.select("span[data-tid=\"e2c7ce8a\"]").text());
                    preparedStatement.setInt(2, counter++);
                    preparedStatement.setInt(3, Integer.parseInt(innerDocument.select("a[data-tid=\"a189db02\"]").text()));
                    Elements ratings = innerDocument.select("div[data-tid=\"1e10e426\"]");
                    preparedStatement.setFloat(4, Float.parseFloat(ratings.select("a.film-rating-value").text()));
                    preparedStatement.setInt(5, Integer.parseInt(ratings.select("span.styles_count__3hSWL").text()));
                    preparedStatement.setDate(6, date);
                    System.out.println("Added " + preparedStatement.executeUpdate());
                }
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
                if (preparedStatement != null)
                    preparedStatement.close();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
