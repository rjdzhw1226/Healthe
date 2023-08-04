package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.constant.MessageConstant;
import com.itheima.dao.MemberDao;
import com.itheima.dao.OrderDao;
import com.itheima.dao.OrderSettingDao;
import com.itheima.entity.Conditions;
import com.itheima.entity.PageResult;
import com.itheima.entity.Result;
import com.itheima.pojo.Member;
import com.itheima.pojo.Order;
import com.itheima.pojo.OrderSetting;
import com.itheima.service.CheckItemService;
import com.itheima.service.OrderService;
import com.itheima.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
@Service(interfaceClass = OrderService.class)
@Transactional
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderSettingDao orderSettingDao;

    @Autowired
    private MemberDao memberDao;

    @Autowired
    private OrderDao orderDao;

    //体检预约
    @Override
    public Result order(Map map) {
        //检查当前日期是否进行了预约设置
        String orderDate = (String) map.get("orderDate");
        Date date = null;
        try {
            date = DateUtils.parseString2Date(orderDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        OrderSetting orderSetting = orderSettingDao.findByOrderDate(date);
        if(orderSetting == null){
            //指定日期没有进行设置
            return new Result(false, MessageConstant.SELECTED_DATE_CANNOT_ORDER);
        }

        //检查预约日期是否预约已满
        int number = orderSetting.getNumber();//可预约人数
        int reservations = orderSetting.getReservations();//已预约人数
        if(reservations >= number){
            //预约已满，不能预约
            return new Result(false,MessageConstant.ORDER_FULL);
        }
        //检查当前用户是否为会员，根据手机号判断
        String telephone = (String) map.get("telephone");
        Member member = memberDao.findByTelephone(telephone);
        //防止重复预约
        if(member != null){
            Integer memberId = member.getId();
            int setmealId = Integer.parseInt((String) map.get("setmealId"));
            Order order = new Order(memberId,date,setmealId);
            List<Order> list = orderDao.findByCondition(order);
            if(list != null && list.size() > 0){
                //已经完成了预约，不能重复预约
                return new Result(false, MessageConstant.HAS_ORDERED);
            }
        }else{
            //当前用户不是会员，需要添加到会员表
            member = new Member();
            member.setName((String) map.get("name"));
            member.setPhoneNumber(telephone);
            member.setIdCard((String) map.get("idCard"));
            member.setSex((String) map.get("sex"));
            member.setRegTime(new Date());
            memberDao.add(member);
        }
        //可以预约，设置预约人数加一
        orderSetting.setReservations(orderSetting.getReservations()+1);
        orderSettingDao.editReservationsByOrderDate(orderSetting);

        //保存预约信息到预约表
        Order order = new Order(member.getId(), date, (String)map.get("orderType"), Order.ORDERSTATUS_NO, Integer.parseInt((String) map.get("setmealId")));
        orderDao.add(order);
        return new Result(true,MessageConstant.ORDER_SUCCESS,order.getId());
    }

    //根据id查询预约信息，包括体检人信息、套餐信息
    @Override
    public Map findById(Integer id) {
        Map map = orderDao.findById4Detail(id);
        if(map != null){
            //处理日期格式
            Date orderDate = (Date) map.get("orderDate");
            try {
                map.put("orderDate",DateUtils.parseDate2String(orderDate));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * 后台的预约管理中实现健康管理师的意见的回显
     * @param id  预约订单id值
     * @return
     */
    @Override
    public Order findHealtMessageById(Integer id) {
        return orderDao.findHealtMessageById(id);//返回order中的sport food suggestion第三个属性值封装到order中返回到前端
    }

    @Override
    public List<Order> findByMemberIdWithCondition(Integer memberId) {
        return orderDao.findByMemberIdWithCondition(memberId);
    }

    @Override
    public List<Map> findAll4OrderAndSetmeal(Integer memberId) {
        return orderDao.findAll4OrderAndSetmeal(memberId);
    }

    @Override
    public List<Map> findSetmealByIdAndDate(Integer memberId, String startDate, String endDate, String setmealName) {
        if (setmealName != null && setmealName.length() > 0) {
            //套餐名称非空
            setmealName = "%" + setmealName + "%";
            return orderDao.findSetmealByIdAndDate(memberId, startDate, endDate, setmealName);
        } else {
            //套餐名称为空
            return orderDao.findSetmealByIdAndDateWithoutName(memberId, startDate, endDate);
        }
    }

    @Override
    public Map findAllDataByOrderId(Integer orderId) {
        return orderDao.findAllDataByOrderId(orderId);
    }

    //分页查询
    public PageResult findByPageAndCondition(Conditions conditions) {
        System.out.println("-----------------");
        System.out.println(conditions.toString());
        System.out.println("-----------------");
        Integer currentPage = conditions.getCurrentPage();
        Integer pageSize = conditions.getPageSize();
        //查询规则
        String queryString = conditions.getQueryString();
        System.out.println(queryString);
        Date[] queryDate = conditions.getQueryDate();
        String queryOrderStatus = conditions.getQueryOrderStatus();
        String queryOrderType = conditions.getQueryOrderType();

        PageHelper.startPage(currentPage, pageSize);
        Date startDate = null;
        Date endDate = null;
        if (queryDate != null && queryDate.length > 0) {
            startDate = queryDate[0];
            endDate = queryDate[1];
        }
        Page page = orderDao.findByPageAndCondition(queryString, startDate, endDate, queryOrderStatus, queryOrderType);
        //查询的总条数
        long total = page.getTotal();
        //查询的当前页的集合
        List result = page.getResult();
        return new PageResult(total, result);
    }

    //编辑操作，先查询点击对象的id
    public Map findOrderById(Integer id) {
        return orderDao.findOrderById1(id);
    }

    //通过订单id查询套餐id
    public List<Integer> findSetmealIdsByOrderId(Integer id) {
        return orderDao.findSetmealIdsByOrderId(id);
    }

    //新增预约
    public Result addPhoneOrder(Map map) throws Exception {
        //1、检查用户所选择的预约日期是否已经提前进行了预约设置，如果没有设置则无法进行预约
        String orderDate = (String) map.get("orderDate");//预约日期
        OrderSetting orderSetting = orderSettingDao.findByOrderDate(DateUtils.parseString2Date(orderDate));
        if (orderSetting == null) {
            //指定日期没有进行预约设置，无法完成体检预约
            return new Result(false, MessageConstant.SELECTED_DATE_CANNOT_ORDER);
        }
        //2、检查用户所选择的预约日期是否已经约满，如果已经约满则无法预约
        int number = orderSetting.getNumber();//可预约人数
        int reservations = orderSetting.getReservations();//已预约人数
        if (reservations >= number) {
            //已经约满，无法预约
            return new Result(false, MessageConstant.ORDER_FULL);
        }
        //3、检查用户是否重复预约（同一个用户在同一天预约了同一个套餐），如果是重复预约则无法完成再次预约
        String telephone = (String) map.get("phoneNumber");//获取用户页面输入的手机号
        Member member = memberDao.findByTelephone(telephone);
        Integer setmealId = null;
        if (member != null) {
            //判断是否在重复预约
            Integer memberId = member.getId();//会员ID
            Date order_Date = DateUtils.parseString2Date(orderDate);//预约日期
            setmealId = (Integer) map.get("setmealId");//套餐ID
            Order order = new Order(memberId, order_Date, setmealId);
            //根据条件进行查询
            List<Order> list = orderDao.findByCondition(order);
            if (list != null && list.size() > 0) {
                //说明用户在重复预约，无法完成再次预约
                return new Result(false, MessageConstant.HAS_ORDERED);
            }
        } else {
            //4、检查当前用户是否为会员，如果是会员则直接完成预约，如果不是会员则自动完成注册并进行预约
            member = new Member();
            member.setName((String) map.get("name"));
            member.setPhoneNumber(telephone);
            member.setIdCard((String) map.get("idCard"));
            member.setSex((String) map.get("sex"));
            member.setRegTime(new Date());
            memberDao.add(member);//自动完成会员注册
        }
        //5、预约成功，更新当日的已预约人数
        Order order = new Order();
        order.setMemberId(member.getId());//设置会员ID
        order.setOrderDate(DateUtils.parseString2Date(orderDate));//预约日期
        order.setOrderType((String) map.get("orderType"));//预约类型
        order.setOrderStatus(Order.ORDERSTATUS_NO);//到诊状态
        setmealId = (Integer) map.get("setmealId");
        order.setSetmealId(setmealId);//套餐ID
        orderDao.add(order);

        orderSetting.setReservations(orderSetting.getReservations() + 1);//设置已预约人数+1
        orderSettingDao.editReservationsByOrderDate(orderSetting);

        return new Result(true, MessageConstant.ORDER_SUCCESS, order.getId());
    }

    //保存编辑后的数据
    public void edit(Integer setmealId, Map map) {
        //获取dataEditForm中各个字段的值
        String name = (String) map.get("name");
        String phoneNumber = (String) map.get("phoneNumber");
        System.out.println(name);
        System.out.println(phoneNumber);
        memberDao.updateByOrder(name, phoneNumber);

        //将编辑页面的字段值重新封装为一个对象
        Order order = new Order();
        String orderDate = (String) map.get("orderDate");
        try {
            order.setOrderDate(DateUtils.parseString2Date(orderDate));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(orderDate);
        String orderType = (String) map.get("orderType");
        order.setOrderType(orderType);
        System.out.println(orderType);
        String orderStatus = (String) map.get("orderStatus");
        order.setOrderStatus(orderStatus);
        Integer orderId = (Integer) map.get("orderId");
        order.setId(orderId);
        order.setSetmealId(setmealId);
        System.out.println(order);
        //执行修改
        orderDao.update(order);
    }

    //删除预约
    public void delete(Integer id) {
        orderDao.delete(id);
    }

    //修改预约状态
    public void statusEdit(Integer id) {
        Order order = orderDao.findOrderStatesById(id);
        if (order.getOrderStatus().equals("已到诊")) {
            orderDao.editStatus(id);
        } else {
            orderDao.editStatusTo(id);
        }
    }

    /**
     * 编辑用户的就诊状态
     * @param orderStatusId  0:表示由未到诊 变成已到诊
     * @param orderId  预约订单id值
     */
    @Override
    public void statusEdit(Integer orderStatusId, Integer orderId) {
        String orderStatus = null;
        if (orderStatusId == 0) {
            orderStatus = "已到诊";
        }
        orderDao.update1(orderStatus, orderId);
    }
}
