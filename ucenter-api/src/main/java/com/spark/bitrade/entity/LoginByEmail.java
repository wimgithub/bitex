package com.spark.bitrade.entity;

import lombok.Data;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;

/**
 * @author Zhang Jinwei
 * @date 2017年12月29日
 */
@Data
public class LoginByEmail {

    @NotBlank(message = "{LoginByEmail.email.null}")
    @Email(message = "{LoginByEmail.email.format}")
    private String email;

    @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[\\s\\S]{6,20}$",message = "{LoginByEmail.password.Pattern}")
    private String password;

    @NotBlank(message = "{LoginByEmail.username.null}")
    @Length(min = 3, max = 20, message = "{LoginByEmail.username.length}")
    private String username;

    @NotBlank(message =  "{LoginByEmail.country.null}")
    private String country;

    private String promotion;

}
