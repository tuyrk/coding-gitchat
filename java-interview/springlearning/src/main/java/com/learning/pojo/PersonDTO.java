package com.learning.pojo;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class PersonDTO {
    @NotNull(message = "姓名不能为空")
    private String name;

    @Min(value = 18,message = "年龄不能低于18岁")
    private int age;
}
