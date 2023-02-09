package cn.nean.boss.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("cn.nean.boss.mapper")
public class MybatisConfig {
}
