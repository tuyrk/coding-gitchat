package com.interview;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 数组和排序算法的应用 + 面试题
 */
public class Lesson1_7 {
    // 数组传参示例
    @Test
    public void arrayParam() {
        int[] intArray = {2, 3, 4, 8};
        change(intArray);
        System.out.println(intArray[2]);
    }

    // 字符串转数组
    @Test
    public void string2Array() {
        String str = "laowang,stone,wanglei";
        String[] strArray = str.split(","); // 字符串转数组
        System.out.println(strArray.length);
        System.out.println(strArray[0]);
    }

    // 数组转字符串
    @Test
    public void array2String() {
        String[] arr = {"laowang", "stone", "wanglei"};

        // 方式一：遍历
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i != arr.length - 1) {
                sb.append(",");
            }
        }
        System.out.println(sb.toString());

        // 方式二：Arrays.toString
        String str2 = Arrays.toString(arr);
        System.out.println(str2);

        // 方式三：StringUtils.join
        String str3 = StringUtils.join(Arrays.asList(arr), ","); // 使用英文逗号分隔
        System.out.println(str3);
    }


    // 数组初始化
    @Test
    public void initArray() {
        // 初始化方式一
        int[] initArray1 = new int[5];
        // 初始化方式二
        int[] initArray2 = new int[]{1, 2, 3, 4, 5};
        // 初始化方式二的延伸版，可省略 new int[] 直接赋值
        int[] initArray3 = {1, 2, 3, 4, 5};

        System.out.println(Arrays.toString(initArray1));
        System.out.println(Arrays.toString(initArray2));
        System.out.println(Arrays.toString(initArray3));
    }

    // 数组遍历
    @Test
    public void iterArray() {
        Integer[] itArray = {2, 3, 6, 7, 9};

        // 方式一：传统 for
        for (int i = 0; i < itArray.length; i++) {
            System.out.println(itArray[i]);
        }
        // 方式二：for each
        for (int i : itArray) {
            System.out.println(i);
        }
        // 方式三：jdk 8 Limbda
        Arrays.asList(itArray).forEach(x -> System.out.println(x));
    }

    // 初始化数组面试题 初始值
    @Test
    public void initArrayValue() {
        int[] intArr = new int[3];
        String[] StrArr = new String[3];
        System.out.println(intArr[1]);
        System.out.println(StrArr[1]);
    }

    //  Arrays.fill 填充值
    @Test
    public void testFill() {
        int[] fillArray = new int[5];

        // 给数组的所有元素分配一个值
        Arrays.fill(fillArray, 9);
        System.out.println(Arrays.toString(fillArray));

        Arrays.fill(fillArray, 1, 3, 8);
        System.out.println(Arrays.toString(fillArray));
    }

    //  Arrays.sort 排序
    @Test
    public void testSort() {
        String[] strArr = {"dog", "cat", "pig", "bird"};
        Arrays.sort(strArr);
        System.out.println(Arrays.toString(strArr));
    }

    // 数组值比较
    @Test
    public void testCompare() {
        String[] strArr1 = {"dog", "cat", "pig", "bird"};
        String[] strArr2 = {"dog", "cat", "pig", "bird"};

        System.out.println(Arrays.equals(strArr1, strArr2)); // true
        System.out.println(strArr1.equals(strArr2)); // false
        System.out.println(strArr1 == strArr2); // false
    }

    //  二分法查询元素 Arrays.binarySearch 的正确使用方式
    @Test
    public void testBinarySearch() {
        String[] searchArray = {"dog", "cat", "pig", "bird"};
        Arrays.sort(searchArray);
        // 使用二分法查询数组元素，查询到返回元素的索引位置，查找不到返回-1
        int result = Arrays.binarySearch(searchArray, "bird");
        System.out.println(result == -1);
    }

    //  二维数组的使用
    @Test
    public void testArrays() {
        // 声明二维数组
        int[][] twoArray = new int[2][4];
        //循环二维数组
        for (int i = 0; i < twoArray.length; i++) {
            for (int j = 0; j < twoArray[0].length; j++) {
                // 二维数组赋值
                twoArray[i][j] = j;
            }
        }
        // 二维数组取值
        System.out.println(twoArray[0][1]);
        // 打印二维数组
        System.out.println(Arrays.toString(twoArray[0]));
        System.out.println(Arrays.toString(twoArray[1]));
    }

    // 数组拷贝
    @Test
    public void testCopy() {
        int[] copyArray = {3, 4, 9};
        int[] copyArray2 = Arrays.copyOf(copyArray, 5);
        System.out.println(Arrays.toString(copyArray2));
    }

    //  数组合并
    @Test
    public void testUnion() {
        int[] addArray = {2, 8, 13, 11, 6, 7};
        int[] addArray2 = {66, 88};
        // 合并数组
        int[] addArray3 = org.apache.commons.lang3.ArrayUtils.addAll(addArray, addArray2);
        System.out.println(Arrays.toString(addArray3));
    }

    // 数组正序和逆序
    @Test
    public void testReverse() {
        int[] reverseArray = {2, 8, 13, 11, 6, 7};

        // 数组正序（排序）
        Arrays.sort(reverseArray);
        // 数组逆序
        org.apache.commons.lang3.ArrayUtils.reverse(reverseArray);
        System.out.println(Arrays.toString(reverseArray));
    }

    // 字符串数组某个值查询
    @Test
    public void testExist() {
        String[] findArray = {"doc", "pig", "cat"};

        // 方式一：Arrays.asList(array).contains
        boolean bool = Arrays.asList(findArray).contains("cat");
        System.out.println(bool);

        // 方式二：Arrays.binarySearch
        Arrays.sort(findArray);
        boolean bool2 = Arrays.binarySearch(findArray, "cat") > -1;
        System.out.println(bool2);
    }

    // 冒泡排序
    @Test
    public void bubbleSort() {
        int[] sortArray = {2, 8, 13, 11, 6, 7};
        System.out.println("排序前：" + Arrays.toString(sortArray));
        for (int i = 0; i < sortArray.length; i++) {
            for (int j = 0; j < sortArray.length; j++) {
                if (sortArray[i] > sortArray[j]) {
                    // 元素交换
                    int temp = sortArray[i];
                    sortArray[i] = sortArray[j];
                    sortArray[j] = temp;
                }
            }
        }
        System.out.println("排序后：" + Arrays.toString(sortArray));
    }

    // 选择排序
    @Test
    public void selectSort() {
        int[] selArray = {2, 8, 13, 11, 6, 7};
        System.out.println("排序前：" + Arrays.toString(selArray));
        for (int i = 0; i < selArray.length; i++) {
            int lowerIndex = i;
            for (int j = i + 1; j < selArray.length; j++) {
                // 找出最小的一个索引
                if (selArray[j] < selArray[lowerIndex]) {
                    lowerIndex = j;
                }
            }
            // 交换
            int temp = selArray[i];
            selArray[i] = selArray[lowerIndex];
            selArray[lowerIndex] = temp;
        }
        System.out.println("排序后：" + Arrays.toString(selArray));
    }

    // 数组转集合
    @Test
    public void array2List() {
        String[] listArray = {"cat", "dog"};
        List list = Arrays.asList(listArray);
        System.out.println(list);
    }

    // 集合转数组
    @Test
    public void list2Array() {
        List<String> strList = new ArrayList<>();
        strList.add("cat");
        strList.add("dog");
        // 集合转换为数组
        String[] strArrays = strList.toArray(new String[0]);
        System.out.println(Arrays.toString(strArrays));
    }

    // 数组元素赋值
    @Test
    public void fill() {
        int[] arrInt = new int[10];

        Arrays.fill(arrInt, 2, 5, 6);
        System.out.println(Arrays.toString(arrInt));

        for (int i = 0; i < arrInt.length; i++) {
            if (i >= 2 && i < 5) {
                arrInt[i] = 6;
            }
        }
        System.out.println(Arrays.toString(arrInt));
    }

    private static void change(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (i % 2 == 0) {
                arr[i] *= i;
            }
        }
    }

    @Test
    public void test() {
        String[] arr = {"laowang", "stone", "wanglei"};
        String str2 = Arrays.toString(arr);
        System.out.println(str2);
    }
}
