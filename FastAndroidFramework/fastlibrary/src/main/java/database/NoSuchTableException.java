package database;

/**
 * 如果该表就会报出该错误
 * Created by pc on 2015/3/11.
 */
public class NoSuchTableException extends Exception {
    public NoSuchTableException(String message) {
        super(message);
    }
}
