package by.epam.jmp;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase mongoDatabase = mongoClient.getDatabase("jmp");

        //Create users
        List<Document> users = new ArrayList<Document>();
        for (int i = 0; i < 100; i++){
            Document doc = new Document("name", "name" + i);
            users.add(doc);
        }
        mongoDatabase.getCollection("users").insertMany(users);

        //create movies
        List<Document> movies = new ArrayList<Document>();
        for (int i = 0; i < 100; i++){
            Document doc = new Document("name", "name" + i).append("length", i * 3).append("date", getRandomDate());
            movies.add(doc);
        }
        mongoDatabase.getCollection("movies").insertMany(movies);

        //add watched movies
        MongoCursor<Document> cursor = mongoDatabase.getCollection("users").find().iterator();
        try{
            while (cursor.hasNext()){
                cursor.next();
            }
        }finally {
            cursor.close();
        }

        //add friends
        for (int i = 0; i < 100; i++){

        }

        //add messages
        for (int i = 0; i < 100; i++){

        }


    }
    public static Date getRandomDate(){
        long offset = Timestamp.valueOf("2016-01-01 00:00:00").getTime();
        long end = Timestamp.valueOf("2017-01-01 00:00:00").getTime();
        long diff = end - offset + 1;
        return new Date(offset + (long)(Math.random() * diff));
    }

}
