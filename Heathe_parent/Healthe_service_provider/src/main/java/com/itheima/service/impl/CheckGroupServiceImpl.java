package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.dao.CheckGroupDao;
import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.pojo.CheckGroup;
import com.itheima.service.CheckGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = CheckGroupService.class)
@Transactional
public class CheckGroupServiceImpl implements CheckGroupService {

    @Autowired
    private CheckGroupDao checkGroupDao;

    //新增检查组 同时使检查项关联检查组 多对多 操作中间关系表和检查组表
    @Override
    public void add(CheckGroup checkGroup, Integer[] checkitemIds) {
        //新增检查组 t_checkgroup表
        System.out.println(checkGroup);
        checkGroupDao.add(checkGroup);
        //设置关系 t_checkgroup_checkitem
        //取刚刚插入t_checkgroup表的id mybatis框架selectKey
        Integer checkGroupId = checkGroup.getId();
        this.setCheckGroupAndCheckItem(checkGroupId,checkitemIds);
    }

    //分页查询
    @Override
    public PageResult pageQuery(QueryPageBean queryPageBean) {
        Integer currentPage = queryPageBean.getCurrentPage();
        Integer pageSize = queryPageBean.getPageSize();
        String queryString = queryPageBean.getQueryString();
        PageHelper.startPage(currentPage , pageSize);
        Page<CheckGroup> page = checkGroupDao.findByCondition(queryString);
        return new PageResult(page.getTotal(),page.getResult());
    }

    //根据id查询检查组
    @Override
    public CheckGroup findById(Integer id) {
        return checkGroupDao.findById(id);
    }

    //根据检查组id查询关联检查项
    @Override
    public List<Integer> findCheckItemIdsByCheckGroupId(Integer id) {
        return checkGroupDao.findCheckItemIdsByCheckGroupId(id);
    }

    //编辑检查组信息 同时关联检查项
    @Override
    public void edit(CheckGroup checkGroup, Integer[] checkitemIds) {
        //修改检查组基本信息
        checkGroupDao.edit(checkGroup);
        //先清理关联关系
        checkGroupDao.deleteAssoication(checkGroup.getId());
        //再关联新的关联信息
        Integer checkGroupId = checkGroup.getId();
        this.setCheckGroupAndCheckItem(checkGroupId,checkitemIds);
    }

    //删除检查组
    @Override
    public void delete(Integer id) {
        try {
            //先删除关联关系
            checkGroupDao.deleteAssoication(id);
            Thread.sleep(5);
            //再删除检查组的信息
            checkGroupDao.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<CheckGroup> findAll() {

        return checkGroupDao.findAll();
    }


    //建立关联关系
    public void setCheckGroupAndCheckItem(Integer checkGroupId ,Integer[] checkitemIds){
        if(checkitemIds != null && checkitemIds.length > 0){
            for (Integer checkitemId : checkitemIds) {
                Map<String,Integer> map = new HashMap<>();
                map.put("checkgroupId",checkGroupId);
                map.put("checkitemId",checkitemId);
                checkGroupDao.setCheckGroupAndCheckItem(map);
            }
        }else{
            System.out.println("ERROR: 关联检查项项为空！");
        }
    }
}
