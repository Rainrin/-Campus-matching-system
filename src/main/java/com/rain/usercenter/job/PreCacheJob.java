package com.rain.usercenter.job;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rain.usercenter.model.domain.User;
import com.rain.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;
    //重点用户
    private List<Long> mainUserList = Arrays.asList(3L);

    //每天执行 ，预热推荐用户
    @Scheduled(cron = "0 52 17 * * *")
    public  void doCacheRecommendUser(){
        RLock lock = redissonClient.getLock("Rain:precachejob:docache:lock");
        try{
            //只有一个线程能获取到锁，这锁的时间设置为-1是为了redis能自动续费锁的时间，
            //只要当前的线程没有执行完，那么锁的时间就不会结束，直到线程运行完成
            if (lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                System.out.println("getLock:"+ Thread.currentThread().getId());
                //Thread.sleep(300000);//调试用的
                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("Rain:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    try{
                        valueOperations.set(redisKey,userPage, 20000, TimeUnit.MILLISECONDS);
                    }catch (Exception e){
                        log.error("redis set key error",e);
                    }
                }
            }

        }catch (Exception e){
            log.error("redis set key error",e);
        }finally {
            //只释放自己的锁
            if (lock.isHeldByCurrentThread()){
                System.out.println("unLock:"+ Thread.currentThread().getId());
                lock.unlock();
            }
        }


    }
}
