package com.example.rtnt;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class WipeMongoDatabaseTestExecutionListener extends AbstractTestExecutionListener {
    @Override
    public void beforeTestMethod(TestContext testContext) {
        if (!testContext.hasApplicationContext()) {
            return;
        }
        var context = testContext.getApplicationContext();
        if (context.getBeanNamesForType(MongoTemplate.class).length == 0) {
            return;
        }
        MongoTestDatabase.drop(context.getBean(MongoTemplate.class));
    }
}
