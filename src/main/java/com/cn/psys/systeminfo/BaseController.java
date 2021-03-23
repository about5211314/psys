package com.cn.psys.systeminfo;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class BaseController {
    protected Log logger = LogFactory.getLog(getClass());


    protected void response2Client (HttpServletResponse response, String content) {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = null;
        logger.info("response content -->" + content);


        try {

            out = response.getWriter();

            out.print(content);

            out.close();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

}
