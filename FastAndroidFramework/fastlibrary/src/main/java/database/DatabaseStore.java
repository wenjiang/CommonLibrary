package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import log.Logger;

/**
 * 数据库操作
 * Created by pc on 2015/2/9.
 */
public class DatabaseStore {
    private String whereStr = "";
    private String orderStr = "";
    private BaseSQLiteOpenHelper openHelper;
    private SQLiteDatabase db;
    private Map<String, String> columnMap;
    private String whereMultiStr = " where ";
    private Context context;

    private DatabaseStore() {
        columnMap = new HashMap<>();
        if (openHelper == null) {
            openHelper = BaseSQLiteOpenHelper.getInstance(context);
            db = openHelper.getWritableDatabase();
        }
    }

    /**
     * 单例方法
     *
     * @return DatabaseStore
     */
    public static DatabaseStore getInstance() {
        return new DatabaseStore();
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        BaseSQLiteOpenHelper.getInstance(context);
        this.context = context;
    }

    /**
     * 查询所有数据
     *
     * @param clazz
     * @param <T>
     * @return
     * @throws BaseSQLiteException
     */
    public <T> List<T> findAll(Class<T> clazz) throws BaseSQLiteException {
        String table = getTableNameFromClass(clazz);
        if (table.equals("")) {
            throw new BaseSQLiteException("Please check model at the first");
        }
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<>();
        Map<String, String> types = new HashMap<>();
        for (Field field : fields) {
            fieldNames.add(field.getName());
            if (field.isAnnotationPresent(Column.class)) {
                Column meta = field.getAnnotation(Column.class);
                String column = meta.column();
                if (!column.equals("")) {
                    columnMap.put(field.getName(), column);
                }
            }
            if (field.isAnnotationPresent(ColumnType.class)) {
                ColumnType fieldType = field.getAnnotation(ColumnType.class);
                if (field.isAnnotationPresent(Column.class)) {
                    Column meta = field.getAnnotation(Column.class);
                    String column = meta.column();
                    if (column.equals("")) {
                        column = field.getName();
                    } else {
                        columnMap.put(field.getName(), column);
                    }

                    types.put(column, fieldType.ColumnType());
                }
            }
        }

        List<Method> setMethods = getSetMethods(clazz);
        Cursor cursor = getDatabase().query(table, null, null, null, null, null, null);
        List<T> list = getList(clazz, cursor, setMethods, fieldNames, fields, types);
        cursor.close();
        db.close();
        return list;
    }

    private <T> String getTableNameFromClass(Class<T> clazz) {
        String table = "";
        if (clazz.isAnnotationPresent(Table.class)) {
            table = clazz.getAnnotation(Table.class).table();
        } else {
            table = clazz.getSimpleName();
        }
        return table;
    }

    /**
     * 查询最佳的构造器
     *
     * @param modelClass
     * @return
     */
    private Constructor<?> findBestSuitConstructor(Class<?> modelClass) {
        Constructor<?> finalConstructor = null;
        Constructor<?>[] constructors = modelClass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if (finalConstructor == null) {
                finalConstructor = constructor;
            } else {
                int finalParamLength = finalConstructor.getParameterTypes().length;
                int newParamLength = constructor.getParameterTypes().length;
                if (newParamLength < finalParamLength) {
                    finalConstructor = constructor;
                }
            }
        }
        finalConstructor.setAccessible(true);
        return finalConstructor;
    }

    /**
     * where条件
     *
     * @param column
     * @param value  ֵ
     * @return DatabaseStore
     */
    public synchronized DatabaseStore where(String column, Object value) {
        whereStr = " where " + column + " like '%" + value + "%'";
        return this;
    }

    /**
     * 多个where条件的查询
     *
     * @return
     */
    public synchronized DatabaseStore whereMulti(ColumnValueMap map) throws BaseSQLiteException {
        whereMultiStr = " where ";
        int size = map.size();
        Iterator keys = map.keySet().iterator();
        if(size == 0){
            throw new BaseSQLiteException("There is not column and value");
        }
        for (int i = 0; i < size; i++) {
            String key = (String)keys.next();
            if (i == size - 1) {
                whereMultiStr += key + " like '%" + map.get(key) + "%'";
            } else {
                whereMultiStr += key + " like '%" + map.get(key) + "%'" + " and ";
            }
        }

        Logger.e(whereMultiStr);
        return this;
    }

    /**
     * 查询某个表的所有数据
     *
     * @param clazz
     * @param <T>
     * @return
     * @throws BaseSQLiteException
     */
    public synchronized <T> List<T> find(Class<T> clazz) throws BaseSQLiteException {
        String table = getTableNameFromClass(clazz);
        if (table.equals("")) {
            throw new BaseSQLiteException("Please check model at the first");
        }

        String sql = "select * from " + table + (orderStr.equals("") ? "" : orderStr);
        if (!whereStr.equals("")) {
            sql = "select * from " + table + whereStr + (orderStr.equals("") ? "" : orderStr);
        }

        Cursor cursor = getDatabase().rawQuery(sql, null);
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<>();
        Map<String, String> types = new HashMap<>();
        for (Field field : fields) {
            fieldNames.add(field.getName());
            if (field.isAnnotationPresent(Column.class)) {
                Column meta = field.getAnnotation(Column.class);
                String column = meta.column();
                if (!column.equals("")) {
                    columnMap.put(field.getName(), column);
                }
            }
            if (field.isAnnotationPresent(ColumnType.class)) {
                ColumnType fieldType = field.getAnnotation(ColumnType.class);
                if (field.isAnnotationPresent(Column.class)) {
                    Column meta = field.getAnnotation(Column.class);
                    String column = meta.column();
                    if (column.equals("")) {
                        column = field.getName();
                    } else {
                        columnMap.put(field.getName(), column);
                    }

                    types.put(column, fieldType.ColumnType());
                }
            }
        }

        List<Method> setMethods = getSetMethods(clazz);
        List<T> list = getList(clazz, cursor, setMethods, fieldNames, fields, types);
        cursor.close();
        db.close();
        whereStr = "";
        orderStr = "";
        return list;
    }

    /**
     * 查询某个表的所有数据
     *
     * @param clazz
     * @param <T>
     * @return
     * @throws BaseSQLiteException
     */
    public synchronized <T> List<T> findMulti(Class<T> clazz) throws BaseSQLiteException {
        String table = getTableNameFromClass(clazz);
        if (table.equals("")) {
            throw new BaseSQLiteException("Please check model at the first");
        }

        String sql = "select * from " + table + (orderStr.equals("") ? "" : orderStr);

        if (!whereMultiStr.equals(" where ")) {
            sql = "select * from " + table + whereMultiStr + (orderStr.equals("") ? "" : orderStr);
        }

        Logger.e(sql);
        Cursor cursor = getDatabase().rawQuery(sql, null);
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<>();
        Map<String, String> types = new HashMap<>();
        for (Field field : fields) {
            fieldNames.add(field.getName());
            if (field.isAnnotationPresent(Column.class)) {
                Column meta = field.getAnnotation(Column.class);
                String column = meta.column();
                if (!column.equals("")) {
                    columnMap.put(field.getName(), column);
                }
            }
            if (field.isAnnotationPresent(ColumnType.class)) {
                ColumnType fieldType = field.getAnnotation(ColumnType.class);
                if (field.isAnnotationPresent(Column.class)) {
                    Column meta = field.getAnnotation(Column.class);
                    String column = meta.column();
                    if (column.equals("")) {
                        column = field.getName();
                    } else {
                        columnMap.put(field.getName(), column);
                    }

                    types.put(column, fieldType.ColumnType());
                }
            }
        }

        List<Method> setMethods = getSetMethods(clazz);
        List<T> list = getList(clazz, cursor, setMethods, fieldNames, fields, types);
        cursor.close();
        db.close();
        orderStr = "";
        whereMultiStr = " where ";
        return list;
    }

    /**
     * 获取符合某个条件的所有数据
     *
     * @param clazz
     * @param cursor
     * @param methods
     * @param fieldNames
     * @param fields
     * @param types
     * @param <T>
     * @return
     */
    private <T> List<T> getList(Class<T> clazz, Cursor cursor, List<Method> methods, List<String> fieldNames, Field[] fields, Map<String, String> types) {
        List<T> list = new ArrayList<>();
        Constructor<?> constructor = findBestSuitConstructor(clazz);
        Set<String> keySet = types.keySet();
        while (cursor.moveToNext()) {
            try {
                T data = (T) constructor
                        .newInstance();
                for (Method method : methods) {
                    String name = method.getName();
                    String valueName = name.substring(3).substring(0, 1).toLowerCase() + name.substring(4);
                    String type = null;
                    String fieldType = null;
                    int index = 0;
                    if (fieldNames.contains(valueName)) {
                        index = fieldNames.indexOf(valueName);
                        type = fields[index].getGenericType().toString();
                        if (keySet.contains(valueName)) {
                            fieldType = types.get(valueName);
                        }

                        if (columnMap.containsKey(valueName)) {
                            valueName = columnMap.get(valueName);
                        }
                    }
                    Object value = getColumnValue(cursor, valueName, type, fieldType);
                    if (value != null) {
                        fields[index].setAccessible(true);
                        fields[index].set(data, value);
                    }
                }

                list.add(data);
            } catch (InstantiationException e) {
                Logger.e(e.toString());
            } catch (IllegalAccessException e) {
                Logger.e(e.toString());
            } catch (InvocationTargetException e) {
                Logger.e(e.toString());
            } catch (JSONException e) {
                Logger.e(e.toString());
            }
        }
        return list;
    }

    /**
     * 获取set方法
     *
     * @param clazz
     * @return
     */
    private List<Method> getSetMethods(Class clazz) {
        Method[] allMethods = clazz.getMethods();
        List<Method> setMethods = new ArrayList<>();
        for (Method method : allMethods) {
            String name = method.getName();

            if (name.contains("set") && !name.equals("offset")) {
                setMethods.add(method);
            }
        }

        return setMethods;
    }

    /**
     * order排序
     *
     * @param column
     * @return DatabaseStore
     */
    public synchronized DatabaseStore order(String column, boolean isAscending) {
        if (isAscending) {
            orderStr = " order by " + column + " asc";
            return this;
        }

        orderStr = " order by " + column + " desc";
        return this;
    }

    /**
     * 获取数量
     *
     * @return
     * @throws BaseSQLiteException
     */
    public synchronized <T> int count(Class<T> clazz) throws BaseSQLiteException {
        String table = getTableNameFromClass(clazz);
        if (table.equals("")) {
            throw new BaseSQLiteException("Please check model at the first");
        }
        int count = 0;
        String sql = " select count(1) from " + table + (whereStr.equals("") ? "" : whereStr);
        Cursor cursor = getDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        whereStr = "";
        orderStr = "";
        whereMultiStr = " where ";
        return count;
    }

    /**
     * 获取某列平均数
     *
     * @param column
     * @return
     * @throws BaseSQLiteException
     */
    public <T> double average(String column, Class<T> clazz) throws BaseSQLiteException {
        String table = getTableNameFromClass(clazz);
        if (table.equals("")) {
            throw new BaseSQLiteException("Please check model at the first");
        }
        double average = 0.0;
        String sql = " select avg( " + column + ") from " + table;
        Cursor cursor = getDatabase().rawQuery(sql, null);
        if (cursor.moveToNext()) {
            average = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return average;
    }

    /**
     * 删除
     *
     * @param table
     * @param valueMap
     */
    public synchronized void delete(String table, Map<String, Object> valueMap) {
        String[] columnArr = valueMap.keySet().toArray(new String[]{});
        String[] valueArr = new String[columnArr.length];
        String whereStr = "";
        for (int i = 0, length = columnArr.length; i < length; i++) {
            if (i == length - 1) {
                whereStr += columnArr[i] + "=?";
            } else {
                whereStr += columnArr[i] + "=? and ";
            }

            valueArr[i] = (String) valueMap.get(columnArr[i]);
        }
        getDatabase().delete(table, whereStr, valueArr);
    }

    /**
     * 删除全部数据
     *
     * @param column
     * @param value
     * @return
     * @throws BaseSQLiteException
     */
    public <T> int deleteAll(String column, String value, Class<T> clazz) throws BaseSQLiteException {
        String table = getTableNameFromClass(clazz);
        if (table.equals("")) {
            throw new BaseSQLiteException("Please check model at the first");
        }
        int count = getDatabase().delete(table, column + "= ?", new String[]{value});
        table = "";
        return count;
    }

    public void deleteTable(String table) {
        getDatabase().delete(table, null, null);
    }

    /**
     * 更新全部数据
     *
     * @param contentValues
     * @param columns
     * @param values
     * @return
     * @throws BaseSQLiteException
     */
    public <T> void updateAll(ContentValues contentValues, String[] columns, Object[] values, Class<T> clazz) throws BaseSQLiteException {
        String table = getTableNameFromClass(clazz);
        if (table.equals("")) {
            throw new BaseSQLiteException("Please check model at the first");
        }

        StringBuilder whereClause = new StringBuilder(" Update " + table + " Set ");
        Set<String> keySet = contentValues.keySet();
        Iterator<String> it = keySet.iterator();
        int index = 0;
        while(it.hasNext()){
            String key = it.next();
            whereClause.append(key + " = ");
            Object value = contentValues.get(key);
            if(value instanceof String){
                whereClause.append("\'" + values + "\'");
            }else{
                whereClause.append(value);
            }

            if(index == keySet.size() - 1 && keySet.size() > 1){
                whereClause.append(", ");
            }
            index++;
        }
        int length = columns.length;
        whereClause.append(" Where ");
        for(int i = 0; i < length; i++){
            whereClause.append(columns[i] + " = ");
            if(values[i] instanceof String){
                whereClause.append("\'" + values[i] + "\'");
            }else{
                whereClause.append(values[i]);
            }

            if(i == 0 && length > 1){
                whereClause.append(" and ");
            }
        }
        getDatabase().execSQL(whereClause.toString());
    }

    /**
     * 更新全部数据
     *
     * @param contentValues
     * @param column
     * @param value
     * @return
     * @throws BaseSQLiteException
     */
    public <T> int updateAll(ContentValues contentValues, String column, Object value, Class<T> clazz) throws BaseSQLiteException {
        String table = getTableNameFromClass(clazz);
        if (table.equals("")) {
            throw new BaseSQLiteException("Please check model at the first");
        }

        StringBuilder whereClause = new StringBuilder();
        int count = getDatabase().update(table, contentValues, whereClause.toString(), null);
        return count;
    }

    /**
     * 保存全部数据
     *
     * @param collection
     * @param <T>
     * @throws Exception
     */
    public <T extends BaseTable> void saveAll(Collection<T> collection) throws Exception {
        getDatabase().beginTransaction();
        BaseTable[] array = collection.toArray(new BaseTable[0]);
        for (BaseTable data : array) {
            data.save();
        }
        getDatabase().setTransactionSuccessful();
        getDatabase().endTransaction();
    }

    /**
     * 查询某列的值
     *
     * @param column
     * @param valueType
     * @return
     * @throws BaseSQLiteException
     */
    public synchronized <T> Object findColumn(String column, Class<?> valueType, Class<T> clazz) throws BaseSQLiteException {
        String table = getTableNameFromClass(clazz);
        if (table.equals("")) {
            throw new BaseSQLiteException("Please check model at the first");
        }
        String sql = " select " + column + " from " + table + (whereStr.equals("") ? "" : whereStr);
        Cursor cursor = getDatabase().rawQuery(sql, null);
        Object data = null;
        try {
            data = getRightColumn(cursor, column, valueType);
        } catch (JSONException e) {
            Logger.e(e.toString());
        }
        cursor.close();
        db.close();
        whereStr = "";
        orderStr = "";
        whereMultiStr = " where ";
        return data;
    }

    /**
     * 查询某列的所有值
     *
     * @param column
     * @param valueType
     * @return
     * @throws BaseSQLiteException
     */
    public synchronized <T> List findColumns(String column, Class<?> valueType, Class<T> clazz) throws BaseSQLiteException {
        String table = getTableNameFromClass(clazz);
        if (table.equals("")) {
            throw new BaseSQLiteException("Please check model at the first");
        }
        String sql = " select " + column + " from " + table + (whereStr.equals("") ? "" : whereStr);
        Cursor cursor = getDatabase().rawQuery(sql, null);
        List list = new ArrayList();
        while (cursor.moveToNext()) {
            try {
                list.add(getColumnValue(cursor, column, null, valueType.getSimpleName()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        db.close();
        whereStr = "";
        orderStr = "";
        whereMultiStr = " where ";
        return list;
    }

    /**
     * 查询最后一列的值
     *
     * @param column
     * @param valueType
     * @return
     * @throws BaseSQLiteException
     */
    public synchronized <T> Object findLastColumn(String column, Class<?> valueType, Class<T> clazz) throws BaseSQLiteException {
        String table = getTableNameFromClass(clazz);
        if (table.equals("")) {
            throw new BaseSQLiteException("Please check model at the first");
        }
        String sql = " select " + column + " from " + table + " " + (whereStr.equals("") ? "" : whereStr) + " order by id desc limit 0,1";
        Cursor cursor = getDatabase().rawQuery(sql, null);
        Object data = null;
        try {
            data = getRightColumn(cursor, column, valueType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cursor.close();
        db.close();
        whereStr = "";
        orderStr = "";
        whereMultiStr = " where ";
        return data;
    }

    /**
     * 查询第一列的值
     *
     * @param column
     * @param valueType
     * @return
     * @throws BaseSQLiteException
     */
    public synchronized <T> Object findFirstColumn(String column, Class<?> valueType, Class<T> clazz) throws BaseSQLiteException {
        String table = getTableNameFromClass(clazz);
        if (table.equals("")) {
            throw new BaseSQLiteException("Please check model at the first");
        }
        String sql = " select " + column + " from " + table + " " + (whereStr.equals("") ? "" : whereStr) + " order by id limit 0,1";
        Cursor cursor = getDatabase().rawQuery(sql, null);
        Object data = null;
        try {
            data = getRightColumn(cursor, column, valueType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cursor.close();
        db.close();
        whereStr = "";
        orderStr = "";
        whereMultiStr = " where ";
        return data;
    }

    /**
     * 查询第一列的值
     *
     * @param <T>
     * @return
     * @throws BaseSQLiteException
     */
    public synchronized <T> Object findFirst(Class<T> tableClazz) throws BaseSQLiteException {
        String table = getTableNameFromClass(tableClazz);
        if (table.equals("")) {
            throw new BaseSQLiteException("Please check model at the first");
        }
        String sql = "";
        if (!whereStr.equals("")) {
            sql = "select * from " + table + whereStr + " order by id limit 0,1";
        }

        Cursor cursor = getDatabase().rawQuery(sql, null);
        Field[] fields = tableClazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<>();
        Map<String, String> typeMap = new HashMap<>();
        for (Field field : fields) {
            fieldNames.add(field.getName());
            if (field.isAnnotationPresent(Column.class)) {
                Column meta = field.getAnnotation(Column.class);
                String column = meta.column();
                if (!column.equals("")) {
                    columnMap.put(field.getName(), column);
                }
            }
            if (field.isAnnotationPresent(ColumnType.class)) {
                ColumnType fieldType = field.getAnnotation(ColumnType.class);
                if (field.isAnnotationPresent(Column.class)) {
                    Column meta = field.getAnnotation(Column.class);
                    String column = meta.column();
                    if (column.equals("")) {
                        column = field.getName();
                    } else {
                        columnMap.put(field.getName(), column);
                    }

                    typeMap.put(column, fieldType.ColumnType());
                }
            }
        }

        List<Method> setMethods = getSetMethods(tableClazz);
        List<T> list = getList(tableClazz, cursor, setMethods, fieldNames, fields, typeMap);
        cursor.close();
        db.close();
        whereStr = "";
        orderStr = "";
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * 查询第一列的值
     *
     * @param clazz
     * @param <T>
     * @return
     * @throws BaseSQLiteException
     */
    public <T> Object findFirstMulti(Class<T> clazz, Class<T> tableClazz) throws BaseSQLiteException {
        String table = getTableNameFromClass(tableClazz);
        if (table.equals("")) {
            throw new BaseSQLiteException("Please check model at the first");
        }
        String sql = "";
        if (!whereMultiStr.equals(" where ")) {
            sql = "select * from " + table + whereMultiStr + " order by id limit 0,1";
        }

        Cursor cursor = getDatabase().rawQuery(sql, null);
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<>();
        Map<String, String> typeMap = new HashMap<>();
        for (Field field : fields) {
            fieldNames.add(field.getName());
            if (field.isAnnotationPresent(Column.class)) {
                Column meta = field.getAnnotation(Column.class);
                String column = meta.column();
                if (!column.equals("")) {
                    columnMap.put(field.getName(), column);
                }
            }
            if (field.isAnnotationPresent(ColumnType.class)) {
                ColumnType fieldType = field.getAnnotation(ColumnType.class);
                if (field.isAnnotationPresent(Column.class)) {
                    Column meta = field.getAnnotation(Column.class);
                    String column = meta.column();
                    if (column.equals("")) {
                        column = field.getName();
                    } else {
                        columnMap.put(field.getName(), column);
                    }

                    typeMap.put(column, fieldType.ColumnType());
                }
            }
        }

        List<Method> setMethods = getSetMethods(clazz);
        List<T> list = getList(clazz, cursor, setMethods, fieldNames, fields, typeMap);
        cursor.close();
        db.close();
        orderStr = "";
        whereMultiStr = " where ";
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * 查询最后的值
     *
     * @param clazz
     * @param <T>
     * @return
     * @throws BaseSQLiteException
     */
    public synchronized <T> Object findLast(Class<T> clazz, Class<T> tableClazz) throws BaseSQLiteException {
        String table = getTableNameFromClass(tableClazz);
        if (table.equals("")) {
            throw new BaseSQLiteException("Please check model at the first");
        }

        String sql = "";
        if (!whereStr.equals("")) {
            sql = "select * from " + table + whereStr + " order by id desc limit 0,1";
        }

        Cursor cursor = getDatabase().rawQuery(sql, null);
        Field[] fields = clazz.getDeclaredFields();
        List<String> fieldNames = new ArrayList<>();
        Map<String, String> types = new HashMap<>();
        for (Field field : fields) {
            fieldNames.add(field.getName());
            if (field.isAnnotationPresent(Column.class)) {
                Column meta = field.getAnnotation(Column.class);
                String column = meta.column();
                if (!column.equals("")) {
                    columnMap.put(field.getName(), column);
                }
            }
            if (field.isAnnotationPresent(ColumnType.class)) {
                ColumnType fieldType = field.getAnnotation(ColumnType.class);
                if (field.isAnnotationPresent(Column.class)) {
                    Column meta = field.getAnnotation(Column.class);
                    String column = meta.column();
                    if (column.equals("")) {
                        column = field.getName();
                    } else {
                        columnMap.put(field.getName(), column);
                    }

                    types.put(column, fieldType.ColumnType());
                }
            }
        }

        List<Method> setMethods = getSetMethods(clazz);
        List<T> list = getList(clazz, cursor, setMethods, fieldNames, fields, types);
        cursor.close();
        db.close();
        whereStr = "";
        orderStr = "";
        whereMultiStr = " where ";
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * 获取正确的列名
     *
     * @param cursor
     * @param column
     * @param valueType
     * @return
     * @throws JSONException
     */

    private Object getRightColumn(Cursor cursor, String column, Class<?> valueType) throws JSONException {
        Object data = null;
        if (cursor.moveToFirst()) {
            data = getColumnValue(cursor, column, null, valueType.getSimpleName());
        }

        return data;
    }

    /**
     * 获取列的值
     *
     * @param cursor
     * @param column
     * @param fieldType
     * @param dbType
     * @return
     * @throws JSONException
     */
    public Object getColumnValue(Cursor cursor, String column, String fieldType, String dbType) throws JSONException {
        Object data = null;
        try {
            if (dbType == null) {
                dbType = fieldType;
            }
            if (dbType.equals("String") || dbType.contains("String")) {
                data = cursor.getString(cursor.getColumnIndexOrThrow(column));
                if (fieldType != null && fieldType.contains("JSONObject")) {
                    data = new JSONObject((String) data);
                } else if (fieldType != null && fieldType.contains("JSONArray")) {
                    data = new JSONArray((String) data);
                }
            } else if (dbType.equals("Integer") || dbType.equals("int")) {
                data = cursor.getInt(cursor.getColumnIndex(column));
            } else if (dbType.equals("Long") || dbType.equals("long")) {
                data = cursor.getLong(cursor.getColumnIndex(column));
            } else if (dbType.equals("Double") || dbType.equals("double")) {
                data = cursor.getDouble(cursor.getColumnIndex(column));
            } else if (dbType.equals("Float") || dbType.equals("float")) {
                data = cursor.getFloat(cursor.getColumnIndex(column));
            } else if (dbType.equals("Short") || dbType.equals("short")) {
                data = cursor.getShort(cursor.getColumnIndex(column));
            }
        } catch (Exception e) {
        }
        return data;
    }

    /**
     * 保存
     *
     * @param tableName
     * @param values    ContentValues
     */
    public void save(String tableName, ContentValues values) {
        getDatabase().replace(tableName, null, values);
    }

    /**
     * 重置查询条件
     */
    public synchronized void reset() {
        whereStr = "";
        orderStr = "";
        whereMultiStr = " where ";
    }

    public void close(){
        openHelper.close();
    }

    /**
     * 清除表的所有数据
     */
    public void clear() {
        List<String> tableModelList = SharedPreferencesManager.getInstance().getListString("tablesName");
        for (String table : tableModelList) {
            deleteTable(table);
        }
    }

    public void showTableList() {
        Cursor cursor = db.rawQuery("select name from sqlite_master where type='table' order by name", null);
        while (cursor.moveToNext()) {
            //遍历出表名
            String name = cursor.getString(0);
            Logger.e(name);
        }
    }

    private SQLiteDatabase getDatabase() {
        return db;
    }
}
