//package com.lnf.client.config;
//
//
//import com.lnf.tenant.core.config.TenantDataSource;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.stereotype.Component;
//
//import javax.sql.DataSource;
//import java.io.Serializable;
//import java.util.HashMap;
//
//@Component
//@RequiredArgsConstructor
//@Lazy
//public class TenantData implements Serializable {
//
//    private final HashMap<String, DataSource> dataSources = new HashMap<>();
//
////    private final DataSourceConfigRepository configRepo;
//
//    private final TenantDataSource source;
//
//    public DataSource getDataSource(String name) {
//        System.out.println(name + "   name of tenant");
//
//        return source.getDataSource(name);
//    }
////
////    @PostConstruct
////    public Map<String, DataSource> getAll() {
////        List<DataSourceConfig> configList = configRepo.findAll().stream().filter(DataSourceConfig::isInitialize).toList();
////        Map<String, DataSource> result = new HashMap<>();
////        for (DataSourceConfig config : configList) {
////            DataSource dataSource = getDataSource(config.getName());
////            result.put(config.getName(), dataSource);
////        }
////        return result;
////    }
////
////    private DataSource createDataSource(String name) {
////        DataSourceConfig config = configRepo.findByName(name);
////        if (config != null) {
////            DataSourceBuilder<?> factory = DataSourceBuilder
////                    .create().driverClassName(config.getDriverClassName())
////                    .username(config.getUsername())
////                    .password(config.getPassword())
////                    .url(config.getUrl());
////            DataSource ds = factory.build();
////            return ds;
////        }
////        return null;
////    }
////
//}