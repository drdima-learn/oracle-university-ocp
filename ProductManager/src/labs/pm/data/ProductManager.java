package labs.pm.data;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProductManager {


    private Map<Product, List<Review>> products = new HashMap<>();

    private ResourceFormatter formatter;

    private static Map<String, ResourceFormatter> formatters
            = Map.of("en-GB", new ResourceFormatter(Locale.UK),
            "en-US", new ResourceFormatter(Locale.US),
            "fr-FR", new ResourceFormatter(Locale.FRANCE),
            "ru-RU", new ResourceFormatter(new Locale("ru", "RU")),
            "zh-CN", new ResourceFormatter(Locale.CHINA)
    );


    public ProductManager(Locale locale) {
        this(locale.toLanguageTag());
    }

    public ProductManager(String languageTag) {
        changeLocal(languageTag);
    }

    public void changeLocal(String languageTag) {
        formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
    }

    public static Set<String> getSupportedLocale() {
        return formatters.keySet();
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product product = new Food(id, name, price, rating, bestBefore);
        products.putIfAbsent(product, new ArrayList<>());
        return product;
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
        Product product = new Drink(id, name, price, rating);
        products.putIfAbsent(product, new ArrayList<>());
        return product;
    }

//    public Product findProduct(int id) {
//        Product result = null;
//        for (Product product : products.keySet()) {
//            if (product.getId() == id) {
//                result = product;
//                break;
//            }
//        }
//        return result;
//    }

    public Product findProduct(int id) {
        return products.keySet().stream().filter(p -> p.getId() == id).findFirst().orElseGet(() -> null);
    }

    public Product reviewProduct(int id, Rating rating, String comments) {
        return reviewProduct(findProduct(id), rating, comments);
    }

    public Product reviewProduct(Product product, Rating rating, String comments) {
        List<Review> reviews = products.get(product);
        //reviewsStatic = reviews;
        products.remove(product);
        reviews.add(new Review(rating, comments));


//        long average = Math.round(reviews.stream().mapToInt(r->r.getRating().ordinal()).average().orElseGet(()->0));
//        product = product.applyRating((int) average);

        product = product.applyRating(Rateble.convert((int) Math.round(reviews.stream().mapToInt(r -> r.getRating().ordinal()).average().orElse(0))));
//        int sum = 0;
//        for (Review review : reviews) {
//            sum += review.getRating().ordinal();
//        }
//
//        product = product.applyRating(Rateble.convert(Math.round((float) sum / reviews.size())));
        products.put(product, reviews);
        return product;
    }

    public void printProductReport(int id) {
        printProductReport(findProduct(id));
    }

    private void printProductReport(Product product) {
        List<Review> reviews = products.get(product);
        StringBuilder txt = new StringBuilder();
        txt.append(formatter.formatProduct(product));
        txt.append('\n');

        Collections.sort(reviews);
        reviews.sort(null);

        if (reviews.isEmpty()) {
            txt.append(formatter.getText("no.reviews") + '\n');
        } else {
            //reviews.stream().forEach(r->txt.append(formatter.formatReview(r) + '\n'));
            txt.append(reviews.stream().map(r -> formatter.formatReview(r) + '\n').collect(Collectors.joining()));
        }


//
//        for (Review review : reviews) {
//            txt.append(formatter.formatReview(review));
//            txt.append('\n');
//        }
//        if (reviews.isEmpty()) {
//            txt.append(formatter.getText("no.reviews"));
//            txt.append('\n');
//        }
        System.out.println(txt);
    }

    public void printProducts(Predicate<Product> filter, Comparator<Product> sorter) {
        //List<Product> productList = new ArrayList<>(products.keySet());
        //productList.sort(sorter);

        StringBuilder txt = new StringBuilder();
//        for (Product product : productList) {
//            txt.append(formatter.formatProduct(product) + '\n');
//        }

//        txt.append(
//                products.keySet().stream().sorted(sorter).map(p -> formatter.formatProduct(p) + '\n').collect(Collectors.joining())
//        );
        products.keySet().stream().sorted(sorter).filter(filter).forEach(p->txt.append(formatter.formatProduct(p)+'\n'));

        //productList.forEach( (p) -> txt.append(formatter.formatProduct(p) + '\n'));
        System.out.println(txt);

    }

    public Map<String, String> getDiscount(){

        return products.keySet()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                product -> product.getRating().getStars(),
                                Collectors.collectingAndThen(
                                    Collectors.summarizingDouble(
                                        product -> product.getDiscount().doubleValue()),
                                 discount->formatter.moneyFormat.format(discount.getSum())

                                )
                        ));



    }


    private static class ResourceFormatter {
        private Locale locale;
        private ResourceBundle resources;
        private DateTimeFormatter dateFormat;
        private NumberFormat moneyFormat;

        private ResourceFormatter(Locale locale) {
            this.locale = locale;
            resources = ResourceBundle.getBundle("labs.pm.data.resources", locale);
            dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
            moneyFormat = NumberFormat.getCurrencyInstance(locale);
        }

        private String formatProduct(Product product) {
            return MessageFormat.format(resources.getString("product"),
                    product.getName(),
                    moneyFormat.format(product.getPrice()),
                    product.getRating().getStars(),
                    dateFormat.format(product.getBestBefore()));
        }

        private String formatReview(Review review) {
            return MessageFormat.format(resources.getString("review"),
                    review.getRating().getStars(),
                    review.getComments());
        }

        private String getText(String key) {
            return resources.getString(key);
        }


    }
}
