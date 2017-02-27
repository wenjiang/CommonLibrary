package database;

/**
 * 数据库异常的基类
 * Created by pc on 2015/3/11.
 */
public class BaseSQLiteException extends Exception {
    public BaseSQLiteException(String message) {
        super(message);
    }
}