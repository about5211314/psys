package com.cn.psys.mybatisplus;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;

public class MybatisPlusQuery{
    public static QueryWrapper parseWhereSql(String conditionJson) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (StrUtil.isNotEmpty(conditionJson)) {
            JSONArray jSONArray = JSONUtil.parseArray(conditionJson);
            List<ConditionVo> conditionList = jSONArray.toList(ConditionVo.class);
            if (CollUtil.isNotEmpty(conditionList)) {
                for (ConditionVo conditionVo : conditionList) {
                    switch (conditionVo.getType()) {
                        case "eq":
                            queryWrapper.eq(conditionVo.getColumn(), conditionVo.getValue());
                            break;
                        case "ne":
                            queryWrapper.ne(conditionVo.getColumn(), conditionVo.getValue());
                            break;
                        case "like":
                            queryWrapper.like(conditionVo.getColumn(), conditionVo.getValue());
                            break;
                        case "leftlike":
                            queryWrapper.likeLeft(conditionVo.getColumn(), conditionVo.getValue());
                            break;
                        case "rightlike":
                            queryWrapper.likeRight(conditionVo.getColumn(), conditionVo.getValue());
                            break;
                        case "notlike":
                            queryWrapper.notLike(conditionVo.getColumn(), conditionVo.getValue());
                            break;
                        case "gt":
                            queryWrapper.gt(conditionVo.getColumn(), conditionVo.getValue());
                            break;
                        case "lt":
                            queryWrapper.lt(conditionVo.getColumn(), conditionVo.getValue());
                            break;
                        case "ge":
                            queryWrapper.ge(conditionVo.getColumn(), conditionVo.getValue());
                            break;
                        case "le":
                            queryWrapper.le(conditionVo.getColumn(), conditionVo.getValue());
                            break;
                        case "orderByAsc":
                            queryWrapper.orderByAsc(conditionVo.getColumn());
                            break;
                        case "orderByDesc":
                            queryWrapper.orderByDesc(conditionVo.getColumn());
                            break;
                    }
                }
            }
        }
        return queryWrapper;
    }

    public static void main(String[] args) {
        parseWhereSql("[" +
                "    {column: \"COLUMN_NAME\",type: \"like\", value: \"tim\"}," +
                "    {column: \"COLUMN_AGE\",type: \"eq\", value: \"22\"}," +
                "    {column: \"COLUMN_DATE\",type: \"ge\", value: \"2019-08-16 00:00:00\"}," +
                "    {column: \"COLUMN_DATE\",type: \"le\", value: \"2019-08-16 23:59:59\"}" +
                "]");
    }
}
