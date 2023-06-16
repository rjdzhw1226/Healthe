package com.itheima.jobs;

import com.itheima.constant.RedisConstant;
import com.itheima.utils.QiniuUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.JedisPool;

import java.util.Set;

/**
 * 定时任务
 */
public class ClearImgJob {

    @Autowired
    private JedisPool jedisPool;
    public void clearImg(){
        //取差值清除图片
        Set<String> set = jedisPool.getResource().sdiff(RedisConstant.SETMEAL_PIC_RESOURCES, RedisConstant.SETMEAL_PIC_DB_RESOURCES);
        if(set != null){
            for (String pic : set) {
                //删除服务器的图片
                QiniuUtils.deleteFileFromQiniu(pic);
                //删除集合中图片
                jedisPool.getResource().srem(RedisConstant.SETMEAL_PIC_RESOURCES,pic);
            }
        }
    }
}
