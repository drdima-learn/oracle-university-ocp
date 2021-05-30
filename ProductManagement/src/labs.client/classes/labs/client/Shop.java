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
package labs.client;

//import labs.file.service.ProductFileManager;
import labs.pm.data.Product;
import labs.pm.data.Rating;
import labs.pm.data.Review;
import labs.pm.service.ProductManagerException;
import labs.pm.service.ProductManagerInterface;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Shop {

    private static final Logger logger = Logger.getLogger(Shop.class.getName());

    public static void main(String[] args) {

        try {
            ResourceFormatter formatter = ResourceFormatter.getResourceFormatter("en-GB");
            ServiceLoader<ProductManagerInterface> serviceLoader = ServiceLoader.load(ProductManagerInterface.class);
            serviceLoader.stream().forEach(x->System.out.println(x));

            //ProductManagerInterface pm = new ProductFileManager();
            ProductManagerInterface pm = serviceLoader.findFirst().get();
            pm.createProduct(164, "Kombucha", BigDecimal.valueOf(1.99), Rating.NOT_RATED);
            pm.reviewProduct(164, Rating.TWO_STAR, "Look like tea but is it ?");
            pm.reviewProduct(164, Rating.FOUR_STAR, "Fine tea");
            pm.reviewProduct(164, Rating.FOUR_STAR, "This is not tea");
            pm.reviewProduct(164, Rating.FIVE_STAR, "Perfect");
            pm.findProducts(p->p.getPrice().doubleValue()<2).stream().forEach(product -> System.out.println(formatter.formatProduct(product)));
            Product product = pm.findProduct(101);
            List<Review> reviews = pm.findReviews(101);
            System.out.println(formatter.formatProduct(product));
            reviews.forEach(review-> System.out.println(formatter.formatReview(review)));
            //printFile(formatter.formatProductReport(product, reviews), Path.of(formatter.for)
        } catch (ProductManagerException ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
        }

    }

    private static void printFile(String content, Path file){

    }

}
