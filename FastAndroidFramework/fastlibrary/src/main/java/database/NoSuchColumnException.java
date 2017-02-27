package database;

/**
 * 如果没有该列就会报出该错误
 * Created by pc on 2015/3/11.
 */
public class NoSuchColumnException extends Exception {
    public NoSuchColumnException(String message) {
        super(message);
    }
}
