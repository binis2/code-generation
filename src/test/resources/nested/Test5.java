package code.test;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.type.GenerationStrategy;

import java.util.List;

@CodePrototype(strategy = GenerationStrategy.NONE)
public class PricesTask {

    @CodePrototype
    public interface PriceResponsePrototype {
        List<MainDataPrototype> mainData();
        List<PriceDataPrototype> priceData();
        SummaryPrototype summary();
        String date();
        boolean isHourlyData();
        String language();
        String currency();
        double currencyMultiplier();
    }

    @CodePrototype
    public interface MainDataPrototype {
        String product();
        String deliveryPeriod();
        String price();
        String volume();
    }

    @CodePrototype
    public interface PriceDataPrototype {
        String product();
        String priceIndex();
    }

    @CodePrototype
    public interface SummaryPrototype {
        String base();
        String peak();
        String offPeak();
        String totalVolume();
    }

}
