package com.lk.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lk.backend.annotation.AuthCheck;
import com.lk.backend.model.dto.credit.CreditAddRequest;
import com.lk.backend.model.dto.credit.CreditEditRequest;
import com.lk.backend.model.dto.credit.CreditQueryRequest;
import com.lk.backend.model.dto.credit.CreditUpdateRequest;
import com.lk.backend.model.dto.user.DeleteRequest;
import com.lk.backend.model.entity.Credit;
import com.lk.backend.model.entity.User;
import com.lk.backend.service.CreditService;
import com.lk.backend.service.UserService;
import com.lk.common.api.BaseResponse;
import com.lk.common.api.ErrorCode;
import com.lk.common.api.ResultUtils;
import com.lk.common.constant.CommonConstant;
import com.lk.common.constant.UserConstant;
import com.lk.common.exception.BusinessException;
import com.lk.common.exception.ThrowUtils;
import com.lk.common.model.to.UserTo;
import com.lk.common.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * 积分接口
 * @Author : lk
 * @create 2023/10/15
 */
@RestController
@RequestMapping("/credit")
@Slf4j
public class CreditController {
    @Resource
    private CreditService creditService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    public BaseResponse<Long> addCredit(@RequestBody CreditAddRequest creditAddRequest, HttpServletRequest request) {
        if (creditAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Credit credit = new Credit();
        BeanUtils.copyProperties(creditAddRequest, credit);

        Long userId = creditAddRequest.getUserId();
        credit.setUserId(userId);
        boolean result = creditService.save(credit);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newCreditId = credit.getId();
        return ResultUtils.success(newCreditId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteCredit(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser();
        long id = deleteRequest.getId();
        // 判断是否存在
        Credit oldCredit = creditService.getById(id);
        ThrowUtils.throwIf(oldCredit == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldCredit.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = creditService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param creditUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateCredit(@RequestBody CreditUpdateRequest creditUpdateRequest) {
        if (creditUpdateRequest == null || creditUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Credit credit = new Credit();
        BeanUtils.copyProperties(creditUpdateRequest, credit);
        long id = creditUpdateRequest.getId();
        // 判断是否存在
        Credit oldCredit = creditService.getById(id);
        ThrowUtils.throwIf(oldCredit == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = creditService.updateById(credit);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Credit> getCreditById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Credit credit = creditService.getById(id);
        if (credit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(credit);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param creditQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Credit>> listCreditByPage(@RequestBody CreditQueryRequest creditQueryRequest,
                                                       HttpServletRequest request) {
        long current = creditQueryRequest.getCurrent();
        long size = creditQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Credit> creditPage = creditService.page(new Page<>(current, size),
                getQueryWrapper(creditQueryRequest));
        return ResultUtils.success(creditPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param creditQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Credit>> listMyCreditByPage(@RequestBody CreditQueryRequest creditQueryRequest,
                                                         HttpServletRequest request) {
        if (creditQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser();
        creditQueryRequest.setUserId(loginUser.getId());
        long current = creditQueryRequest.getCurrent();
        long size = creditQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Credit> creditPage = creditService.page(new Page<>(current, size),
                getQueryWrapper(creditQueryRequest));
        return ResultUtils.success(creditPage);
    }

    // endregion


    /**
     * 编辑（用户）
     *
     * @param creditEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editCredit(@RequestBody CreditEditRequest creditEditRequest, HttpServletRequest request) {
        if (creditEditRequest == null || creditEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Credit credit = new Credit();
        BeanUtils.copyProperties(creditEditRequest, credit);
        User loginUser = userService.getLoginUser();
        long id = creditEditRequest.getId();
        // 判断是否存在
        Credit oldCredit = creditService.getById(id);
        ThrowUtils.throwIf(oldCredit == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        UserTo userTo = new UserTo();
        BeanUtils.copyProperties(loginUser,userTo);
        if (!oldCredit.getUserId().equals(loginUser.getId()) && !userService.isAdmin(userTo)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = creditService.updateById(credit);
        return ResultUtils.success(result);
    }

    /**
     * 每日签到
     * @param request
     * @return
     */
    @GetMapping("/sign")
    public BaseResponse<Boolean> signCredit(HttpServletRequest request) {
        User loginUser = userService.getLoginUser();
        Long userId = loginUser.getId();
        Boolean result = creditService.signUser(userId);
        return ResultUtils.success(result);
    }


    private QueryWrapper<Credit> getQueryWrapper(CreditQueryRequest creditQueryRequest) {
        QueryWrapper<Credit> queryWrapper = new QueryWrapper<>();

        if (creditQueryRequest == null) {
            return queryWrapper;
        }

        Long creditTotal = creditQueryRequest.getCreditTotal();
        Date updateTime = creditQueryRequest.getUpdateTime();
        Date createTime = creditQueryRequest.getCreateTime();
        String sortField = creditQueryRequest.getSortField();
        String sortOrder = creditQueryRequest.getSortOrder();
        Long id = creditQueryRequest.getId();
        Long userId = creditQueryRequest.getUserId();


        queryWrapper.eq(id!=null &&id>0,"id",id);
        queryWrapper.like(ObjectUtils.isNotEmpty(creditTotal),"creditTotal",creditTotal);
        //小于等于查询时间
        queryWrapper.le(ObjectUtils.isNotEmpty(updateTime),"updateTime",updateTime);
        queryWrapper.le(ObjectUtils.isNotEmpty(createTime),"createTime",createTime);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId),"userId",userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}
