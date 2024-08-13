package com.rain.usercenter.service;

import com.rain.usercenter.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class AlgorithmUtilsTest {


//@Test
//    void test(){
//
//    String str1 = "我是谁";
//    String str2 = "我是你吗";
//    String str3 = "我是不是你";
//
//    int score1 = AlgorithmUtils.minDistance(str1, str2);
//    int score2 = AlgorithmUtils.minDistance(str1, str3);
//    System.out.println(score1);
//    System.out.println(score2);
//
//}
@Test
    void testCompareTags(){

//        String str1 = "我是谁";
//        String str2 = "我是你吗";
//        String str3 = "我是不是你";
    List<String> tagList1 = Arrays.asList("java", "大一", "男");
    List<String> tagList2 = Arrays.asList("java", "大二", "女");
    List<String> tagList3 = Arrays.asList("Python", "大二", "男");

        int score1 = AlgorithmUtils.minDistance(tagList1, tagList2);
        int score2 = AlgorithmUtils.minDistance(tagList1, tagList3);
        System.out.println(score1);
        System.out.println(score2);

    }


}
