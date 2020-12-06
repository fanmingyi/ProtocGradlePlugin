package com.example.javamodule;

public class MainJava {
    public static void main(String[] args) {
        //不能访问另一个SourceSet代码
//        MyJavaClass myJavaClass = new MyJavaClass();
        System.out.printf(":::" + com.example.tutorial.AddressBookProtos.class);

//        Log.e("MainActivity", "测试  " +::class.java)

    }
}
