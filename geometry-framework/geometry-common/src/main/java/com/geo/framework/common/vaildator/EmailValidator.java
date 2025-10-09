package com.geo.framework.common.vaildator;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static com.geo.framework.common.util.Constants.REGEX_EMAIL;

/*
creator：AZERL7
createTime：12:50
*/
public class EmailValidator implements ConstraintValidator<Email,String> {
    @Override
    public void initialize(Email constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return email!=null && email.matches(REGEX_EMAIL);//使用hutool的regex
    }

}
