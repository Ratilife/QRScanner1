package com.example.qrscanner.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.qrscanner.utils.DebugLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryViewModel extends AndroidViewModel {
    private static final String TAG = "InventoryViewModel";
    private MutableLiveData<List<Map<String, String>>> inventoryData = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public InventoryViewModel(Application application) {
        super(application);
    }

    public MutableLiveData<List<Map<String, String>>> getInventoryData() {
        return inventoryData;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadInventoryFromJson(String jsonString) {
        try {
            List<Map<String, String>> data = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(jsonString);
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                Map<String, String> map = new HashMap<>();
                
                // Extract all key-value pairs from JSON
                JSONArray keys = item.names();
                if (keys != null) {
                    for (int j = 0; j < keys.length(); j++) {
                        String key = keys.getString(j);
                        map.put(key, item.getString(key));
                    }
                }
                data.add(map);
            }
            
            inventoryData.setValue(data);
            DebugLogger.d(TAG, "Inventory data loaded: " + data.size() + " items");
        } catch (JSONException e) {
            DebugLogger.e(TAG, "Error parsing JSON", e);
            errorMessage.setValue("Ошибка при парсинге JSON: " + e.getMessage());
        }
    }

    public void addManualInventoryItem(Map<String, String> item) {
        List<Map<String, String>> currentData = inventoryData.getValue();
        if (currentData == null) {
            currentData = new ArrayList<>();
        }
        currentData.add(item);
        inventoryData.setValue(currentData);
    }
}


