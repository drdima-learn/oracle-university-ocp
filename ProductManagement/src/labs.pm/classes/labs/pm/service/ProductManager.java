//package labs.pm.service;
//
//import labs.client.ResourceFormatter;
//import labs.pm.data.*;
//
//import java.io.*;
//import java.math.BigDecimal;
//import java.nio.charset.Charset;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardOpenOption;
//import java.text.MessageFormat;
//import java.text.ParseException;
//import java.time.Instant;
//import java.time.LocalDate;
//import java.time.format.DateTimeParseException;
//import java.util.*;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantReadWriteLock;
//import java.util.function.Predicate;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.stream.Collectors;
//
//public class ProductManager {
//
//
//    private Map<Product, List<Review>> products = new HashMap<>();
//
//    private final ResourceBundle config = ResourceBundle.getBundle("labs.pm.data.config");
//    private final MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
//    private final MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
//    private final Path reportsFolder = Path.of(config.getString("reports.folder"));
//    private final Path dataFolder = Path.of(config.getString("data.folder"));
//    private final Path tempFolder = Path.of(config.getString("temp.folder"));
//    //private ResourceFormatter formatter;
//    private static final Map<String, ResourceFormatter> formatters
//            = Map.of("en-GB", new ResourceFormatter(Locale.UK),
//            "en-US", new ResourceFormatter(Locale.US),
//            "fr-FR", new ResourceFormatter(Locale.FRANCE),
//            "ru-RU", new ResourceFormatter(new Locale("ru", "RU")),
//            "zh-CN", new ResourceFormatter(Locale.CHINA)
//    );
//    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());
//    private static final ProductManager pm = new ProductManager();
//    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
//    private final Lock writeLock = lock.writeLock();
//    private final Lock readLock = lock.readLock();
//
//    public static ProductManager getInstance() {
//        return pm;
//    }
//
////    public ProductManager(Locale locale) {
////        this(locale.toLanguageTag());
////    }
//
//    private ProductManager() {
//        //changeLocal(languageTag);
//        loadAllData();
//    }
//
//
////    public void changeLocal(String languageTag) {
////        formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
////    }
//
//    public static Set<String> getSupportedLocale() {
//        return formatters.keySet();
//    }
//
//    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
//        Product product = null;
//        try {
//            writeLock.lock();
//            product = new Food(id, name, price, rating, bestBefore);
//            products.putIfAbsent(product, new ArrayList<>());
//        } catch (Exception ex) {
//            logger.log(Level.INFO, "Error adding product " + ex.getMessage());
//            return null;
//        } finally {
//            writeLock.unlock();
//        }
//        return product;
//    }
//
//    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
//        Product product = null;
//        try {
//            writeLock.lock();
//            product = new Drink(id, name, price, rating);
//            products.putIfAbsent(product, new ArrayList<>());
//        } catch (Exception ex) {
//            logger.log(Level.INFO, "Error adding product " + ex.getMessage());
//            return null;
//        } finally {
//            writeLock.unlock();
//        }
//
//        return product;
//    }
//
////    public Product findProduct(int id) {
////        Product result = null;
////        for (Product product : products.keySet()) {
////            if (product.getId() == id) {
////                result = product;
////                break;
////            }
////        }
////        return result;
////    }
//
//    public Product findProduct(int id) throws ProductManagerException {
//        try {
//            readLock.lock();
//            return products.keySet().stream().filter(p -> p.getId() == id).findFirst()
//                    .orElseThrow(() -> new ProductManagerException("Product with id " + id + " not found"));
//        } finally {
//            readLock.unlock();
//        }
//
//    }
//
//    public Product reviewProduct(int id, Rating rating, String comments) {
//        try {
//            writeLock.lock();
//            return reviewProduct(findProduct(id), rating, comments);
//        } catch (ProductManagerException ex) {
//            //e.printStackTrace();
//            logger.log(Level.INFO, ex.getMessage());
//            return null;
//        } finally {
//            writeLock.unlock();
//        }
//
//    }
//
//    private Product reviewProduct(Product product, Rating rating, String comments) {
//        List<Review> reviews = products.get(product);
//        products.remove(product);
//        reviews.add(new Review(rating, comments));
//        product = product.applyRating(Rateble.convert((int) Math.round(reviews.stream().mapToInt(r -> r.getRating().ordinal()).average().orElse(0))));
//        products.put(product, reviews);
//        return product;
//    }
//
//    public void printProductReport(int id, String languageTag, String client) {
//        try {
//            readLock.lock();
//            printProductReport(findProduct(id), languageTag, client);
//        } catch (ProductManagerException ex) {
//            logger.log(Level.INFO, ex.getMessage());
//        } catch (IOException ex) {
//            logger.log(Level.SEVERE, "Error printing product report " + ex.getMessage());
//        } finally {
//            readLock.unlock();
//        }
//    }
//
//    private void printProductReport(Product product, String languageTag, String client) throws IOException {
//        ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
//        List<Review> reviews = products.get(product);
//        Collections.sort(reviews);
//        Path productReportFile = reportsFolder.resolve(MessageFormat.format(config.getString("report.file"), product.getId(), client));
//        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(productReportFile, StandardOpenOption.CREATE), "UTF-8"));) {
//            out.append(formatter.formatProduct(product) + System.lineSeparator());
//            if (reviews.isEmpty()) {
//                out.append(formatter.getText("no.reviews") + System.lineSeparator());
//            } else {
//                out.append(reviews.stream().map(r -> formatter.formatReview(r) + System.lineSeparator()).collect(Collectors.joining()));
//            }
//        }
//
//    }
//
//    private void dumpData() {
//        try {
//            if (Files.notExists(tempFolder)) {
//                Files.createDirectory(tempFolder);
//            }
//            Path tempFile = tempFolder.resolve(Path.of(MessageFormat.format(config.getString("temp.file"), Instant.now().toString().replace(":", "_"))));
//            //Path tempFilePath = tempFolder.resolve(tempFile);
//            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(tempFile, StandardOpenOption.CREATE))) {
//                out.writeObject(products);
//                //products = new HashMap<>();
//            }
//        } catch (IOException ex) {
//            logger.log(Level.SEVERE, "Error dumping data " + ex.getMessage(), ex);
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    private void restoreData() {
//        try {
//            Path tempFile = Files.list(tempFolder).filter(path -> path.getFileName().toString().endsWith("tmp")).findAny().orElseThrow();
//            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(tempFile, StandardOpenOption.DELETE_ON_CLOSE))) {
//                products = (HashMap) in.readObject();
//            }
//        } catch (Exception ex) {
//            logger.log(Level.SEVERE, "Error restoring data " + ex.getMessage(), ex);
//        }
//    }
//
//    private void loadAllData() {
//        try {
//            products = Files.list(dataFolder).filter(file -> file.getFileName().toString().startsWith("product"))
//                    .map(file -> loadProduct(file))
//                    .filter(product -> product != null)
//                    .collect(Collectors.toMap(product -> product,
//                            product -> loadReviews(product)
//                    ));
//        } catch (IOException ex) {
//            logger.log(Level.SEVERE, "Error loading data " + ex.getMessage());
//        }
//    }
//
//    private Product loadProduct(Path file) {
//        Product product = null;
//        try {
//            product = parseProduct(Files.lines(dataFolder.resolve(file), Charset.forName("UTF-8")).findFirst().orElseThrow());
//        } catch (Exception ex) {
//            logger.log(Level.WARNING, "Error loading product " + ex.getMessage());
//        }
//
//        return product;
//    }
//
//    private List<Review> loadReviews(Product product) {
//        List<Review> reviews = null;
//        Path file = dataFolder.resolve(MessageFormat.format(config.getString("reviews.data.file"), product.getId()));
//        if (Files.notExists(file)) {
//            reviews = new ArrayList<>();
//        } else {
//            try {
//                reviews = Files.lines(file, Charset.forName("UTF-8")).map(text -> parseReview(text)).filter(review -> review != null).collect(Collectors.toList());
//            } catch (IOException ex) {
//                logger.log(Level.WARNING, "Error loading reviews " + ex.getMessage());
//            }
//        }
//        return reviews;
//
//    }
//
//    private Review parseReview(String text) {
//        Review review = null;
//        try {
//            Object[] values = reviewFormat.parse(text);
//            //reviewProduct(Integer.parseInt((String) values[0]), Rateble.convert(Integer.parseInt((String) values[1])), (String) values[2]);
//            review = new Review(Rateble.convert(Integer.parseInt((String) values[0])), (String) values[1]);
//        } catch (ParseException | NumberFormatException ex) {
//            logger.log(Level.WARNING, "Error parsing review " + text + " " + ex.getMessage());
//        }
//        return review;
//    }
//
//    private Product parseProduct(String text) {
//        Product product = null;
//        try {
//            Object[] values = productFormat.parse(text);
//            String productType = (String) values[0];
//            int id = Integer.parseInt((String) values[1]);
//            String name = (String) values[2];
//            BigDecimal price = BigDecimal.valueOf(Double.valueOf((String) values[3]));
//            Rating rating = Rateble.convert(Integer.parseInt((String) values[4]));
//
//            switch (productType) {
//                case "D":
//                    product = new Drink(id, name, price, rating);
//                    break;
//                case "F":
//                    LocalDate bestBefore = LocalDate.parse(((String) values[5]));
//                    product = new Food(id, name, price, rating, bestBefore);
//                    break;
//            }
//
//        } catch (ParseException | NumberFormatException | DateTimeParseException ex) {
//            logger.log(Level.WARNING, "Error parsing product " + text + " " + ex.getMessage());
//        }
//        return product;
//    }
//
//    public void printProducts(Predicate<Product> filter, Comparator<Product> sorter, String languageTag) {
//        try {
//            readLock.lock();
//            ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
//            StringBuilder txt = new StringBuilder();
//            products.keySet().stream().sorted(sorter).filter(filter).forEach(p -> txt.append(formatter.formatProduct(p) + '\n'));
//            System.out.println(txt);
//        } finally {
//            readLock.unlock();
//        }
//    }
//
//    public Map<String, String> getDiscount(String languageTag) {
//        try {
//            readLock.lock();
//            ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-GB"));
//            return products.keySet()
//                    .stream()
//                    .collect(
//                            Collectors.groupingBy(
//                                    product -> product.getRating().getStars(),
//                                    Collectors.collectingAndThen(
//                                            Collectors.summarizingDouble(
//                                                    product -> product.getDiscount().doubleValue()),
//                                            discount -> formatter.moneyFormat.format(discount.getSum())
//
//                                    )
//                            ));
//
//        }finally {
//            readLock.unlock();
//        }
//    }
//
//
//}
