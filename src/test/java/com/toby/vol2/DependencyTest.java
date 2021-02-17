package com.toby.vol2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.StaticApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DependencyTest {

    @Test
    public void registerBeanWithDependency() {
        // 코드로 설정 메타정보를 등록하는 기능을 제공하는 애플리케이션 컨텍스트
        StaticApplicationContext ac = new StaticApplicationContext();

        // 가장 기본적인 BeanDefinition 인터페이스의 구현체인 RootBeanDefinition 을 사용하여 StringPrinter 빈 설정 정보를 만들고
        // 이를 printer 라는 이름으로 빈을 등록해줍니다.
        ac.registerBeanDefinition("printer", new RootBeanDefinition(StringPrinter.class));


        // Hello 클래스도 빈 설정 정보를 만들어서 각 속성 값에 대한 정보를 추가해줍니다.
        // printer 속성의 경우 Runtime 에 참조하게 됨으로 RuntimeReference 에 "printer" 라는 빈을 참조하도록 설정합니다.
        BeanDefinition helloDef = new RootBeanDefinition(Hello.class);
        helloDef.getPropertyValues().addPropertyValue("name", "Spring");
        helloDef.getPropertyValues().addPropertyValue("printer", new RuntimeBeanReference("printer"));

        // 빈 설정을 등록합니다.
        ac.registerBeanDefinition("hello", helloDef);


        // 빈을 가져와서 실행해봅니다.
        Hello hello = ac.getBean("hello", Hello.class);
        hello.print();

        // print 메소드를 호출하면 Hello 가 참조하는 StringPrinter 가 호출됩니다.
        // 따라서 StringPrinter 의 buffer 에 Hello Spring 이라는 값이 append 되겠죠.
        assertEquals("Hello Spring", ac.getBean("printer").toString());
    }
}
