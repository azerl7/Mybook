package com.geo.mybook.distributed.id.generator.biz.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.geo.mybook.distributed.id.generator.biz.Constants;
import com.geo.mybook.distributed.id.generator.biz.core.IDGen;
import com.geo.mybook.distributed.id.generator.biz.core.common.PropertyFactory;
import com.geo.mybook.distributed.id.generator.biz.core.common.Result;
import com.geo.mybook.distributed.id.generator.biz.core.common.ZeroIDGen;
import com.geo.mybook.distributed.id.generator.biz.core.segment.SegmentIDGenImpl;
import com.geo.mybook.distributed.id.generator.biz.core.segment.dao.IDAllocDao;
import com.geo.mybook.distributed.id.generator.biz.core.segment.dao.impl.IDAllocDaoImpl;
import com.geo.mybook.distributed.id.generator.biz.exception.InitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Properties;

@Service("SegmentService")
public class SegmentService {
    private Logger logger = LoggerFactory.getLogger(SegmentService.class);

    private IDGen idGen;
    private DruidDataSource dataSource;

    public SegmentService() throws SQLException, InitException {

        Properties properties = PropertyFactory.getProperties();
        boolean flag = Boolean.parseBoolean(properties.getProperty(Constants.LEAF_SEGMENT_ENABLE, "true"));
        if (flag) {
            // Config dataSource
            dataSource = new DruidDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");//更换为新的mysql驱动
            dataSource.setUrl(properties.getProperty(Constants.LEAF_JDBC_URL));
            dataSource.setUsername(properties.getProperty(Constants.LEAF_JDBC_USERNAME));
            dataSource.setPassword(properties.getProperty(Constants.LEAF_JDBC_PASSWORD));
            dataSource.setValidationQuery("select 1");//验证语句用于连接时验证，在一些场景下确保应用的可用性
                                    //1、从连接池获取连接时
                                    //2、连接池定期检查闲置连接的时候
                                    //3、归还连接的时候
            dataSource.init();

            // Config Dao
            IDAllocDao dao = new IDAllocDaoImpl(dataSource);

            // Config ID Gen
            idGen = new SegmentIDGenImpl();
            ((SegmentIDGenImpl) idGen).setDao(dao);
            if (idGen.init()) {
                logger.info("Segment Service Init Successfully");
            } else {
                throw new InitException("Segment Service Init Fail");
            }
        } else {
            idGen = new ZeroIDGen();
            logger.info("Zero ID Gen Service Init Successfully");
        }
    }

    public Result getId(String key) {
        return idGen.get(key);
    }

    public SegmentIDGenImpl getIdGen() {
        if (idGen instanceof SegmentIDGenImpl) {
            return (SegmentIDGenImpl) idGen;
        }
        return null;
    }
}
