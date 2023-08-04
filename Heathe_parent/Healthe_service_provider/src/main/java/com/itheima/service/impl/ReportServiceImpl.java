package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dao.MemberDao;
import com.itheima.dao.OrderDao;
import com.itheima.dao.ReportDao;
import com.itheima.pojo.Member;
import com.itheima.service.ReportService;
import com.itheima.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 统计报表服务
 */
@Service(interfaceClass = ReportService.class)
@Transactional
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportDao reportDao;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private OrderDao orderDao;

    /**
     * 获得运营统计数据
     * Map数据格式：
     *      todayNewMember -> number
     *      totalMember -> number
     *      thisWeekNewMember -> number
     *      thisMonthNewMember -> number
     *      todayOrderNumber -> number
     *      todayVisitsNumber -> number
     *      thisWeekOrderNumber -> number
     *      thisWeekVisitsNumber -> number
     *      thisMonthOrderNumber -> number
     *      thisMonthVisitsNumber -> number
     *      hotSetmeal -> List<Setmeal>
     */
    @Override
    public Map<String, Object> getBusinessReport() throws Exception{
        //获得当前日期
        String today = DateUtils.parseDate2String(DateUtils.getToday());
        //获得本周一的日期
        String thisWeekMonday = DateUtils.parseDate2String(DateUtils.getThisWeekMonday());
        //获得本月第一天的日期
        String firstDay4ThisMonth =
                DateUtils.parseDate2String(DateUtils.getFirstDay4ThisMonth());

        //今日新增会员数
        Integer todayNewMember = memberDao.findMemberCountByDate(today);

        //总会员数
        Integer totalMember = memberDao.findMemberTotalCount();

        //本周新增会员数
        Integer thisWeekNewMember = memberDao.findMemberCountAfterDate(thisWeekMonday);

        //本月新增会员数
        Integer thisMonthNewMember = memberDao.findMemberCountAfterDate(firstDay4ThisMonth);

        //今日预约数
        Integer todayOrderNumber = orderDao.findOrderCountByDate(today);

        //本周预约数
        Integer thisWeekOrderNumber = orderDao.findOrderCountAfterDate(thisWeekMonday);

        //本月预约数
        Integer thisMonthOrderNumber = orderDao.findOrderCountAfterDate(firstDay4ThisMonth);

        //今日到诊数
        Integer todayVisitsNumber = orderDao.findVisitsCountByDate(today);

        //本周到诊数
        Integer thisWeekVisitsNumber = orderDao.findVisitsCountAfterDate(thisWeekMonday);

        //本月到诊数
        Integer thisMonthVisitsNumber = orderDao.findVisitsCountAfterDate(firstDay4ThisMonth);

        //热门套餐（取前4）
        List<Map> hotSetmeal = orderDao.findHotSetmeal();

        Map<String,Object> result = new HashMap<>();
        result.put("reportDate",today);
        result.put("todayNewMember",todayNewMember);
        result.put("totalMember",totalMember);
        result.put("thisWeekNewMember",thisWeekNewMember);
        result.put("thisMonthNewMember",thisMonthNewMember);
        result.put("todayOrderNumber",todayOrderNumber);
        result.put("thisWeekOrderNumber",thisWeekOrderNumber);
        result.put("thisMonthOrderNumber",thisMonthOrderNumber);
        result.put("todayVisitsNumber",todayVisitsNumber);
        result.put("thisWeekVisitsNumber",thisWeekVisitsNumber);
        result.put("thisMonthVisitsNumber",thisMonthVisitsNumber);
        result.put("hotSetmeal",hotSetmeal);

        return result;
    }

    public Map<String, Object> getMemberAgeReport() {
        List<Member> lists = memberDao.findAll();
        List<Integer> ages = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        int a = 0, b = 0, c = 0, d = 0, e = 0, f = 0;
        for (Member member : lists) {
            Date birthday = member.getBirthday();
            int age = getAgeByBirth(birthday);
            if (age > 0 && age <= 6) {
                a++;
            }
            if (age > 7 && age <= 12) {
                b++;
            }
            if (age > 12 && age <= 17) {
                c++;
            }
            if (age > 17 && age <= 45) {
                d++;
            }
            if (age > 44 && age <= 69) {
                e++;
            }
            if (age > 69) {
                f++;
            }
        }
        ages.add(a);
        ages.add(b);
        ages.add(c);
        ages.add(d);
        ages.add(e);
        ages.add(f);
        List<String> name = new ArrayList<>();
        name.add("婴幼儿");
        name.add("少儿");
        name.add("青少年");
        name.add("青年");
        name.add("中年");
        name.add("老年");
        result.put("memberAgeName", name);
        result.put("memberAgeCount", ages);
        return result;
    }


    private static int getAgeByBirth(Date birthday) {
        if (birthday == null) {
            int age = -1;
            return age;
        }
        int age = 0;
        Calendar cal = Calendar.getInstance();
        if (cal.before(birthday)) { //出生日期晚于当前时间，无法计算
            throw new IllegalArgumentException(
                    "The birthDay is before Now.It's unbelievable!");
        }
        int yearNow = cal.get(Calendar.YEAR);  //当前年份
        int monthNow = cal.get(Calendar.MONTH);  //当前月份
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH); //当前日期
        cal.setTime(birthday);
        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
        age = yearNow - yearBirth;   //计算整岁数
        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth) age--;//当前日期在生日之前，年龄减一
            } else {
                age--;//当前月份在生日之前，年龄减一
            }
        }
        return age;
    }

    /**
     * 查询已预约的套餐的名称和对应的已预约的套餐的金额
     * @return
     */
    public List<Map> findSetmealMoney() {
        return reportDao.findSetmealMoney();
    }
}

