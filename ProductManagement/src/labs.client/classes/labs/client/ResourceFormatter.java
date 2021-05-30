package labs.client;

import labs.pm.data.Product;
import labs.pm.data.Review;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceFormatter {
    private Locale locale;
    private ResourceBundle resources;
    private DateTimeFormatter dateFormat;
    private NumberFormat moneyFormat;

    public static ResourceFormatter getResourceFormatter(String locale){
        String[] localeParts = locale.split("-");
        return new ResourceFormatter(new Locale(localeParts[0], localeParts[1]));
    }

    private ResourceFormatter(Locale locale) {
        this.locale = locale;
        resources = ResourceBundle.getBundle("labs.client.resources", locale);
        dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).localizedBy(locale);
        moneyFormat = NumberFormat.getCurrencyInstance(locale);
    }

    public String formatProduct(Product product) {
        return MessageFormat.format(resources.getString("product"),
                product.getName(),
                moneyFormat.format(product.getPrice()),
                product.getRating().getStars(),
                dateFormat.format(product.getBestBefore()));
    }

    public String formatReview(Review review) {
        return MessageFormat.format(resources.getString("review"),
                review.getRating().getStars(),
                review.getComments());
    }

    public String formatProductReport(Product product, List<Review> reviews){
        StringBuilder sb = new StringBuilder();
        sb.append(formatProduct(product));
        sb.append('\n');
        reviews.stream().forEach((review -> sb.append(review)));
        return sb.toString();
    }

    public String formatData(){
        return "";
    }




}
