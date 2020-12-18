package com.hopu.web.controller;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hopu.domain.Menu;
import com.hopu.domain.Role;
import com.hopu.domain.User;
import com.hopu.domain.UserRole;
import com.hopu.service.IRoleService;
import com.hopu.service.IUserRoleService;
import com.hopu.utils.PageEntity;
import com.hopu.utils.ResponseEntity;
import com.hopu.utils.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.hopu.utils.ResponseEntity.error;
import static com.hopu.utils.ResponseEntity.success;

@Controller
@RequestMapping("/role")
public class RoleController {
    @Autowired
    private IRoleService roleService;
    @Autowired
    private IUserRoleService userRoleService;

    //向用户列表页面跳转
    @RequestMapping("/tolistPage")
    public String roleList() {
        return "admin/role/role_list";
    }

    //分页查询用户列表

    @ResponseBody
    @RequestMapping("/list")
    public IPage<Role> roleList(int page, int limit, Role role, Model model) {
        // 设置分页条件
        Page<Role> page2 = new Page<Role>(page, limit);

        // QueryWrapper封装查询条件
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>(new Role());
        if (role != null) {
            if (!StringUtils.isEmpty(role.getRole()))
                queryWrapper.like("role", role.getRole());
            if (!StringUtils.isEmpty(role.getRemark()))
                queryWrapper.like("remark", role.getRemark());

        }
// 分页查询时，带上分页数据以及查询条件对象
        IPage<Role> roleIPage = roleService.page(page2,queryWrapper);
        return roleIPage;
}
    // 向用户添加页面跳转
    @RequestMapping("/toAddPage")
    public String toAddPage(){
        return "admin/role/role_add";
    }
    /**
     * 保存
     * @return
     */
    @ResponseBody
    @RequestMapping("/save")
    public ResponseEntity addRole(Role role){
        // 进行用户名重名查询
        Role role2 = roleService.getRoleByRole(role.getRole());
        if (role2!=null) {
            return error("用户名已存在");
        }
        role.setId(UUIDUtils.getID());
        role.setRemark(UUIDUtils.getID());
        role.setCreateTime(new Date());
        roleService.save(role);
        return success();
    }


    // 向修改页面跳转
    @RequestMapping("/toUpdatePage")
    public String toUpdatePage(String id, HttpServletRequest request){
        Role role = roleService.getById(id);
        request.setAttribute("role",role);
        return "admin/role/role_update";
    }

    // 用户修改
    @RequestMapping("/update")
    @ResponseBody
    public ResponseEntity updateRole(Role role){
        role.setUpdateTime(new Date());
        roleService.updateById(role);
        return ResponseEntity.success();
    }

    // 用户删除
    @RequestMapping("/delete")
    @ResponseBody
    public ResponseEntity deleteUser(@RequestBody List<Role> roles){
        try {
            // 如果是root用户，禁止删除
            for (Role role : roles) {
                if("root".equals(role.getRole())){
                    throw new Exception("不能删除超级管理员");
                }
//                if(user.getUserName().equals("root")){ // nullpointerException
//                    throw new Exception("不能删除超级管理员");
//                }
                roleService.removeById(role.getId());
            }
            return ResponseEntity.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.error(e.getMessage());
        }
    }

    /**
     * 跳转分配权限界面
     */
    @RequestMapping("/toSetMenuPage")
    public String toSetMenuPage(String id, Model model){
        model.addAttribute("role_id",id);
        return "admin/role/role_setMenu";
    }
/**
 * 设置权限
 */
    @ResponseBody
    @RequestMapping("/setMenu")
    public ResponseEntity setMenu(String id, @RequestBody ArrayList<Menu> menus){
        roleService.setMenu(id, menus);
        return success();
    }

    /**
     * 查询用户关联的角色列表
     */
    @ResponseBody
    @RequestMapping("/roleList")
    public PageEntity List(String userId, Role role){
        List<UserRole> userRoles = userRoleService.list(new
                QueryWrapper<UserRole>().eq("user_id", userId));
        QueryWrapper<Role> queryWrapper = new QueryWrapper<Role>();
        if (role!=null){
            if (!StringUtils.isEmpty(role.getRole()))
                queryWrapper.like("role", role.getRole());
        }
        List<Role> roles = roleService.list(queryWrapper);
        List<JSONObject> list = new ArrayList<JSONObject>();
// 同样需要对用户已经关联的角色进行勾选，根据layui需要填充一个LAY_CHECKED字段
        for (Role role2 : roles) {
            JSONObject jsonObject =
                    JSONObject.parseObject(JSONObject.toJSONString(role2));
            boolean rs = false;
            for (UserRole userRole : userRoles) {
                if (userRole.getRoleId().equals(role2.getId())) {
                    rs = true;
                }
            }
            jsonObject.put("LAY_CHECKED", rs);
            list.add(jsonObject);
        }
        return new PageEntity(list.size(), list);
    }





}
