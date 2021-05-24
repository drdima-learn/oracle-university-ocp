package labs.file.service;

import labs.pm.data.*;
import labs.pm.service.ProductManagerException;
import labs.pm.service.ProductManagerInterface;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProductFileManager implements ProductManagerInterface {

    private Map<Product, List<Review>> products = new HashMap<>();
    private static final Logger logger = Logger.getLogger(ProductFileManager.class.getName());
    private static final ResourceBundle config = ResourceBundle.getBundle("labs.file.service.config");
    private static final Path dataFolder = Path.of(config.getString("data.folder"));
    private static final MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private static final MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private static final Charset charset = Charset.forName("UTF-8");

    public ProductFileManager() {
        loadAllData();
    }


    @Override
    public Product createProduct(int id, String name, BigDecimal price, Rating rating) throws ProductManagerException {
        Product product = new Drink(id, name, price, rating);
        products.putIfAbsent(product, new ArrayList<>());
        return product;
    }

    @Override
    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) throws ProductManagerException {
        Product product = new Food(id, name, price, rating, bestBefore);
        products.putIfAbsent(product, new ArrayList<>());
        return product;
    }

    @Override
    public Product reviewProduct(int id, Rating rating, String comments) throws ProductManagerException {
        return reviewProduct(findProduct(id), rating, comments);
    }


    private Product reviewProduct(Product product, Rating rating, String comments) throws ProductManagerException {
        List<Review> reviews = products.get(product);
        products.remove(product);
        reviews.add(new Review(rating, comments));
        product = product.applyRating(Rateble.convert((int) Math.round(reviews.stream().mapToInt(r -> r.getRating().ordinal()).average().orElse(0))));
        products.put(product, reviews);
        return product;
    }

    @Override
    public Product findProduct(int id) throws ProductManagerException {
        return products.keySet().stream().filter(p -> p.getId() == id).findFirst().orElseThrow(() -> new ProductManagerException("Product with id " + id + " not found"));
    }

    @Override
    public List<Product> findProducts(Predicate<Product> filter) throws ProductManagerException {
        return products.keySet().stream().filter(filter).collect(Collectors.toList());
    }

    @Override
    public List<Review> findReviews(int id) throws ProductManagerException {
        return products.get(findProduct(id));
    }

    @Override
    public Map<Rating, BigDecimal> getDiscount() throws ProductManagerException {
        return products.keySet()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                product -> product.getRating(),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingDouble(
                                                product -> product.getDiscount().doubleValue()),
                                        discount -> BigDecimal.valueOf(discount.getSum())

                                )
                        ));
    }

    private void loadAllData() {
        try {
            products = Files.list(dataFolder).filter(file -> file.getFileName().toString().startsWith("product"))
                    .map(file -> loadProduct(file))
                    .filter(product -> product != null)
                    .collect(Collectors.toMap(product -> product,
                            product -> loadReviews(product)
                    ));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error loading data " + ex.getMessage());
        }
    }

    private Product loadProduct(Path file) {
        Product product = null;
        try {
            product = parseProduct(Files.lines(dataFolder.resolve(file), Charset.forName("UTF-8")).findFirst().orElseThrow());
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error loading product " + ex.getMessage());
        }
        return product;
    }

    private List<Review> loadReviews(Product product) {
        List<Review> reviews = null;
        Path file = dataFolder.resolve(MessageFormat.format(config.getString("reviews.data.file"), product.getId()));
        if (Files.notExists(file)) {
            reviews = new ArrayList<>();
        } else {
            try {
                reviews = Files.lines(file, Charset.forName("UTF-8")).map(text -> parseReview(text)).filter(review -> review != null).collect(Collectors.toList());
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Error loading reviews " + ex.getMessage());
            }
        }
        return reviews;

    }

    private Review parseReview(String text) {
        Review review = null;
        try {
            Object[] values = reviewFormat.parse(text);
            //reviewProduct(Integer.parseInt((String) values[0]), Rateble.convert(Integer.parseInt((String) values[1])), (String) values[2]);
            review = new Review(Rateble.convert(Integer.parseInt((String) values[0])), (String) values[1]);
        } catch (ParseException | NumberFormatException ex) {
            logger.log(Level.WARNING, "Error parsing review " + text + " " + ex.getMessage());
        }
        return review;
    }

    private Product parseProduct(String text) {
        Product product = null;
        try {
            Object[] values = productFormat.parse(text);
            String productType = (String) values[0];
            int id = Integer.parseInt((String) values[1]);
            String name = (String) values[2];
            BigDecimal price = BigDecimal.valueOf(Double.valueOf((String) values[3]));
            Rating rating = Rateble.convert(Integer.parseInt((String) values[4]));

            switch (productType) {
                case "D":
                    product = new Drink(id, name, price, rating);
                    break;
                case "F":
                    LocalDate bestBefore = LocalDate.parse(((String) values[5]));
                    product = new Food(id, name, price, rating, bestBefore);
                    break;
            }

        } catch (ParseException | NumberFormatException | DateTimeParseException ex) {
            logger.log(Level.WARNING, "Error parsing product " + text + " " + ex.getMessage());
        }
        return product;
    }
}
