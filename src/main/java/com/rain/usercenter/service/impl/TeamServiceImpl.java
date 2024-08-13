package com.rain.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rain.usercenter.common.ErrorCode;
import com.rain.usercenter.dto.TeamQuery;
import com.rain.usercenter.enums.TeamStatusEnum;
import com.rain.usercenter.exception.BusinessException;
import com.rain.usercenter.model.domain.User;
import com.rain.usercenter.model.domain.UserTeam;
import com.rain.usercenter.model.domain.request.TeamJoinRequest;
import com.rain.usercenter.model.domain.request.TeamQuitRequest;
import com.rain.usercenter.model.domain.request.TeamUpDataRequest;
import com.rain.usercenter.model.domain.vo.TeamUservo;
import com.rain.usercenter.model.domain.vo.UserVo;
import com.rain.usercenter.service.TeamService;
import com.rain.usercenter.model.domain.Team;
import com.rain.usercenter.mapper.TeamMapper;
import com.rain.usercenter.service.UserService;
import com.rain.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
* @author 小爱
* @description 针对表【team(队伍)】的数据库操作Service实现
*
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空？
        if (team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2. 是否登录，未登录不允许创建
        if (loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);

        }
        final long userId = loginUser.getId();
        //3. 校验信息
        //   1. 队伍人数 > 1 且 <= 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足要求");
        }
        //   2. 队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍标题不满足要求");

        }
        //   3. 描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isBlank(description) && description.length() >512 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述过长");

        }
        //   4. status 是否公开（int）不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足");
        }
        //   5. 如果 status 是加密状态，一定要有密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum) && (StringUtils.isBlank(password) || password.length() > 32)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码设置不正确");
        }
        //   6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"超时时间大于当前时间");

        }
        //   7. 校验用户最多创建 5 个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >=5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多创建5个队伍");

        }
        //8. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result && teamId ==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");

        }

        //9. 插入用户  => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");

        }

        return teamId;
    }

    @Override
    public List<TeamUservo> listTame(TeamQuery teamQuery,boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null){
            Long id = teamQuery.getId();
            if (id != null && id > 0){
                queryWrapper.eq("id",id);
            }
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)){
                queryWrapper.in("id",idList);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNoneBlank(searchText)){
                queryWrapper.and(qw -> qw.like("name",searchText).or().like("description",searchText));
            }

            String name = teamQuery.getName();
            if (StringUtils.isNoneBlank(name)){
                queryWrapper.eq("name",name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNoneBlank(description)){
                queryWrapper.eq("description",description);
            }
            //查询最大人数相等
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0 ){
                queryWrapper.eq("maxNum",maxNum);
            }
            Long userId = teamQuery.getUserId();
            //根据创建人来查询
            if (userId != null && userId > 0){
                queryWrapper.eq("userId",userId);
            }

            Integer status = teamQuery.getStatus();
            //根据状态来查询
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null){
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)){
                throw  new BusinessException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status",statusEnum.getValue());

        }
        //不展示过期队伍
        queryWrapper.and(qw -> qw.gt("expireTime",new Date()).or().isNull("expireTime"));

        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        List<TeamUservo> teamUserVoList = new ArrayList<>();

        //关联查询创建人的信息
        for (Team team : teamList){
            Long userId = team.getUserId();
            if (userId == null){
                continue;
            }
            User user = userService.getById(userId);
            TeamUservo teamUservo = new TeamUservo();
            BeanUtils.copyProperties(team,teamUservo);
            //脱敏用户信息
            if (user != null) {
                UserVo userVo = new UserVo();
                BeanUtils.copyProperties(user, userVo);
                teamUservo.setCreateUser(userVo);
            }
            teamUserVoList.add(teamUservo);
        }

        return teamUserVoList;
    }

    @Override
    public boolean updateTeam(TeamUpDataRequest teamUpDataRequest, User loginUser) {
        if (teamUpDataRequest == null){
              throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long id  = teamUpDataRequest.getId();

        if (id == null && id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        //只有管理员和创建队伍的人可以修改信息
        if (oldTeam.getId() != loginUser.getId() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpDataRequest.getStatus());
        if (statusEnum.equals(TeamStatusEnum.SECRET)){
            if (StringUtils.isBlank(teamUpDataRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"没有传递密码");
            }
        }


        Team upDataTeam = new Team();
        BeanUtils.copyProperties(teamUpDataRequest,upDataTeam);

        return  this.updateById(upDataTeam);


    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser) {
            if (teamJoinRequest == null){
                new BusinessException(ErrorCode.PARAMS_ERROR);
            }

            //没有加入队伍
            Long teamId = teamJoinRequest.getTeamId();
            Team team = getTeamById(teamId);
            if (team.getExpireTime() != null && team.getExpireTime().before(new Date())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍过期了");

            }
            Integer status = team.getStatus();
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
            if (TeamStatusEnum.PRIVATE.equals(teamStatusEnum)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"禁止加入私有队伍");
            }
            String password = teamJoinRequest.getPassword();
            if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
                if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不正确");
                }
            }
            long userId = loginUser.getId();
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("userId",userId);
            long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
            //队伍最多只能加入5个
            if (hasJoinNum > 5){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"最多只能加入五个队伍");

            }
            //不能重复已进入的退伍
                userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("userId",userId);
                 userTeamQueryWrapper.eq("teamId",teamId);
                 long hasJoinTeam = userTeamService.count(userTeamQueryWrapper);
                if (hasJoinTeam > 0 ){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已加入该队伍");
                }


        //已加入队伍的人数
            long teamHasJoinNum =this.countTeamUserByTeamId(teamId);
            //队伍最多只能加入5个
            if (teamHasJoinNum >= team.getMaxNum() ){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已满");
            }
            //修改用户信息
            UserTeam userTeam = new UserTeam();
            userTeam.setUserId(userId);
            userTeam.setTeamId(teamId);
            userTeam.setJoinTime(new Date());
            return  userTeamService.save(userTeam);

    }

    @Override
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {

            if (teamQuitRequest == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            Long teamId = teamQuitRequest.getTeamId();
            Team team = getTeamById(teamId);
            long userId = loginUser.getId();
            UserTeam queryUserTeam = new UserTeam();
            queryUserTeam.setTeamId(teamId);
            queryUserTeam.setUserId(userId);
            QueryWrapper queryWrapper = new QueryWrapper<>(queryUserTeam);
            long count = userTeamService.count(queryWrapper);
            if (count == 0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"未加入队伍");
            }
            long teamHasJoinNum =this.countTeamUserByTeamId(teamId);
            //队伍只剩一人，解散
            if (teamHasJoinNum == 1){
                //删除队伍和解除所有队伍的关系
                this.removeById(teamId);

            }else {
                //队伍是至少有两个人
                //是否为队长
                if (team.getUserId() ==userId){
                    //把队伍转移给最早加入的用户
                    //1.查询已加入队伍的所有用户和加入时间
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("teamId",teamId);
                    userTeamQueryWrapper.last("order by id asc limit 2");
                    List<UserTeam> userTeamsList = userTeamService.list(userTeamQueryWrapper);
                    if (CollectionUtils.isEmpty(userTeamsList) || userTeamsList.size() <= 1){
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                    }

                    UserTeam nextUserTeam = userTeamsList.get(1);
                    Long nextTeamLeaderId = nextUserTeam.getUserId();
                    //更新当前队伍的队长
                    Team updateTeam = new Team();
                    updateTeam.setId(teamId);
                    updateTeam.setUserId(nextTeamLeaderId);
                    boolean result = this.updateById(updateTeam);
                    if (!result){
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队伍队长失败");
                    }
                }
            }
        //移出关系
        return  userTeamService.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        //队伍是否存在
        Team team = getTeamById(id);
        Long teamUserId = team.getUserId();
        Long userId = loginUser.getId();
        //是不是队长
        if (!teamUserId.equals(userId)){
            throw new BusinessException(ErrorCode.NO_AUTH,"无权访问");
        }
        //移除所有读物的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",team.getId());
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍关联信息失败");
        }
        //删除队伍
       return this.removeById(team.getId());

    }


    /**
     * 根据队伍id 获取队伍信息
     * @param teamId
     * @return
     */
//    @NotNull
    private Team getTeamById(Long teamId) {
        if (teamId == null  || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");

        }
        return team;
    }


    /**
     * 获取某队伍当前人数
     */
    private  long countTeamUserByTeamId(long teamId){
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }


}




