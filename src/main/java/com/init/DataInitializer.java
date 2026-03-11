package com.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.enums.RoleEnum;
import com.common.enums.UserStatus;
import com.entity.User;
import com.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init-admin.enabled:true}")
    private boolean enabled;

    @Value("${app.init-admin.username:admin}")
    private String username;

    @Value("${app.init-admin.password:123456}")
    private String password;

    @Value("${app.init-admin.real-name:系统管理员}")
    private String realName;

    @Bean
    public ApplicationRunner initAdminRunner() {
        return args -> {
            if (!enabled) {
                return;
            }
            User admin = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, username)
                    .eq(User::getDeleted, 0)
                    .last("limit 1"));
            if (admin == null) {
                admin = new User();
                admin.setUsername(username);
                admin.setPassword(passwordEncoder.encode(password));
                admin.setRealName(realName);
                admin.setRole(RoleEnum.ADMIN.name());
                admin.setStatus(UserStatus.ACTIVE.name());
                userMapper.insert(admin);
                return;
            }
            admin.setPassword(passwordEncoder.encode(password));
            admin.setRealName(realName);
            admin.setRole(RoleEnum.ADMIN.name());
            admin.setStatus(UserStatus.ACTIVE.name());
            userMapper.updateById(admin);
        };
    }
}
