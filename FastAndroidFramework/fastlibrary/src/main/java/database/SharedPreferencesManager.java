package database;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * SharedPreferences的管理类，可以存取List并且对double类型的存取保证了精度不会丢失，添加了监听事件的接口
 * Created by wenbiao_zheng on 2014/12/25.
 *
 * @author wenbiao_zheng
 */
public final class SharedPreferencesManager {
    //SharedPreferences的名字
    private static final String PRFE_NAME = "com.maomao.client.PRFE_NAME";

    //SharedPerferences的默认Key值
    private static final String PRFE_KEY = "com.maomao.client.PRFE_KEY";

    //SharedPerferencesManager的单例
    private static SharedPreferencesManager manager;

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PRFE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * 初始化SharedPreferences，该方法最好在Application中调用
     *
     * @param context 上下文
     */
    public static synchronized void init(Context context) {
        if (manager == null) {
            manager = new SharedPreferencesManager(context);
        }
    }

    /**
     * SharedPreferencesManager的单例方法，使用synchronized是因为apply方式的提交是异步的，
     * 虽然效率相比commit方式提高了，但如果是多个并发情况，有可能存在问题，所以通过该关键字确保结果的提交是同步的
     *
     * @return SharedPreferencesManager的单例
     */
    public static synchronized SharedPreferencesManager getInstance() {
        if (manager == null) {
            throw new IllegalStateException(SharedPreferencesManager.class.getSimpleName() +
                    " is not initialized, call init(..) method first.");
        }

        return manager;
    }

    /**
     * 获取Int值
     *
     * @param key key值
     * @return 保存的Int值
     */
    public int getInt(String key) {
        return sharedPreferences.getInt(key, -1);
    }

    /**
     * 保存Int值
     *
     * @param key   key值
     * @param value 保存的Int值
     */
    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * 获取String值
     *
     * @param key key值
     * @return 保存的String值
     */
    public String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    /**
     * 保存String值
     *
     * @param key   key值
     * @param value 保存的String值
     */
    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * 获取long值
     *
     * @param key key值
     * @return 保存的long值
     */
    public long getLong(String key) {
        return sharedPreferences.getLong(key, 0L);
    }

    /**
     * 保存long值
     *
     * @param key   key值
     * @param value 保存的long值
     */
    public void putLong(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }

    /**
     * 获取float值
     *
     * @param key key值
     * @return 保存的float值
     */
    public float getFloat(String key) {
        return sharedPreferences.getFloat(key, 0F);
    }

    /**
     * 保存的float值
     *
     * @param key   key值
     * @param value 保存的float值
     */
    public void putFloat(String key, float value) {
        editor.putFloat(key, value);
        editor.apply();
    }

    /**
     * 获取boolean值
     *
     * @param key key值
     * @return 保存的boolean值
     */
    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    /**
     * 获取boolean值
     *
     * @param key          key值
     * @param defaultValue 默认值
     * @return 保存的boolean值
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * 获取boolean值
     *
     * @param key   key值
     * @param value 保存的boolean值
     */
    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * 获取double值
     *
     * @param key key值
     * @return 保存的double值
     */
    public double getDouble(String key) {
        String number = getString(key);
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 保存double值
     *
     * @param key   key值
     * @param value 保存的double值
     */
    public void putDouble(String key, double value) {
        putString(key, String.valueOf(value));
    }

    /**
     * 获取String的List
     *
     * @param key key值
     * @return String的List
     */
    public ArrayList<String> getListString(String key) {
        String[] stringArray = TextUtils
                .split(sharedPreferences.getString(key, ""), "‚‗‚");
        return new ArrayList<>(
                Arrays.asList(stringArray));
    }

    /**
     * 保存String的List
     *
     * @param key  key值
     * @param list 保存的String的List
     */
    public List<String> putListString(String key, List<String> list) {
        String[] stringArray = list.toArray(new String[list.size()]);
        editor.putString(key, TextUtils.join("‚‗‚", stringArray));
        editor.apply();
        return list;
    }

    public List<String> addListString(String key, String value){
        String[] stringArray = TextUtils
                .split(sharedPreferences.getString(key, ""), "‚‗‚");
        List<String> tableList = new ArrayList<>(
                Arrays.asList(stringArray));
        if(!tableList.contains(value)) {
            tableList.add(value);
            putListString(key, tableList);
        }
        return tableList;
    }
    /**
     * 获取Int的List
     *
     * @param key key值
     * @return String的List
     */
    public ArrayList<Integer> getListInt(String key) {
        String[] intArray = TextUtils
                .split(sharedPreferences.getString(key, ""), "‚‗‚");
        ArrayList<String> parseList = new ArrayList<>(
                Arrays.asList(intArray));
        ArrayList<Integer> resultList = new ArrayList<>();
        for (int i = 0, size = parseList.size(); i < size; i++) {
            resultList.add(Integer.parseInt(parseList.get(i)));
        }

        return resultList;
    }

    /**
     * 保存Int的List
     *
     * @param key  key值
     * @param list Int的List
     */
    public List<Integer> putListInt(String key, List<Integer> list) {
        Integer[] intArray = list.toArray(new Integer[list.size()]);
        editor.putString(key, TextUtils.join("‚‗‚", intArray));
        editor.apply();
        return list;
    }

    /**
     * 获取boolean的List
     *
     * @param key key值
     * @return boolean的List
     */
    public ArrayList<Boolean> getListBoolean(String key) {
        ArrayList<String> origList = getListString(key);
        ArrayList<Boolean> booleanList = new ArrayList<>();
        for (String booleanValue : origList) {
            if (booleanValue.equals("true")) {
                booleanList.add(true);
            } else {
                booleanList.add(false);
            }
        }

        return booleanList;
    }

    /**
     * 保存boolean的List
     *
     * @param key  key值
     * @param list boolean的List
     */
    public void putListBoolean(String key, ArrayList<Boolean> list) {
        ArrayList<String> origList = new ArrayList<>();
        for (Boolean booleanValue : list) {
            if (booleanValue) {
                origList.add("true");
            } else {
                origList.add("false");
            }
        }
        putListString(key, origList);
    }

    /**
     * 删除某个元素
     *
     * @param key key值
     */
    public void remove(String key) {
        editor.remove(key);
        editor.apply();
    }

    /**
     * 清空元素
     */
    public void clear() {
        editor.clear();
        editor.apply();
    }

    /**
     * 获取所有的值
     *
     * @return 存储所有值的Map
     */
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }

    /**
     * 注册监听事件
     *
     * @param listener 监听事件
     */
    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * 撤销监听事件
     *
     * @param listener 监听事件
     */
    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * 检查某值是否存在
     *
     * @param key key值
     * @return 某值是否存在
     */
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }
}
