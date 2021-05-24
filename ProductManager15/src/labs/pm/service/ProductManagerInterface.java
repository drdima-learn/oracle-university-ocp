package labs.pm.service;

import labs.pm.data.Product;
import labs.pm.data.Rating;
import labs.pm.data.Review;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface ProductManagerInterface {
    Product createProduct(int id, String name, BigDecimal price, Rating rating) throws ProductManagerException;
    Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) throws ProductManagerException;
    Product reviewProduct(int id, Rating rating, String comments) throws ProductManagerException;
    Product findProduct(int id) throws ProductManagerException;
    List<Product> findProducts(Predicate<Product> filter) throws ProductManagerException;
    List<Review> findReviews(int id) throws ProductManagerException;
    Map<Rating, BigDecimal> getDiscount() throws ProductManagerException;
}
