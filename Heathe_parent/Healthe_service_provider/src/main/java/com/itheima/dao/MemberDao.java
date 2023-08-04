package com.itheima.dao;

import com.github.pagehelper.Page;
import com.itheima.pojo.Member;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MemberDao {
    public List<Member> findAll();
    public Page<Member> selectByCondition(String queryString);
    public void add(Member member);
    public void deleteById(Integer id);
    public Member findById(Integer id);
    public Member findByTelephone(String telephone);
    public void edit(Member member);
    public Integer findMemberCountBeforeDate(String date);
    public Integer findMemberCountByDate(String date);
    public Integer findMemberCountAfterDate(String date);
    public Integer findMemberTotalCount();
    public void update(@Param("id")Integer tempOrderId, @Param("healthName") String username);
    //分页查询会员
    public Page<Member> findPage(String queryString);
    //对健康管理师的回显
    public List<String> findHealthManager();
    //新增
    public void addMember(Member member);
    //会员的回显
    public Member findMemberById(Integer memberId);
    //修改
    public void editMember(Member member);

    public void deleteMember(Integer id);

    //查询所有关联信息
    @MapKey("id")
    public List<Map<String, Object>> findAllmessageById(Integer id);

    public Member findByEmailAndPwd(@Param("email") String email,@Param("password") String md5_password);
    public void updateByOrder(@Param("name") String name, @Param("phoneNumber") String phoneNumber);

}
