package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        HabrCareerParse parser = new HabrCareerParse();
        for (int i = 1; i < 6; i++) {
            String pageLink = String.format("%s?page=%d", PAGE_LINK, i);
            parser.parsePage(pageLink).forEach(System.out::println);
        }
    }

    private List<String> parsePage(String pageLink) throws IOException {
        List<String> rsl = new ArrayList<>();
        Connection connection = Jsoup.connect(pageLink);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element dateElement = row.select(".vacancy-card__date").first();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            ZonedDateTime date = ZonedDateTime.parse(dateElement.child(0).attr("datetime"));
            String dateString = date.format(formatter);
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();
            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            rsl.add(String.format("%s %s %s%n", dateString, vacancyName, link));
        });
        return rsl;
    }
}
