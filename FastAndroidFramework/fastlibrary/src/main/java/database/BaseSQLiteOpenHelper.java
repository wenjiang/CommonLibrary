package database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import log.Logger;

/**
 * 数据库操作类
 * Created by pc on 2015/3/10.
 */
public class BaseSQLiteOpenHelper extends SQLiteOpenHelper {
    private static Set<String> tableSet;
    private static String dbName;
    private static int version;
    private static final String TABLE_MODEL = "table_model";
    private static final String TABLE_NAME = "table_name";
    private Context context;

    /**
     * BaseSQLiteOpenHelper的单例
     *
     * @param context 上下文
     * @return BaseSQLiteOpenHelper的单例
     */
    public static BaseSQLiteOpenHelper getInstance(Context context) {
        try {
            readXml(context);
        } catch (BaseSQLiteException e) {
            Logger.e(e.toString());
        }

        return new BaseSQLiteOpenHelper(context, dbName, version);
    }

    private BaseSQLiteOpenHelper(Context context, String name, int version) {
        super(context, name, null, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            createTable(db);
        } catch (BaseSQLiteException e) {
            Logger.e(e.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            readXml(context);
        } catch (BaseSQLiteException e) {
            Logger.e(e.toString());
        }
        List<String> tableModelList = SharedPreferencesManager.getInstance().getListString(TABLE_MODEL);
        List<String> tableList = new ArrayList<>();
        for (String tableEntity : tableModelList) {
            try {
                BaseTable entity = (BaseTable) (Class.forName(tableEntity).newInstance());
                String tableName = getTableName(entity);
                tableList.add(tableName);
            } catch (InstantiationException e) {
                Logger.e(e.toString());
            } catch (IllegalAccessException e) {
                Logger.e(e.toString());
            } catch (ClassNotFoundException e) {
                Logger.e(e.toString());
            } catch (NoSuchTableException e) {
                Logger.e(e.toString());
            }
        }
        if (oldVersion < newVersion) {
            db.beginTransaction();
            for (int i = 0, size = tableList.size(); i < size; i++) {
                String table = tableList.get(i);
                String tableModel = tableModelList.get(i);
                String selectSql = "select * from " + table;
                Cursor cursor = db.rawQuery(selectSql, null);
                List<String> oldColumns = new ArrayList<String>();
                for (String column : cursor.getColumnNames()) {
                    oldColumns.add(column);
                }
                String alterSql = "alter table " + table + " rename to " + table + "_temp";
                db.execSQL(alterSql);
                try {
                    createTable(db, tableModel);
                } catch (NoColumnException e) {
                    Logger.e(e.toString());
                }
                try {
                    BaseTable entity = (BaseTable) (Class.forName(tableModelList.get(i)).newInstance());
                    List<String> newColumns = getColumns(entity);
                    StringBuilder upgradeSqlBuilder = new StringBuilder("insert into " + table + " select id, ");
                    int j = 0;
                    for (String column : newColumns) {
                        if (oldColumns.contains(column)) {
                            upgradeSqlBuilder.append(column + ", ");
                            j++;
                        }
                    }

                    if (j != 0 && j < newColumns.size()) {
                        for (int k = 0, length = newColumns.size() - j; k < length; k++) {
                            upgradeSqlBuilder.append("'', ");
                        }
                    }
                    String upgradeStr = upgradeSqlBuilder.toString();
                    String upgradeSql = upgradeStr.substring(0, upgradeStr.length() - 2) + " from " + table + "_temp";
                    db.execSQL(upgradeSql);
                    String deleteSql = "drop table if exists " + table + "_temp";
                    db.execSQL(deleteSql);
                } catch (InstantiationException e) {
                    Logger.e(e.toString());
                } catch (IllegalAccessException e) {
                    Logger.e(e.toString());
                } catch (ClassNotFoundException e) {
                    Logger.e(e.toString());
                } catch (Exception e) {
                    Logger.e(e.toString());
                }
            }
            showTableList(db);
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    /**
     * 创建表
     *
     * @param db SQLiteDatabase
     * @return 表名的List
     */
    private List<String> createTable(SQLiteDatabase db) throws BaseSQLiteException {
        List<String> columnList = new ArrayList<>();
        List<String> tableList = new ArrayList<>();
        List<String> tableNameList = new ArrayList<>();
        for (String table : tableSet) {
            try {
                StringBuilder sql = new StringBuilder("create table if not exists ");
                BaseTable entity = (BaseTable) (Class.forName(table).newInstance());
                String tableName = getTableName(entity);
                tableList.add(table);
                tableNameList.add(tableName);
                sql.append(tableName);
                sql.append(" (id integer primary key autoincrement, ");
                columnList = getColumns(entity);
                int length = columnList.size();
                if(length == 0){
                    throw new BaseSQLiteException("The table " + tableName + " has not columns");
                }
                for (int i = 0; i < length; i++) {
                    sql.append(columnList.get(i) + " ");
                    if (i == length - 1) {
                        break;
                    }
                    sql.append(", ");
                }
                sql.append(");");
                db.execSQL(sql.toString());
            } catch (InstantiationException e) {
                Logger.e(e.toString());
            } catch (IllegalAccessException e) {
                Logger.e(e.toString());
            } catch (ClassNotFoundException e) {
                Logger.e(e.toString());
            } catch (NoSuchTableException e) {
                Logger.e(e.toString());
            }
        }

        SharedPreferencesManager.getInstance().putListString(TABLE_MODEL, tableList);
        SharedPreferencesManager.getInstance().putListString(TABLE_NAME, tableNameList);
        return columnList;
    }

    private void createTable(SQLiteDatabase db, String table) throws NoColumnException {
        try {
            StringBuilder sql = new StringBuilder("create table if not exists ");
            BaseTable entity = (BaseTable) (Class.forName(table).newInstance());
            String tableName = getTableName(entity);
            sql.append(tableName);
            sql.append(" (id integer primary key autoincrement, ");
            List<String> columnList = getColumns(entity);
            if(columnList.size() == 0){
                throw new NoColumnException("The table " + table + " has no columns");
            }
            for (int i = 0, length = columnList.size(); i < length; i++) {
                sql.append(columnList.get(i) + " ");
                if (i == length - 1) {
                    break;
                }
                sql.append(", ");
            }
            sql.append(");");
            db.execSQL(sql.toString());
            SharedPreferencesManager.getInstance().addListString(TABLE_MODEL, table);
            SharedPreferencesManager.getInstance().addListString(TABLE_NAME, tableName);
        } catch (InstantiationException e) {
            Logger.e(e.toString());
        } catch (IllegalAccessException e) {
            Logger.e(e.toString());
        } catch (ClassNotFoundException e) {
            Logger.e(e.toString());
        } catch (NoSuchTableException e) {
            Logger.e(e.toString());
        }
    }

    /**
     * 获取表名
     *
     * @param entity 表对象
     * @return 表名
     * @throws NoSuchTableException
     */
    private String getTableName(BaseTable entity) throws NoSuchTableException {
        String tableName = "";
        if (entity.getClass().isAnnotationPresent(Table.class)) {
            Table table = entity.getClass().getAnnotation(Table.class);
            tableName = table.table();

            if (tableName.length() == 0) {
                throw new NoSuchTableException("The table " + entity.getClass().getSimpleName() + " is not exist");
            }
        } else {
            tableName = entity.getClass().getSimpleName();
        }
        return tableName;
    }

    /**
     * 获取列名
     *
     * @param entity 表对象
     * @return 列名的List
     */
    private List<String> getColumns(BaseTable entity) {
        Set<String> columnSet = new HashSet<String>();
        java.lang.reflect.Field[] fields = entity.getClass().getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column meta = field.getAnnotation(Column.class);
                String column = meta.column();
                if (column.equals("")) {
                    column = field.getName();
                }

                columnSet.add(column);
            }
        }

        List<String> columnList = new ArrayList<String>();
        for (String column : columnSet) {
            columnList.add(column);
        }
        return columnList;
    }

    /**
     * 读取数据库的配置文件
     *
     * @param context 上下文
     * @throws BaseSQLiteException
     */
    private static void readXml(Context context) throws BaseSQLiteException {
        tableSet = new HashSet<String>();
        InputStream in = null;
        try {
            in = context.getResources()
                    .getAssets().open("database.xml");
        } catch (IOException e) {
            throw new BaseSQLiteException("database.xml is not exist");
        }
        XmlPullParserFactory factory;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(in, "UTF-8");
            int evtType = xpp.getEventType();
            // 一直循环，直到文档结束
            while (evtType != XmlPullParser.END_DOCUMENT) {
                switch (evtType) {
                    case XmlPullParser.START_TAG:
                        String tag = xpp.getName();
                        if (tag.equals("dbname")) {
                            dbName = xpp.getAttributeValue(0);
                        } else if (tag.equals("version")) {
                            version = Integer.valueOf(xpp.getAttributeValue(0));
                        } else if (tag.equals("mapping")) {
                            tableSet.add(xpp.getAttributeValue(0));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;
                }
                //获得下一个节点的信息
                evtType = xpp.next();
            }
        } catch (Exception e) {
            Logger.e(e.toString());
        } finally {
            List<String> tableList = new ArrayList<String>();
            for (String table : tableSet) {
                tableList.add(table);
            }
            SharedPreferencesManager.getInstance().putListString(TABLE_MODEL, tableList);
        }
    }

    private void showTableList(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("select name from sqlite_master where type='table' order by name", null);
        while (cursor.moveToNext()) {
            //遍历出表名
            String name = cursor.getString(0);
            Logger.e(name);
        }
    }
}