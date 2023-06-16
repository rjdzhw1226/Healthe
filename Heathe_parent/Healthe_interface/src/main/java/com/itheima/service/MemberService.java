package com.itheima.service;

import com.itheima.pojo.Member;

import java.util.List;

public interface MemberService {
    public Member findByTelephone(String telephone);

    public void add(Member member);

    public List<Integer> findMemberCountByMonth(List<String> list);
}
