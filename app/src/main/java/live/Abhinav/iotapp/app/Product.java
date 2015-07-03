package live.Abhinav.iotapp.app;

/**
 * Created by Abhinav on 6/17/2015.
 */
public class Product {

    private boolean isChecked;
    private String pSNo;
    private String pName;


    public Product() {

    }

    public Product(boolean isChecked, String pSNo, String pName) {
        this.isChecked = isChecked;
        this.pSNo = pSNo;
        this.pName = pName;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getpSNo() {
        return pSNo;
    }

    public void setpSNo(String pSNo) {
        this.pSNo = pSNo;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    @Override
    public String toString() {
        return "Sno:" + pSNo +
                " Name " + pName +" isChecked "+isChecked;
    }
}