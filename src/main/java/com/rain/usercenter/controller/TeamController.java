package com.rain.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rain.usercenter.common.BaseResponse;
import com.rain.usercenter.common.DeleteRequest;
import com.rain.usercenter.common.ErrorCode;
import com.rain.usercenter.common.ResulUtils;
import com.rain.usercenter.dto.TeamQuery;
import com.rain.usercenter.exception.BusinessException;
import com.rain.usercenter.model.domain.Team;
import com.rain.usercenter.model.domain.User;
import com.rain.usercenter.model.domain.UserTeam;
import com.rain.usercenter.model.domain.request.TeamJoinRequest;
import com.rain.usercenter.model.domain.request.TeamQuitRequest;
import com.rain.usercenter.model.domain.request.TeamRequest;
import com.rain.usercenter.model.domain.request.TeamUpDataRequest;
import com.rain.usercenter.model.domain.vo.TeamUservo;
import com.rain.usercenter.service.TeamService;
import com.rain.usercenter.service.UserService;
import com.rain.usercenter.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户接口
 */
@RestController//接收到的数据都转换为json的一个注解
@RequestMapping("/team")
@CrossOrigin(origins ={"http://localhost:3000"} ,allowCredentials = "true")
@Slf4j
public class TeamController {

    @Resource
    private TeamService teamService;
    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;


    //创建队伍
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamRequest teamRequest, HttpServletRequest request){
        if (teamRequest ==null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamRequest,team);
        long  teamId= teamService.addTeam(team,loginUser);

            return ResulUtils.success(teamId);

        }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpDataRequest teamUpDataRequest,HttpServletRequest request){
        if (teamUpDataRequest ==null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpDataRequest,loginUser);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        return ResulUtils.success(true);

    }
    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id){
        if (id < 0){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResulUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUservo>> listTeam(TeamQuery teamQuery,HttpServletRequest request){
        if (teamQuery == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean admin = userService.isAdmin(request);
        List<TeamUservo> teamList = teamService.listTame(teamQuery,admin);
        //判断当前用户是否已加入
        List<Long> teamIdList = teamList.stream().map(TeamUservo::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId",loginUser.getId());
            userTeamQueryWrapper.in("teamId",teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            //已加入的队伍Id集合
            Set<Long>hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team ->{
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        }catch (Exception e){}
        //查询加入队伍的用户信息（人数）
        QueryWrapper<UserTeam> userTeamQueryWrapper1 = new QueryWrapper<>();
        userTeamQueryWrapper1.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper1);
        //队伍 id => 加入这个队伍的用户列表
        Map<Long,List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team ->{
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(),new ArrayList<>()).size());
        });
        return ResulUtils.success(teamList);
    }



    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamByPage(TeamQuery teamQuery){
        if (teamQuery == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        Page<Team> resultPage = teamService.page(page,queryWrapper);
        return ResulUtils.success(resultPage);
    }

    //加入队伍
    @PostMapping("/join")
    public  BaseResponse<Boolean>joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request){
        if (teamJoinRequest == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
      boolean result=  teamService.joinTeam(teamJoinRequest, loginUser);

        return ResulUtils.success(result);
    }

//退出队伍
    @PostMapping("/quit")
    public  BaseResponse<Boolean>quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if (teamQuitRequest == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result=  teamService.quitTeam(teamQuitRequest, loginUser);

        return ResulUtils.success(result);
    }


//删除队伍
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request){
        if (deleteRequest == null || deleteRequest.getId() <=0 ){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id,loginUser );
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResulUtils.success(true);

    }

    /**、
     * 获取我创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUservo>> listMyCreateTeam(TeamQuery teamQuery,HttpServletRequest request){
        if (teamQuery == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        boolean admin = userService.isAdmin(loginUser);
        List<TeamUservo> teamList = teamService.listTame(teamQuery,true);

        //判断当前用户是否已加入
        List<Long> teamIdList = teamList.stream().map(TeamUservo::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser2 = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId",loginUser2.getId());
            userTeamQueryWrapper.in("teamId",teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            //已加入的队伍Id集合
            Set<Long>hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team ->{
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        }catch (Exception e){}
        //查询加入队伍的用户信息（人数）
        QueryWrapper<UserTeam> userTeamQueryWrapper1 = new QueryWrapper<>();
        userTeamQueryWrapper1.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper1);
        //队伍 id => 加入这个队伍的用户列表
        Map<Long,List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team ->{
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(),new ArrayList<>()).size());
        });
        return ResulUtils.success(teamList);
    }

    /**
     * 获取我加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUservo>> listMyJoinTeam(TeamQuery teamQuery,HttpServletRequest request){
        if (teamQuery == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
//        boolean admin = userService.isAdmin(loginUser);
        QueryWrapper<UserTeam>queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);

        //teamId => userId
        //比如：
        // 1 , 2
        // 1 , 3
        // 2 , 4
        //result
        // 1 => 2,3
        // 2 => 4
        Map<Long,List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));

         List<Long> idList  =  new ArrayList<>(listMap.keySet());
         teamQuery.setIdList(idList);
        List<TeamUservo> teamList = teamService.listTame(teamQuery,true);
        return ResulUtils.success(teamList);
    }
}
