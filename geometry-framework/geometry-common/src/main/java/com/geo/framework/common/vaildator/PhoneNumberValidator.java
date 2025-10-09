package com.geo.framework.common.vaildator;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import javax.print.DocFlavor;
import java.awt.event.ContainerListener;

import static com.geo.framework.common.util.Constants.REGEX_CHINA_PHONE;

/*
creator：AZERL7
createTime：12:40
*/
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber,String> {

    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
        //进行初始化操作
    }
    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext constraintValidatorContext) {
        //使用正则表达式进行校验
        return phoneNumber != null && phoneNumber.matches(REGEX_CHINA_PHONE);
    }
}
