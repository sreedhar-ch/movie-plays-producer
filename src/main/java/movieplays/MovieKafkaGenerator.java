package movieplays;

import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.Record;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class MovieKafkaGenerator {

    private List<Movie> movies = List.of(
            new Movie(1, "The Hobbit", "Peter Jackson", "Fantasy"),
            new Movie(2, "Star Trek: First Contact", "Jonathan Frakes", "Space"),
            new Movie(3, "Encanto", "Jared Bush", "Animation"),
            new Movie(4, "Cruella", "Craig Gillespie", "Crime Comedy"),
            new Movie(5, "Sing 2", "Garth Jennings", "Jukebox Musical Comedy")
    );

    // Populates movies into Kafka topic
    @Outgoing("movies")
    public Multi<Record<Integer, Movie>> movies() {
        System.out.println("Publishing movies::"+movies);
        return Multi.createFrom().items(movies.stream()
                .map(m -> Record.of(m.id, m))
        );
    }

    private Random random = new Random();

    /*@Inject
    Logger logger;*/

    @Outgoing("play-time-movies")
    public Multi<Record<String, PlayedMovie>> generate() {
        return Multi.createFrom().ticks().every(Duration.ofMillis(1000))
                .onOverflow().drop()
                .map(tick -> {
                    Movie movie = movies.get(random.nextInt(movies.size()));
                    movie.name=random.nextInt(movies.size())+"-name";
                    movie.director=random.nextInt(movies.size())+"-director";
                    movie.genre=random.nextInt(movies.size())+"-genre";
                    int time = random.nextInt(300);
                    //logger.info("movie {0} played for {1} minutes", movie.name, time);

                    //System.out.println("Play time movie="+movie.name+" played for "+time+" minutes");
                    // Region as key
                    return Record.of("eu", new PlayedMovie(movie.id, time));
                });
    }
}
