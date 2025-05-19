package com.aerospike.generator.example;

import com.aerospike.generator.Generator;

public class Runner {

    public static void main(String[] args) throws Exception{
        Generator generator = new Generator(Member.class);
        generator.generate(1, 10, 1, Member.class, null, 
                (member) -> System.out.println(member.toString()));
        generator.monitor();
    }
}
