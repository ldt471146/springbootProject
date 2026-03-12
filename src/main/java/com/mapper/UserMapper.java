package com.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM sys_user WHERE id = #{id} LIMIT 1")
    User selectIncludingDeletedById(@Param("id") Long id);
}
