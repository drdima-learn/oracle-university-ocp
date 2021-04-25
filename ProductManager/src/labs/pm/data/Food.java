package labs.pm.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.StringJoiner;

public class Food extends Product{
    private LocalDate bestBefore;

    public Food(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        super(id, name, price, rating);
        this.bestBefore = bestBefore;
    }

    /**
     * Get the value of the best before date for the product
     * @return the value of bestBefore
     */
    public LocalDate getBestBefore() {
        return bestBefore;
    }

    @Override
    public BigDecimal getDiscount() {
        return (bestBefore.isEqual(LocalDate.now())) ? super.getDiscount(): BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return super.toString() + ", " + bestBefore;
    }
}
