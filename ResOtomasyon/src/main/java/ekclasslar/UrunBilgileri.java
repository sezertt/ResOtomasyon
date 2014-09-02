package ekclasslar;

import java.util.ArrayList;
import java.util.List;

public class UrunBilgileri {

    public String productGroupName;

    public final List<String> productName = new ArrayList<String>();
    public final List<String> productPrice = new ArrayList<String>();
    public final List<String> productInfo = new ArrayList<String>();
    public final List<String> productCount = new ArrayList<String>();
    public final List<Double> productPortionClass = new ArrayList<Double>();

    public UrunBilgileri(String productGroupName) {
        this.productGroupName = productGroupName;
    }
}