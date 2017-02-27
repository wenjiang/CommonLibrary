package database;

/**
 * Created by weber_zheng on 17/1/25.
 */

public class NoColumnException extends BaseSQLiteException {
    public NoColumnException(String message) {
        super(message);
    }
}
