package by.epam.jmp;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase mongoDatabase = mongoClient.getDatabase("jmp");

        mongoDatabase.getCollection("movies").drop();
        mongoDatabase.getCollection("users").drop();
        //create movies
        List<Document> movies = new ArrayList<Document>();
        for (int i = 0; i < 10; i++) {
            Document doc = new Document("name", "name" + i).append("length", i * 3).append("date", getRandomDate());
            movies.add(doc);
        }
        mongoDatabase.getCollection("movies").insertMany(movies);

        //Create users
        List<Document> users = new ArrayList<Document>();
        for (int i = 0; i < 10; i++){
            Document doc = new Document("name", "name" + i);
            doc.append("movies", getRandomDocumentSublist(movies, 3));
            users.add(doc);
        }

        //add friends
        for(Document user: users) {
            List<Document> friends = getRandomDocumentSublist(users, 4)
                    .stream()
                    .filter(friend->!friend.equals(user))
                    .map(friend->{
                        List<Document> messages = new ArrayList<>();
                        for(int i = 0; i < 20; i++){
                            messages.add(new Document("text", "text" + i).append("date", getRandomDate()));
                        }
                        friend.remove("friends");
                        friend.remove("movies");
                        return friend.append("messages", messages);
                    })
                    .collect(Collectors.toList());
            user.append("friends", friends);
            mongoDatabase.getCollection("users").insertOne(user);
        }
    }

    public static List<Document> getRandomDocumentSublist(List<Document> superList, int sublistSize ){
        if(sublistSize > superList.size() || sublistSize < 0){
            throw new IllegalArgumentException("Incorrect sublist size");
        }
        List<Document> sublist = new ArrayList<>();
        for(int i = 0; i < sublistSize; i++){
            sublist.add(new Document(superList.get(new Random().nextInt(superList.size()))));
        }
        return sublist;
    }

    public static Date getRandomDate(){
        long offset = Timestamp.valueOf("2016-01-01 00:00:00").getTime();
        long end = Timestamp.valueOf("2017-01-01 00:00:00").getTime();
        long diff = end - offset + 1;
        return new Date(offset + (long)(Math.random() * diff));
    }

}
