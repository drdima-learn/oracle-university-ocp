package labs.pm.data;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProductManager {


    private Map<Product, List<Review>> products = new HashMap<>();

    private ResourceBundle config = ResourceBundle.getBundle("labs.pm.data.config");

    private MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));

    private ResourceFormatter formatter;

    private static Map<String, ResourceFormatter> formatters
            = Map.of("en-GB", new ResourceFormatter(Locale.UK),
            "en-US", new ResourceFormatter(Locale.US),
            "fr-FR", new ResourceFormatter(Locale.FRANCE),
            "ru-RU", new ResourceFormatter(new Locale("ru", "RU")),
            "zh-CN", new ResourceFormatter(Locale.CHINA)
    );

    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());

    private Path reportsFolder = Path.of(config.getString("reports.folder"));
    private Path dataFolder = Path.of(config.getString("data.folder"));
    private Path tempFolder = Path.of(config.getString("temp.folder"));


    public ProductManager(Locale locale) {
        this(locale.toLanguageTag());
    }

    public ProductManager(String languageTag) {
        changeLocal(languageTag);
        loadAllData();
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

    public Product findProduct(int id) throws ProductManagerException {
        //Product product =  products.keySet().stream().filter(p -> p.getId() == id).findFirst().orElseGet(() -> null);
        //return products.keySet().stream().filter(p -> p.getId() == id).findFirst().orElseThrow(()->new RuntimeException());

        //return products.keySet().stream().filter(p -> p.getId() == id).findFirst().get();
        return products.keySet().stream().filter(p -> p.getId() == id).findFirst()
                .orElseThrow(() -> new ProductManagerException("Product with id " + id + " not found"));

    }

    public Product reviewProduct(int id, Rating rating, String comments) {
        try {
            return reviewProduct(findProduct(id), rating, comments);
        } catch (ProductManagerException ex) {
            //e.printStackTrace();
            logger.log(Level.INFO, ex.getMessage());

        }
        return null;
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
        try {
            printProductReport(findProduct(id));
        } catch (ProductManagerException ex) {
            logger.log(Level.INFO, ex.getMessage());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error printing product report " + ex.getMessage());
        }

    }

    private void printProductReport(Product product) throws IOException {
        List<Review> reviews = products.get(product);
        Collections.sort(reviews);
        Path productReportFile = reportsFolder.resolve(MessageFormat.format(config.getString("report.file"), product.getId()));
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(productReportFile, StandardOpenOption.CREATE), "UTF-8"));) {
            out.append(formatter.formatProduct(product) + System.lineSeparator());
            if (reviews.isEmpty()) {
                out.append(formatter.getText("no.reviews") + System.lineSeparator());
            } else {
                out.append(reviews.stream().map(r -> formatter.formatReview(r) + System.lineSeparator()).collect(Collectors.joining()));
            }
        }

    }

    private void dumpData(){
        try{
            if (Files.notExists(tempFolder)){
                Files.createDirectory(tempFolder);
            }
            Path tempFile = tempFolder.resolve(Path.of(MessageFormat.format(config.getString("temp.file"), Instant.now().toString().replace(":","_"))));
            //Path tempFilePath = tempFolder.resolve(tempFile);
            try(ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(tempFile, StandardOpenOption.CREATE))){
                out.writeObject(products);
                products = new HashMap<>();
            }
        } catch (IOException ex){
            logger.log(Level.SEVERE, "Error dumping data " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private void restoreData(){
        try{
            Path tempFile = Files.list(tempFolder).filter(path -> path.getFileName().toString().endsWith("tmp")).findAny().orElseThrow();
            try(ObjectInputStream in= new ObjectInputStream(Files.newInputStream(tempFile, StandardOpenOption.DELETE_ON_CLOSE))){
                products = (HashMap) in.readObject();
            }
        } catch (Exception ex){
            logger.log(Level.SEVERE, "Error restoring data " + ex.getMessage(), ex);
        }
    }

    private void loadAllData(){
        try {
            products = Files.list(dataFolder).filter(file->file.getFileName().toString().startsWith("product"))
                    .map(file->loadProduct(file))
                    .filter(product -> product!=null)
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
        products.keySet().stream().sorted(sorter).filter(filter).forEach(p -> txt.append(formatter.formatProduct(p) + '\n'));

        //productList.forEach( (p) -> txt.append(formatter.formatProduct(p) + '\n'));
        System.out.println(txt);

    }

    public Map<String, String> getDiscount() {

        return products.keySet()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                product -> product.getRating().getStars(),
                                Collectors.collectingAndThen(
                                        Collectors.summarizingDouble(
                                                product -> product.getDiscount().doubleValue()),
                                        discount -> formatter.moneyFormat.format(discount.getSum())

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
