package labs.pm.data;

import java.io.Serializable;

public class Review implements Comparable<Review> , Serializable {
    private Rating rating;
    private String comments;

    public Review(Rating rating, String comments) {
        this.rating = rating;
        this.comments = comments;
    }

    public Rating getRating() {
        return rating;
    }

    public String getComments() {
        return comments;
    }

    @Override
    public String toString() {
        return "Review{" + "rating=" + rating + ", comments=" + comments + "}";

    }

    @Override
    public int compareTo(Review other) {
        //int compare = this.rating.ordinal() - other.rating.ordinal();
        //System.out.println(ProductManager.reviewsStatic);
        //return compare;
        return other.rating.ordinal() - this.rating.ordinal();
    }
}
