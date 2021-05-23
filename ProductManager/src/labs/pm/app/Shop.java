/*
 * Copyright (C) 2021 admin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package labs.pm.app;

import labs.pm.data.Product;
import labs.pm.data.ProductManager;
import labs.pm.data.Rating;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@code Shop} class represents an application that manages Products
 * version 4.0
 *
 * @author admin
 */
public class Shop {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        ProductManager pm = ProductManager.getInstance();
        AtomicInteger clientCount = new AtomicInteger(0);
        Callable<String> client = () -> {
            String clientId = "Client" + clientCount.incrementAndGet();
            String threadName = Thread.currentThread().getName();
            int productId = ThreadLocalRandom.current().nextInt(2)+101;
            String languageTag = ProductManager.getSupportedLocale()
                    .stream()
                    .skip(ThreadLocalRandom.current().nextInt(4))
                    .findFirst().get();
            StringBuilder log = new StringBuilder();
            log.append(clientId+" "+threadName+"\n-\tstart of log\t-\n");
            log.append(pm.getDiscount(languageTag).entrySet().stream()
                    .map(entry->entry.getKey()+"\t"+entry.getValue())
                    .collect(Collectors.joining("\n"))
            );
            Product product = pm.reviewProduct(productId, Rating.FOUR_STAR, "Yet another review");
            log.append((product!=null) ? "\nProduct " + productId+"reviewed\n" : "\nProduct " + productId + "not reviewed");
            pm.printProductReport(productId, languageTag, clientId);
            log.append(clientId + " generated report for" + productId + " product");
            log.append("\n-\tend of log\t-\n");

            return log.toString();
        };

        List<Callable<String>> clients = Stream.generate(()->client).limit(5).collect(Collectors.toList());
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        try {
            List<Future<String>> results = executorService.invokeAll(clients);
            executorService.shutdown();
            results.stream().forEach(result->{
                try {
                    System.out.println(result.get());
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Error retrieving client log", ex);
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Error invoking clients", ex);
        }



//
//        pm.createProduct(164, "Kombucha", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
//        pm.reviewProduct(164, Rating.TWO_STAR, "Looks like tea but is it ?");
//        pm.reviewProduct(164, Rating.FOUR_STAR, "Fine tea");
//        pm.reviewProduct(164, Rating.FOUR_STAR, "This is not tea");
//        pm.reviewProduct(164, Rating.FIVE_STAR, "Perfect!");
//        pm.printProductReport(164);

//        pm.dumpData();
//        pm.restoreData();


//        pm.printProducts(p->p.getPrice().floatValue()<2, (p1,p2)->p2.getRating().ordinal()-p1.getRating().ordinal());
//        pm.getDiscount().forEach( (rating, discount)-> System.out.println(rating+"\t"+discount));
//
//






        //ProductManager pm = new ProductManager(Locale.UK);
        //ProductManager pm = new ProductManager("ru-RU");

        //Product p1 = pm.createProduct(101, "Tea", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
        //pm.parseProduct("D,101,Tea,1.99,0,2019-09-19");
        //pm.parseProduct("D,101,Tea,1.99,0,2019-09-19-49");
        //pm.printProductReport(101);

//        pm.reviewProduct(101, Rating.FOUR_STAR, "Nice hot cup of tea");
//        pm.reviewProduct(101, Rating.TWO_STAR, "Rather Weak tea");
//        pm.reviewProduct(101, Rating.FOUR_STAR, "Fine tea");
//        pm.reviewProduct(101, Rating.FOUR_STAR, "Good tea");
//        pm.reviewProduct(101, Rating.FIVE_STAR, "Perfect tea");
//        pm.reviewProduct(101, Rating.THREE_STAR, "Just add some lemon");

//        pm.parseReview("101,4,Nice hot cup of tea");
//        pm.parseReview("101,2,Rather Weak tea");
//        pm.parseReview("101,4,Fine tea");
//        pm.parseReview("101,4,Good tea");
//        pm.parseReview("101,5,Perfect tea");
//        pm.parseReview("101,3,Just add some lemon");
        //pm.printProductReport(101);


        //pm.parseProduct("F,103,Cake,3.99,0,2019-09-19");


        //pm.changeLocal("ru-RU");

//        Product p2 = pm.createProduct(102, "Coffee", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
//        pm.reviewProduct(102, Rating.THREE_STAR, "Coffee was ok");
//        pm.reviewProduct(102, Rating.ONE_STAR, "Where is the milk?!");
//        pm.reviewProduct(102, Rating.FIVE_STAR, "It's perfect with ten spons of sugar!");
//        //pm.printProductReport(102);
//
//        //pm.changeLocal("fr-FR");
//
//        Product p3 = pm.createProduct(103, "Cake", BigDecimal.valueOf(3.99), Rating.NOT_RATED, LocalDate.now().plusDays(2));
//        pm.reviewProduct(103, Rating.FIVE_STAR, "Very nice cake");
//        pm.reviewProduct(103, Rating.FOUR_STAR, "It good, but I've expected more chocolate");
//        pm.reviewProduct(103, Rating.FIVE_STAR, "This cake is perfect!");
//        pm.printProductReport(103);
//
//        Product p4 = pm.createProduct(104, "Cookie", BigDecimal.valueOf(2.99), Rating.NOT_RATED, LocalDate.now());
//        pm.reviewProduct(104, Rating.THREE_STAR, "Just another cookie!");
//        pm.reviewProduct(104, Rating.THREE_STAR, "Ok");
//        //pm.printProductReport(104);
//
//        Product p5 = pm.createProduct(105, "Hot Chocolate", BigDecimal.valueOf(2.50), Rating.NOT_RATED);
//        pm.reviewProduct(105, Rating.FOUR_STAR, "Tasty!");
//        pm.reviewProduct(105, Rating.FOUR_STAR, "Not bad at all");
//        //pm.printProductReport(105);
//
//        Product p6 = pm.createProduct(106, "Chocolate", BigDecimal.valueOf(2.50), Rating.NOT_RATED, LocalDate.now().plusDays(3));
//        pm.reviewProduct(106, Rating.TWO_STAR, "Too sweet");
//        pm.reviewProduct(106, Rating.THREE_STAR, "Better then cookie");
//        pm.reviewProduct(106, Rating.TWO_STAR, "Too bitter");
//        pm.reviewProduct(106, Rating.ONE_STAR, "I don't get it!");
//        pm.printProductReport(106);
//
//
//
//
//
//        System.out.println("-------------");
//        pm.printProducts(p ->p.getPrice().floatValue()<2,
//                (pr1,pr2)->pr2.getRating().ordinal()-pr1.getRating().ordinal());
//        System.out.println("------end -------");
//
//        Comparator<Product> ratingSorter = (o1,o2) -> o1.getRating().ordinal() - o2.getRating().ordinal();
//        pm.printProducts((p -> true), ratingSorter );
//
//        Comparator<Product> priceSorter = (o1,o2) -> o1.getPrice().compareTo(o2.getPrice());
//        pm.printProducts((p -> true), priceSorter );
//
//        pm.printProducts((p -> true), ratingSorter.thenComparing(priceSorter).reversed());
//
//        pm.getDiscount().forEach( (rating, discount)-> System.out.println(rating +"\t" + discount) );

    }

}
