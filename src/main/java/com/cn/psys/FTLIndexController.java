package com.cn.psys;

import org.activiti.engine.FormService;
import org.activiti.engine.form.TaskFormData;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class FTLIndexController {
    @Resource
    private FormService formService;

    @RequestMapping("/")
    public String login() {
        return "dist/index";
    }
//    public String login() {
//        return "mobile/login";
//    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String defaultLogin() {
        return "dist/index";
    }
//    public String defaultLogin() {
//        return "mobile/login";
//    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public String login(@RequestParam("username") String username, @RequestParam("password") String password) {
        // 从SecurityUtils里边创建一个 subject
        Subject subject = SecurityUtils.getSubject();
        // 在认证提交前准备 token（令牌）
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        // 执行认证登陆
        try {
            subject.login(token);
        } catch (UnknownAccountException uae) {
            return "{\"success\":false, \"msg\":\"未知账户\"}";
        } catch (IncorrectCredentialsException ice) {
            return "{\"success\":false, \"msg\":\"密码不正确\"}";
        } catch (LockedAccountException lae) {
            return "{\"success\":false, \"msg\":\"账户已锁定\"}";
        } catch (ExcessiveAttemptsException eae) {
            return "{\"success\":false, \"msg\":\"用户名或密码错误次数过多\"}";
        } catch (AuthenticationException ae) {
            return "{\"success\":false, \"msg\":\"用户名或密码不正确\"}";
        }
        if (subject.isAuthenticated()) {
            return "{\"success\":true, \"msg\":\"登录成功\",\"data\":{\"username\":\""+username+"\"}}";
        } else {
            token.clear();
            return "{\"success\":false, \"msg\":\"登录失败\"}";
        }
    }


    @RequestMapping("index")
    public String index() {
        return "index";
    }



    @RequestMapping("forms")
    public String forms(String taskId, Map<String, Object> map) {
        //获取自定义表单id
        TaskFormData formData = formService.getTaskFormData(taskId);
        String formKey = formData.getFormKey();
        System.out.println(formKey);

        map.put("taskId", taskId);
        map.put("formKey", formKey);
        return "forms";
    }


    @RequestMapping("form")
    public String form() {
        return "form";
    }

    @GetMapping("shiroAll")
    @RequiresPermissions("shiro:all")
    @ResponseBody
    public Map<String,Object> shiroAll(){
        Subject subject = SecurityUtils.getSubject();
        String UserName = subject.getPrincipal().toString().split(":")[0];
        Map<String,Object> map = new HashMap<>();
        map.put("userName", UserName);
        map.put("value", "有权限");
        return map;
    }


    @GetMapping("noAuthority")
    @RequiresPermissions("noAuthority")
    @ResponseBody
    public Map<String,Object> noAuthority(){
        Subject subject = SecurityUtils.getSubject();
        String UserName = subject.getPrincipal().toString().split(":")[0];
        Map<String,Object> map = new HashMap<>();
        map.put("value", "无权限");
        map.put("userName", UserName);
        return map;
    }


    /**
     * 手动退出
     * @param httpServletRequest
     * @return
     */
    @GetMapping("logout")
    public String logout(HttpServletRequest httpServletRequest) {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return  "退出成功";
    }

    /**
     * 自动退出 退出后会重定向到跟目录
     * @return
     */
    @GetMapping("logout2")
    public String logout2() {
        return  "退出成功";
    }
}
