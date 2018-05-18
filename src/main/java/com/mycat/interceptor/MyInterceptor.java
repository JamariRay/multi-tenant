package com.mycat.interceptor;

import com.mycat.com.mycat.utils.SessionUtil;
import com.mycat.controller.TestController;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import javax.websocket.Session;
import java.sql.Connection;
import java.util.Properties;

@Intercepts(value = {
        @Signature(type = StatementHandler.class,
                method = "prepare",
                args = {Connection.class,Integer.class})})
public class MyInterceptor implements Interceptor {
    private static final String preState="/*!mycat:datanode=";
    private static final String afterState="*/";

    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler=(StatementHandler)invocation.getTarget();
        MetaObject metaStatementHandler=SystemMetaObject.forObject(statementHandler);
        Object object=null;
        //分离代理对象链
        while(metaStatementHandler.hasGetter("h")){
            object=metaStatementHandler.getValue("h");
            metaStatementHandler=SystemMetaObject.forObject(object);
        }
        statementHandler=(StatementHandler)object;
        String sql=(String)metaStatementHandler.getValue("delegate.boundSql.sql");
        /*
        String node=(String)TestController.threadLocal.get();
        */
        String node=(String)SessionUtil.getSession().getAttribute("corp");
        if(node!=null) {
            sql = preState + node + afterState + sql;
        }

        System.out.println("sql is "+sql);
        metaStatementHandler.setValue("delegate.boundSql.sql",sql);
        Object result = invocation.proceed();
        System.out.println("Invocation.proceed()");
        return result;
    }

    public Object plugin(Object target) {

        return Plugin.wrap(target, this);
    }

    public void setProperties(Properties properties) {
        String prop1 = properties.getProperty("prop1");
        String prop2 = properties.getProperty("prop2");
        System.out.println(prop1 + "------" + prop2);
    }
}
