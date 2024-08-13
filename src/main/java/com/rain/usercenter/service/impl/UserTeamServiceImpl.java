package com.rain.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rain.usercenter.service.UserTeamService;
import com.rain.usercenter.model.domain.UserTeam;
import com.rain.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 小爱
* @description 针对表【user_team(用户队伍)】的数据库操作Service实现
* @createDate 2023-04-22 20:07:29
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




