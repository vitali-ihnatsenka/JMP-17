package by.epam.jmp;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

import org.bson.Document;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final int MOVIES_NUMBER = 10;
    private static final int USERS_NUMBER = 30;
    private static final int MAX_MOVIES_PER_USER = 8;
    private static final int MESSAGES_PER_FRIEND = 20;

    public static void main(String[] args) {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase mongoDatabase = mongoClient.getDatabase("jmp");

        mongoDatabase.getCollection("movies").drop();
        mongoDatabase.getCollection("users").drop();
        //create movies
        List<Document> movies = new ArrayList<>();
        for (int i = 0; i < MOVIES_NUMBER; i++) {
            Document doc = new Document("name", "name" + i).append("length", i * 3).append("date", getRandomDate());
            movies.add(doc);
        }
        mongoDatabase.getCollection("movies").insertMany(movies);

        //Create users
        List<Document> users = new ArrayList<>();
        for (int i = 0; i < USERS_NUMBER; i++){
            Document doc = new Document("name", "name" + i);
            doc.append("movies", getRandomDocumentSublist(movies, 1 + new Random().nextInt(MAX_MOVIES_PER_USER)));
            users.add(doc);
        }

        //add friends
        for(Document user: users) {
            List<Document> friends = getRandomDocumentSublist(users, 1 + new Random().nextInt(30))
                    .stream()
                    .filter(friend -> !friend.equals(user))
                    .map(friend -> {
                        List<Document> messages = new ArrayList<>();
                        for (int i = 0; i < MESSAGES_PER_FRIEND; i++) {
                            messages.add(new Document("text", "text" + i).append("date", getRandomDate()));
                        }
                        friend.remove("friends");
                        friend.remove("movies");
                        return friend.append("messages", messages).append("friendshipDate", getRandomDate());
                    })
                    .collect(Collectors.toList());
            user.append("friends", friends);
            mongoDatabase.getCollection("users").insertOne(user);
        }

        //Average number of messages by monday

        MongoCursor<Document> iterator =
                mongoDatabase.
                        getCollection("users").
                        aggregate(Arrays.asList(
                                unwind("$friends"),
                                unwind("$friends.messages"),
                                group(new BasicDBObject("$dayOfWeek", "$friends.messages.date"),
                                        push("messages", "$friends.messages")),
                                match(eq("_id", 2)),
                                unwind("$messages"),
                                group(new BasicDBObject("$dayOfYear", "$messages.date"),
                                        sum("count", 1)),
                                group("_id", avg("avg", "$count"))
                        )).iterator();
        try {
            while (iterator.hasNext()){
                System.out.println("Average number of messages for monday - " + iterator.next().get("avg"));
            }
        } finally {
            iterator.close();
        }

        //Max number of new friendships from month to month
        iterator =
                mongoDatabase.
                        getCollection("users").aggregate(Arrays.asList(
                            unwind("$friends"),
                            group(new BasicDBObject("$month", "$friends.friendshipDate"),
                                    sum("count", 1))
                )).iterator();
        try{
            while (iterator.hasNext()) {
                Document doc = iterator.next();
                System.out.println("Number of friendships for " + doc.get("_id") + " month - " + doc.get("count") );
            }
        }finally {
            iterator.close();
        }

        //Min number of watched movies by users with more than 20 friends
        iterator =
                mongoDatabase.
                        getCollection("users").aggregate(Arrays.asList(
                        project(fields(include("name", "movies"), computed("friendsCount", new BasicDBObject("$size", "$friends")))),
                        match(gt("friendsCount", 20)),
                        project(fields(include("name"), computed("movieCount", new BasicDBObject("$size", "$movies")))),
                        group("$name", min("minMovieCount", "$movieCount"))
                )).iterator();
        try{
            if (iterator.hasNext()) {
                Document doc = iterator.next();
                System.out.println("Min number of watched movies by users with more than 20 friends - " + doc.get("minMovieCount"));
            }
        }finally {
            iterator.close();
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
