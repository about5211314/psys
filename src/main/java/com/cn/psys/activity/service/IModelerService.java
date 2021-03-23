package com.cn.psys.activity.service;

import com.cn.psys.base.RestResult;
import org.activiti.engine.repository.Model;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;

public interface IModelerService extends IBaseService<Model>{
    RestResult createModel(String key, String name, String category, String descp) throws UnsupportedEncodingException;


    RestResult saveModelXml(String modelId, MultiValueMap<String, String> values);

    RestResult deleteModel(String modelId);

    RestResult deployModel(String modelId) throws Exception;

    RestResult getEditorXml(String modelId);

    RestResult listModel();
}
