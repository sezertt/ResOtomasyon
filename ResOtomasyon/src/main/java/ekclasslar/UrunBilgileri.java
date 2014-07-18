package ekclasslar;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by sezer on 08.07.2014.
 */
public class UrunBilgileri {

    public String productGroupName;

    public final List<String> productName = new ArrayList<String>();
    public final List<String> productPrice = new ArrayList<String>();
    public final List<String> productInfo = new ArrayList<String>();

    public UrunBilgileri(String productGroupName) {
        this.productGroupName = productGroupName;
    }
}