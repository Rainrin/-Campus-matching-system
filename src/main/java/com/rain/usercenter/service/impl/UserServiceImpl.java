package com.rain.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rain.usercenter.common.ErrorCode;
import com.rain.usercenter.exception.BusinessException;
import com.rain.usercenter.service.UserService;
import com.rain.usercenter.model.domain.User;
import com.rain.usercenter.mapper.UserMapper;
import com.rain.usercenter.utils.AlgorithmUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.rain.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.rain.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author 小爱
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值混淆密码
     */
    private static final  String SALT = "rain";



    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //校验
        //这里使用了插件Apache Commons Lang，不用写怎么多的逻辑判断
        //校验不能为空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //校验用户名不能少于4个字符（因为设计表结构的时候定义了字符不能少于4位）
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名不能少于4位");
        }
        //校验密码不能少于8位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不能小于8位数");
        }

        //账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不能包含特殊字符");
        }
        //密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码与重复密码不一致");
        }
        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该用户已存在请重新注册");
        }

        //2.加密

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"保存错误");
        }
        return user.getId();

    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码为空");
        }
        //校验用户名不能少于4个字符
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号小于4个字符");
        }
        //校验密码不能少于8位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不能少于8位");
        }

        //账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号不能包含特殊字符");
        }
        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null){
            log.info("user login failed userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }

        //用户脱敏
        User safetyUser = getsafetyUser(user);

        //记录登录用户信息（登录态）
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        return safetyUser;
    }


    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public int userLoginOut(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户(内存过滤)
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<User>queryWrapper = new QueryWrapper<>();
        List<User>userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();//这个是用来转换json根式的序列化的方法，也是一个外部引用的库
        //2.在内存中判断是否包含要求的标签
        return userList.stream().filter(user ->{
            String tagsStr = user.getTags();
            if (StringUtils.isBlank(tagsStr)){
                return false;
            }
            Set<String>tempTagNameSet = gson.fromJson(tagsStr,new TypeToken<Set<String>>(){}.getType());
            //这句话其实和if else语句差不多，这样写可以减少一些语句嵌套。
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName: tagNameList){
                if (!tempTagNameSet.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getsafetyUser).collect(Collectors.toList());
    }

    /**
     * 根据标签搜索用户（SQL）
     * @param tagNameList
     * @return
     */

@Deprecated
    public List<User> searchUserBySQL(List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //拼接  and  查询
        //like '%Java%'  and  like '%Python%'
        for (String tagName : tagNameList){
            queryWrapper = queryWrapper.like("tags",tagName);
        }
        List<User>userList = userMapper.selectList(queryWrapper);
       return userList.stream().map(this::getsafetyUser).collect(Collectors.toList());
        //1.先查询所用用户
//        QueryWrapper<User>queryWrapper = new QueryWrapper<>();
//        List<User>userList = userMapper.selectList(queryWrapper);
//        Gson gson = new Gson();
//        //2.在内存中判断是否包含要求的标签
//        return userList.stream().filter(user ->{
//            String tagsStr = user.getTags();
//            if (StringUtils.isBlank(tagsStr)){
//                return false;
//            }
//            Set<String>tempTagNameSet = gson.fromJson(tagsStr,new TypeToken<Set<String>>(){}.getType());
//            for (String tagName: tagNameList){
//                if (!tempTagNameSet.contains(tagName)){
//                    return false;
//                }
//            }
//            return true;
//        }).map(this::getsafetyUser).collect(Collectors.toList());
    }



    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    @Override
    public User getsafetyUser(User originUser){
        if (originUser == null){
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    @Override
    public int updateUser(User user,User loginUser) {
            long userId = user.getId();
            if (userId <= 0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            //自己只能修改自己的信息
            //管理员可以修改任意信息
            if (!isAdmin(loginUser) && userId != loginUser.getId()){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            User OldUser = this.getById(user.getId());
            if (OldUser == null){
                throw new BusinessException(ErrorCode.NULL_ERROR);
            }
            //更新
            return this.userMapper.updateById(user);
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request){
        //仅管理员可查
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }


    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    public boolean isAdmin(User loginUser){
        //仅管理员可查
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public List<User> matchUser(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags,new TypeToken<List<String>>(){}.getType());
        //用户列表的下标 => 相似度
        List<Pair<User,Long>>list = new ArrayList<>();
        //一次计算了所有用户和当前用户的相似度
        for (int i = 0; i< userList.size(); i++){
            User user = userList.get(i);
            String userTags = user.getTags();
            //无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())){
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags,new TypeToken<List<String>>(){}.getType());
            //计算分数
            long distance = AlgorithmUtils.minDistance(tagList,userTagList);

            list.add(new Pair<>(user,distance));
        }
        //按编辑距离由小到大排序（编辑距离越小说明和当前用户标签越相似）
        List<Pair<User,Long>> topUserPairList = list.stream()
                .sorted((a,b)->(int) (a.getValue() - b.getValue()) )
                .limit(num)
                .collect(Collectors.toList());
        //原本的userID列表
        List<Long> userIdList = topUserPairList.stream().map(Pair ->Pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id",userIdList);
        //1,3,2
        //user1,user2,user3
        //1 =>user1,  2 => user2,  3=>user3
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> getsafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId :userIdList){
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }


        return finalUserList;

    }


    /**
     * 获取登录信息
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user =(User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return user;
    }
}




