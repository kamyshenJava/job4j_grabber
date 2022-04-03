package ru.job4j.grabber;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection con;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
            con = DriverManager.getConnection(
                    cfg.getProperty("url"),
                    cfg.getProperty("username"),
                    cfg.getProperty("password")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Post post) {
        String sql = "insert into posts(title, link, description, created) values (?, ?, ?, ?)";
        Timestamp created = Timestamp.valueOf(post.getCreated());
        try (PreparedStatement ps = con.prepareStatement(sql)) {
             ps.setString(1, post.getTitle());
             ps.setString(2, post.getLink());
             ps.setString(3, post.getDescription());
             ps.setTimestamp(4, created);
             ps.execute();
             try (ResultSet result = ps.getGeneratedKeys()) {
                if (result.next()) {
                    post.setId(result.getInt(1));
                }
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        String sql = "select * from posts";
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    posts.add(getPostFromResult(result));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        String sql = "select * from posts where id = ?";
        Post post = null;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    post = getPostFromResult(result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (con != null) {
            con.close();
        }
    }

    private Post getPostFromResult(ResultSet result) throws SQLException {
        return new Post(
                result.getInt("id"),
                result.getString("title"),
                result.getString("link"),
                result.getString("description"),
                result.getTimestamp("created").toLocalDateTime()
                );
    }

    public static void main(String[] args) {
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("app.properties")) {
            Properties cfg = new Properties();
            cfg.load(in);
            PsqlStore psqlStore = new PsqlStore(cfg);
            Post post1 = new Post("Junior Developer",
                    "yandex.ru",
                    "Cool position",
                    LocalDateTime.now());
            Post post2 = new Post("Middle Developer",
                    "google.com",
                    "we hire anyone",
                    LocalDateTime.now());
            Post post3 = new Post("Senior Developer",
                    "microsoft.com",
                    "no education is required",
                    LocalDateTime.now());
            psqlStore.save(post1);
            psqlStore.save(post2);
            psqlStore.save(post3);
            System.out.println(psqlStore.getAll());
            System.out.println(psqlStore.findById(1));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
