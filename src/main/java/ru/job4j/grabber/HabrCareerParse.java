package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HarbCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public List<Post> list(String pageLink) {
        List<Post> posts = new ArrayList<>();
        try {
            Connection connection = Jsoup.connect(pageLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element dateElement = row.select(".vacancy-card__date").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                try {
                    String description = retrieveDescription(link);
                    posts.add(new Post(vacancyName,
                            link, description,
                            dateTimeParser.parse(dateElement.child(0).attr("datetime"))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    private String retrieveDescription(String link) throws IOException {
        StringJoiner description = new StringJoiner(System.lineSeparator());
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".style-ugc");
        rows.forEach(row -> description.add(row.text()));
        return description.toString();
    }

    public static void main(String[] args) {
        DateTimeParser dateTimeParser = new HarbCareerDateTimeParser();
        HabrCareerParse parser = new HabrCareerParse(dateTimeParser);
        for (int i = 1; i < 6; i++) {
            String pageLink = String.format("%s?page=%d", PAGE_LINK, i);
            parser.list(pageLink).forEach(System.out::println);
        }
    }
}
