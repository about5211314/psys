package com.cn.psys.base;

import com.cn.psys.mybatisplus.BaseSelect;
import com.cn.psys.system.mapper.CommonMapper;
import com.cn.psys.system.mapper.DepartmentMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbOPeration {


    private static List<BaseSelect> categories;
    private static Boolean closed = false;

    public static String getSelectDataForJson(String tableName, String pId,
                                              String id, String name, CommonMapper mapperD,int type) {
        List<BaseSelect> listC_T_Sys_BaseSelect;

        listC_T_Sys_BaseSelect = getSelectData(tableName, pId, id,name,mapperD,type);

        return getJsonData(listC_T_Sys_BaseSelect);
    }

    public static List<BaseSelect> getSelectData(String tableName,
                                                 String pId, String id, String name,CommonMapper mapperD,int type) {
        List<BaseSelect> listC_T_Sys_BaseSelect = null;
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("tableName", tableName);
//        map.put("pid", pId);
//        map.put("id", id);
//        map.put("name", name);
        if (type==0){
            listC_T_Sys_BaseSelect = mapperD.selectByMyWrapper();
        }
        if (type==1){
            listC_T_Sys_BaseSelect = mapperD.selectPermissionList();
        }
//        map = null;
        return listC_T_Sys_BaseSelect;
    }

    private static String getJsonData(List<BaseSelect> listSelect) {

        StringBuffer buffer = new StringBuffer();

        categories = listSelect;

        // 获取没有父级的类别
        List<BaseSelect> parents = findByCondition("-1");

        buildJSON(parents, buffer);

        String tmp = buffer.toString();

        return tmp;
    }

    private static List<BaseSelect> findByCondition(String parent) {
        List<BaseSelect> result = new ArrayList<BaseSelect>();

        for (BaseSelect category : categories) {
            if (parent.equals(category.getpId())) {
                result.add(category);
            }
        }
        return result;
    }

    private static void buildJSON(List<BaseSelect> list,
                                  StringBuffer json) {

        json.append("[");
        if (list != null && list.size() > 0) {

            for (int i = 0; i < list.size(); i++) {

                BaseSelect category = list.get(i);

                json.append("{");

                json.append("\"id\"");
                json.append(":");
                json.append("\"");
                json.append(category.getId());
                json.append("\"");

                json.append(",");

                json.append("\"text\"");
                json.append(":");
                json.append("\"");
                json.append(category.getName());
                json.append("\"");

                List<BaseSelect> children = findByCondition(category
                        .getId());

                if (children != null && children.size() > 0) {

                    if (closed) {
                        json.append(",");

                        json.append("\"state\"");
                        json.append(":");
                        json.append("\"closed\"");
                    }

                    json.append(",");

                    json.append("\"children\"");
                    json.append(":");

                    buildJSON(children, json);

                }

                json.append("}");

                if (i != list.size() - 1) {
                    json.append(",");
                }

            }

        }
        json.append("]");

    }
}
