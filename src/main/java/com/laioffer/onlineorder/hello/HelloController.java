package com.laioffer.onlineorder.hello;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController//给spring boot看
public class HelloController {


    @GetMapping("/hello")
    public Person sayHello(@RequestParam(required = false) String name) {
        //@RequestParam意思是参数是否需要提供，如果required=true，意味着必须要传参数
        if(name == null){
            name = "Guest";
        }
        return new Person(
                name,
                "laioffer",
                new Address("123 Happy Street", null, null, null),
                new Book("Clean Code", null)
        );
    }
}

