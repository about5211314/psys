package com.cn.psys.activity;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;

import java.util.List;

public class ActivityTools {

    public JSONObject list(int page,int rows,long total,List<String> obj){

        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);
        JSONArray json = JSONArray.fromObject(obj, jsonConfig);
        JSONObject resultJson = new JSONObject();
        resultJson.put("total", total);
        resultJson.put("size", rows);
        resultJson.put("current", page);
        resultJson.put("searchCount", true);
        resultJson.put("pages", total / rows + 1);
        resultJson.put("records", json);

        return resultJson;

    }


}
