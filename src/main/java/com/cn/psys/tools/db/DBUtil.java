package com.cn.psys.tools.db;

import com.cn.psys.tools.yml.YmlUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author zhuwei
 */
public class DBUtil {


    /**
     *
     * @return
     * @throws Exception
     */
    public static Connection getConnection_notes() throws Exception {
        // 数据库地址
        String url = YmlUtils.getYmlByFileName("classpath:application-jdbc.yml", "spring", "datasource", "url").get("spring.datasource.url");
        // 账号
        String username = YmlUtils.getYmlByFileName("classpath:application-jdbc.yml", "spring", "datasource", "username").get("spring.datasource.username");
        // 密码
        String password = YmlUtils.getYmlByFileName("classpath:application-jdbc.yml", "spring", "datasource", "password").get("spring.datasource.password");
        //驱动
        String deiverUrl = YmlUtils.getYmlByFileName("classpath:application-jdbc.yml", "spring", "datasource", "driver-class-name").get("spring.datasource.driver-class-name");
        Connection conn = null;
        try {
            Class.forName(deiverUrl);
            conn = DriverManager
                    .getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println(getConnection_notes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}