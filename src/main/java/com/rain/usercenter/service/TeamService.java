package com.rain.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rain.usercenter.dto.TeamQuery;
import com.rain.usercenter.model.domain.Team;
import com.rain.usercenter.model.domain.User;
import com.rain.usercenter.model.domain.request.TeamJoinRequest;
import com.rain.usercenter.model.domain.request.TeamQuitRequest;
import com.rain.usercenter.model.domain.request.TeamUpDataRequest;
import com.rain.usercenter.model.domain.vo.TeamUservo;

import java.util.List;

/**
* @author 小爱
* @description 针对表【team(队伍)】的数据库操作Service
*
*/
public interface TeamService extends IService<Team> {

    /**
     *
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team ,User loginUser);

    /**
     * 搜索队伍
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUservo> listTame(TeamQuery teamQuery ,boolean isAdmin);

    /**
     * 更新队伍
     *
     * @param teamUpDataRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpDataRequest teamUpDataRequest, User loginUser);

    /**\
     *用户加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest , User loginUser);

    /**
     * 用户退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 解散队伍
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long id, User loginUser);
}
