package database;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by weber_zheng on 17/2/5.
 */

public class ColumnValueMap {
    private Map<String, Object> map;

    public ColumnValueMap(){
        map = new HashMap<>();
    }

    public void put(String column, Object value){
        map.put(column, value);
    }

    public Object get(String column){
        return map.get(column);
    }

    public Set keySet(){
        return map.keySet();
    }

    public int size(){
        return map.size();
    }
}
